package com.lx.takeaseat;

import com.lx.takeaseat.config.Config;
import com.lx.takeaseat.data.SittingData;
import com.lx.takeaseat.data.BlockTagKeyWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class SittingManager {
    public static final HashMap<UUID, SittingData> playerSitting = new HashMap<>();

    public static ActionResult onBlockRightClick(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult) {
        if(world.isClient() || player.isSneaking()) return ActionResult.PASS;
        BlockPos hittedBlockPos = blockHitResult.getBlockPos();
        BlockState blockState = world.getBlockState(hittedBlockPos);

        if(!playerCanSit(player, world, hittedBlockPos, blockState)) {
            return ActionResult.PASS;
        }

        Vec3d seatPos = getSeatSpawnPos(world, blockState, hittedBlockPos);
        Entity sitEntity = spawnSeatEntity(world, seatPos);
        player.startRiding(sitEntity);

        playerSitting.put(player.getUuid(), new SittingData(hittedBlockPos, sitEntity));
        return ActionResult.SUCCESS;
    }

    /**
     * This does the necessary checking with the Config to ensure the player are allowed to sit
     * @return Whether the player can sit
     */
    private static boolean playerCanSit(PlayerEntity player, World world, BlockPos hittedBlockPos, BlockState blockState) {
        Block block = blockState.getBlock();
        Identifier blockId = Util.getBlockId(block);
        if(player.isSpectator()) return false;

        if(playerSitting.values().stream().anyMatch(e -> e.blockPos == hittedBlockPos)) {
            TakeASeat.LOGGER.debug("[TakeASeat] The seat is already occupied by someone else.");
            return false;
        }

        if(Config.mustBeEmptyHandToSit() && !playerHandIsEmpty(player)) {
            TakeASeat.LOGGER.debug("[TakeASeat] Player is holding something.");
            return false;
        }

        if(!Config.blockIdIsAllowed(blockId) && !blockInTag(blockState)) {
            TakeASeat.LOGGER.debug("[TakeASeat] Block is not allowed to sit.");
            return false;
        }

        if(Config.blockMustBeLowerThanPlayer() && hittedBlockPos.getY() - 0.5 > player.getY()) {
            TakeASeat.LOGGER.debug("[TakeASeat] Seat Block is higher than the player.");
            return false;
        }

        if(Config.maxDistance() > 0 && Util.euclideanDistance(hittedBlockPos, player.getBlockPos()) > Config.maxDistance()) {
            TakeASeat.LOGGER.debug("[TakeASeat] Player is too far from seat.");
            return false;
        }

        if(Config.ensurePlayerWontSuffocate() && blockAboveCanSuffocate(world, hittedBlockPos)) {
            TakeASeat.LOGGER.debug("[TakeASeat] Player would suffocate if they tried to sit.");
            return false;
        }

        if(Config.mustNotBeObstructed() && hasObstruction(world, hittedBlockPos, player.getEyePos())) {
            TakeASeat.LOGGER.debug("[TakeASeat] There's a block between the player and the seat.");
            return false;
        }

        return true;
    }

    private static boolean blockAboveCanSuffocate(World world, BlockPos pos) {
        BlockPos abovePos = pos.up();
        BlockState blockState = world.getBlockState(abovePos);
        return blockState.shouldSuffocate(world, abovePos);
    }

    private static boolean blockInTag(BlockState blockState) {
        for(BlockTagKeyWrapper tag : Config.getAllowedBlockTag()) {
            if(blockState.isIn(tag.get())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasObstruction(World world, BlockPos targetBlockPos, Vec3d playerPos) {
        /* This is not a really good way and could create false positive, but I am just going to leave it to the next person viewing this source code because yes. */
        Vec3d targetPos = Util.toVec3d(targetBlockPos);
        double distance = Util.euclideanDistance(targetPos, playerPos, false);
        double increment = 1 / distance;
        double progress = 0;
        double lowestY = Math.min(targetPos.y, playerPos.y);
        double highestY = Math.max(targetPos.y, playerPos.y);
        double yDifference = highestY - lowestY;

        while(progress < 1) {
            progress += increment;

            for(int i = 0; i < yDifference; i++) {
                Vec3d lerped = targetPos.lerp(playerPos, progress);
                BlockPos finalPos = Util.toBlockPos(lerped.x, lowestY + i, lerped.z);
                if(finalPos.equals(targetBlockPos)) continue;

                BlockState blockState = world.getBlockState(finalPos);
                if(blockState.getCollisionShape(world, finalPos) != VoxelShapes.empty()) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean playerHandIsEmpty(PlayerEntity player) {
        return player.getMainHandStack().isEmpty() && player.getOffHandStack().isEmpty();
    }

    private static Vec3d getSeatSpawnPos(World world, BlockState blockState, BlockPos pos) {
        Vec3d centeredBlockPos = Vec3d.ofBottomCenter(pos);
        if(blockState.getBlock() instanceof StairsBlock) {
            Direction dir = blockState.get(StairsBlock.FACING);
            double offsetX = dir.getOffsetX() * 0.25;
            double offsetY = dir.getOffsetY() * 0.25;
            double offsetZ = dir.getOffsetZ() * 0.25;
            centeredBlockPos = new Vec3d(centeredBlockPos.getX() - offsetX, centeredBlockPos.getY() + offsetY, centeredBlockPos.getZ() - offsetZ);
        }

        if(blockState.getBlock() instanceof SlabBlock) {
            SlabType slabType = blockState.get(SlabBlock.TYPE);
            if(slabType == SlabType.TOP || slabType == SlabType.DOUBLE) {
                double offsetY = 0.5;
                centeredBlockPos = new Vec3d(centeredBlockPos.getX(), centeredBlockPos.getY() + offsetY, centeredBlockPos.getZ());
            }
        }

        if(blockState.isFullCube(world, pos)) {
            double offsetY = 0.5;
            centeredBlockPos = new Vec3d(centeredBlockPos.getX(), pos.getY() + offsetY, centeredBlockPos.getZ());
        }

        return centeredBlockPos;
    }

    /**
     * Spawn a seat entity (An invisible, no gravity arrow)
     * @param world The world
     * @param pos A Vec3d position that the entity should spawn
     * @return The entity for the player to be ridden.
     */
    private static Entity spawnSeatEntity(World world, Vec3d pos) {
        Entity sitEntity = EntityType.ARROW.create(world);
        sitEntity.teleport(pos.x, pos.y, pos.z);
        sitEntity.setNoGravity(true);
        sitEntity.setInvisible(true);
        world.spawnEntity(sitEntity);
        return sitEntity;
    }
}