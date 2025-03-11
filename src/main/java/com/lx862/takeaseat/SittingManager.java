package com.lx862.takeaseat;

import com.lx862.takeaseat.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
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
import java.util.Map;
import java.util.UUID;

public class SittingManager {
    private static final HashMap<UUID, BlockPos> playersSitting = new HashMap<>();

    public static ActionResult onBlockRightClick(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult) {
        if(world.isClient() || player.isSneaking()) return ActionResult.PASS;
        BlockPos hittedBlockPos = blockHitResult.getBlockPos();
        BlockState blockState = world.getBlockState(hittedBlockPos);

        if(!playerCanSit(player, world, hittedBlockPos, blockState)) {
            return ActionResult.PASS;
        } else {
            addPlayerToSeat(world, blockState, hittedBlockPos, player);
            return ActionResult.SUCCESS_SERVER;
        }
    }

    public static void addPlayerToSeat(World world, BlockState seatBlockState, BlockPos seatPos, PlayerEntity player) {
        if(player.hasVehicle()) {
            player.dismountVehicle();
        }

        Vec3d seatEntityPos = getSeatPosition(world, seatBlockState, seatPos);
        Entity sitEntity = spawnSeatEntity(world, seatEntityPos, seatPos);
        player.startRiding(sitEntity);

        playersSitting.put(player.getUuid(), seatPos);
    }
    public static void removeBlockPosFromSeat(BlockPos seatPos) {
        for(Map.Entry<UUID, BlockPos> entry : new HashMap<>(playersSitting).entrySet()) {
            if(Util.blockPosEquals(entry.getValue(), seatPos)) {
                playersSitting.remove(entry.getKey());
            }
        }
    }

    private static boolean playerCanSit(PlayerEntity player, World world, BlockPos hittedBlockPos, BlockState blockState) {
        Block block = blockState.getBlock();
        Identifier blockId = Util.getBlockId(block);
        Config config = TakeASeat.getConfig();
        if(player.isSpectator()) return false;

        if(!player.hasPermissionLevel(config.requiredOpLevel())) {
            TakeASeat.LOGGER.debug("[TakeASeat] Player don't have permission to sit.");
            return false;
        }

        if(playersSitting.values().stream().anyMatch(e -> Util.blockPosEquals(e, hittedBlockPos))) {
            TakeASeat.LOGGER.debug("[TakeASeat] The seat has already been occupied.");
            return false;
        }

        if(config.mustBeEmptyHandToSit() && !Util.playerHandIsEmpty(player)) {
            TakeASeat.LOGGER.debug("[TakeASeat] Player is holding something.");
            return false;
        }

        if(!config.blockIdIsAllowed(blockId) && !blockInTag(blockState)) {
            TakeASeat.LOGGER.debug("[TakeASeat] Block is not allowed to sit.");
            return false;
        }

        if(config.blockMustBeLowerThanPlayer() && hittedBlockPos.getY() - 0.5 > player.getY()) {
            TakeASeat.LOGGER.debug("[TakeASeat] Seat Block is higher than the player.");
            return false;
        }

        if(config.maxDistance() > 0 && Util.euclideanDistance(hittedBlockPos, player.getBlockPos()) > config.maxDistance()) {
            TakeASeat.LOGGER.debug("[TakeASeat] Player is too far from seat.");
            return false;
        }

        if(config.ensurePlayerWontSuffocate() && blockAboveCanSuffocate(world, hittedBlockPos)) {
            TakeASeat.LOGGER.debug("[TakeASeat] Player would suffocate if they tried to sit.");
            return false;
        }

        if(config.mustNotBeObstructed() && hasObstruction(world, hittedBlockPos, player.getEyePos())) {
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
        for(TagKey<Block> tag : TakeASeat.getConfig().getAllowedBlockTag()) {
            if(blockState.isIn(tag)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasObstruction(World world, BlockPos targetBlockPos, Vec3d playerPos) {
        /* Jank code begins! */
        Vec3d targetPos = Util.toVec3d(targetBlockPos);
        BlockPos playerBlockPos = Util.toBlockPos(playerPos.x, playerPos.y, playerPos.z);
        double distance = Util.euclideanDistance(targetPos, playerPos, false);
        double increment = 1 / distance;
        double lerpProgress = 0;
        double lowestY = Math.min(targetPos.y, playerPos.y);
        double highestY = Math.max(targetPos.y, playerPos.y);
        double yDifference = highestY - lowestY;

        while(lerpProgress < 1) {
            lerpProgress += increment;

            for(int i = 0; i < yDifference; i++) {
                Vec3d lerped = targetPos.lerp(playerPos, lerpProgress);
                BlockPos finalPos = Util.toBlockPos(lerped.x, lowestY + i, lerped.z);
                if(Util.equalXZBlockPos(playerBlockPos, finalPos) || finalPos.equals(targetBlockPos)) continue;

                BlockState blockState = world.getBlockState(finalPos);
                if(blockState.getCollisionShape(world, finalPos) != VoxelShapes.empty()) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Vec3d getSeatPosition(World world, BlockState blockState, BlockPos pos) {
        Vec3d centeredBlockPos = Vec3d.ofBottomCenter(pos);
        if(blockState.getBlock() instanceof StairsBlock) {
            Direction dir = blockState.get(StairsBlock.FACING);
            BlockHalf half = blockState.get(StairsBlock.HALF);
            double offsetX = dir.getOffsetX() * 0.25;
            double offsetY = half == BlockHalf.TOP ? 0.5 : 0;
            double offsetZ = dir.getOffsetZ() * 0.25;

            if(!TakeASeat.getConfig().stairs025Offset()) {
                offsetX = 0;
                offsetZ = 0;
            }

            centeredBlockPos = new Vec3d(centeredBlockPos.getX() - offsetX, centeredBlockPos.getY() + offsetY, centeredBlockPos.getZ() - offsetZ);
        }

        if(blockState.getBlock() instanceof SlabBlock) {
            SlabType slabType = blockState.get(SlabBlock.TYPE);
            if(slabType == SlabType.TOP || slabType == SlabType.DOUBLE) {
                centeredBlockPos = new Vec3d(centeredBlockPos.getX(), pos.getY() + 0.5, centeredBlockPos.getZ());
            }
        }

        if(blockState.isFullCube(world, pos)) {
            centeredBlockPos = new Vec3d(centeredBlockPos.getX(), pos.getY() + 0.5, centeredBlockPos.getZ());
        }

        return centeredBlockPos;
    }

    /**
     * Spawn a seat entity (An invisible, invulnerable, area effect cloud)
     * @param world The world
     * @param pos A Vec3d position that the entity should spawn
     * @return The entity for the player to be ridden.
     */
    public static Entity spawnSeatEntity(World world, Vec3d pos, BlockPos seatPos) {
        AreaEffectCloudEntity sitEntity = new AreaEffectCloudEntity(world, pos.getX(), pos.getY(), pos.getZ()) {
            @Override
            public void tick() {
                Entity firstPassenger = getFirstPassenger();
                if(firstPassenger == null || world.getBlockState(seatPos).isAir()) {
                    removeBlockPosFromSeat(seatPos);
                    this.kill((ServerWorld)world);
                }

                super.tick();
            }
        };
        sitEntity.setNoGravity(true);
        sitEntity.setInvulnerable(true);
        sitEntity.setInvisible(true);
        sitEntity.setWaitTime(0);
        sitEntity.setRadius(0);
        sitEntity.setDuration(Integer.MAX_VALUE);
        world.spawnEntity(sitEntity);
        return sitEntity;
    }
}