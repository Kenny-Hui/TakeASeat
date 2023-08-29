package com.lx.takeaseat;

import com.lx.takeaseat.config.Config;
import com.lx.takeaseat.data.BlockTagKeyWrapper;
import com.lx.takeaseat.data.SittingInstance;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
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
    private static final HashMap<UUID, SittingInstance> playerSitting = new HashMap<>();

    public static ActionResult onBlockRightClick(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult) {
        if(world.isClient() || player.isSneaking()) return ActionResult.PASS;
        BlockPos hittedBlockPos = blockHitResult.getBlockPos();
        BlockState blockState = world.getBlockState(hittedBlockPos);

        if(!playerCanSit(player, world, hittedBlockPos, blockState)) {
            return ActionResult.PASS;
        } else {
            addPlayerToSeat(world, blockState, hittedBlockPos, player);
            return ActionResult.SUCCESS;
        }
    }

    public static void addPlayerToSeat(World world, BlockState seatBlockState, BlockPos seatPos, PlayerEntity player) {
        if(playerSitting.containsKey(player.getUuid())) {
            removePlayerFromSeat(player, playerSitting.get(player.getUuid()).seatEntity);
        }

        Vec3d seatEntityPos = getSeatPosition(world, seatBlockState, seatPos);
        Entity sitEntity = spawnSeatEntity(world, seatEntityPos);
        player.startRiding(sitEntity);

        playerSitting.put(player.getUuid(), new SittingInstance(seatPos, sitEntity));
    }

    /**
     * This method attempts to remove the player from the seat if they are on one.
     * @param player The player the seat is removed for
     * @param mountedEntity Pass in if you are uncertain whether the player is currently riding a seat, or null to skip the check and always eject the player.
     */
    public static void removePlayerFromSeat(PlayerEntity player, Entity mountedEntity) {
        SittingInstance sittingInstance = SittingManager.playerSitting.get(player.getUuid());
        if(sittingInstance != null) {
            if(mountedEntity.getUuid() == sittingInstance.seatEntity.getUuid()) {
                player.dismountVehicle();
            }

            sittingInstance.seatEntity.kill();
            playerSitting.remove(player.getUuid());
            TakeASeat.LOGGER.debug("[TakeASeat] Killing seat entity as player dismounted.");
        }
    }

    private static boolean playerCanSit(PlayerEntity player, World world, BlockPos hittedBlockPos, BlockState blockState) {
        Block block = blockState.getBlock();
        Identifier blockId = Util.getBlockId(block);
        if(player.isSpectator()) return false;

        if(playerSitting.values().stream().anyMatch(e -> e.blockPos == hittedBlockPos)) {
            TakeASeat.LOGGER.debug("[TakeASeat] The seat is occupied by someone else.");
            return false;
        }

        if(Config.mustBeEmptyHandToSit() && !Util.playerHandIsEmpty(player)) {
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
            // This is a bit of a hack
            Direction dir = blockState.get(StairsBlock.FACING);
            BlockHalf half = blockState.get(StairsBlock.HALF);
            double offsetX = dir.getOffsetX() * 0.25;
            double offsetY = half == BlockHalf.TOP ? 0.5 : 0;
            double offsetZ = dir.getOffsetZ() * 0.25;

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
     * Spawn a seat entity (An invisible, invulnerable, no gravity armor stand)
     * @param world The world
     * @param pos A Vec3d position that the entity should spawn
     * @return The entity for the player to be ridden.
     */
    public static Entity spawnSeatEntity(World world, Vec3d pos) {
        ArmorStandEntity sitEntity = new ArmorStandEntity(world, pos.getX(), pos.getY() - 1.125, pos.getZ()) {

            @Override
            public void tick() {
                // Always face where the player is facing
                Entity firstPassenger = getFirstPassenger();
                if(firstPassenger != null) {
                    this.setHeadYaw(firstPassenger.getHeadYaw());
                    this.setYaw(firstPassenger.getYaw());
                    this.setPitch(firstPassenger.getPitch());
                } else {
                    this.kill();
                }

                super.tick();
            }
        };
        sitEntity.setNoGravity(true);
        sitEntity.setInvulnerable(true);
        sitEntity.setInvisible(true);
        world.spawnEntity(sitEntity);
        return sitEntity;
    }
}