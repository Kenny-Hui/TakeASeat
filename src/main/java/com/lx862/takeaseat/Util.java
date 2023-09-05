package com.lx862.takeaseat;

import com.google.gson.JsonArray;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;

public class Util {
    public static Identifier getBlockId(Block block) {
        #if MC_VERSION >= "11903"
            return net.minecraft.registry.Registries.BLOCK.getId(block);
        #else
            return net.minecraft.util.registry.Registry.BLOCK.getId(block);
        #endif
    }

    public static double euclideanDistance(BlockPos pos1, BlockPos pos2) {
        int x1 = pos1.getX();
        int x2 = pos2.getX();
        int z1 = pos1.getZ();
        int z2 = pos2.getZ();
        int yDifference = Math.abs(pos2.getY() - pos1.getY());
        return yDifference + Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(z2 - z1, 2));
    }

    public static double euclideanDistance(Vec3d pos1, Vec3d pos2, boolean accountForY) {
        double x1 = pos1.x;
        double x2 = pos2.x;
        double z1 = pos1.z;
        double z2 = pos2.z;
        double yDifference = accountForY ? Math.abs(pos2.getY() - pos1.getY()) : 0;
        return yDifference + (Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(z2 - z1, 2)));
    }

    public static Vec3d toVec3d(BlockPos pos) {
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos toBlockPos(double x, double y, double z) {
        return new BlockPos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    public static boolean blockPosEquals(BlockPos pos1, BlockPos pos2) {
        return pos1.getX() == pos2.getX() && pos1.getY() == pos2.getY() && pos1.getZ() == pos2.getZ();
    }

    public static JsonArray toJsonArray(Collection<?> iterable) {
        JsonArray array = new JsonArray();
        for(Object object : iterable) {
            array.add(object.toString());
        }
        return array;
    }

    public static boolean equalXZBlockPos(BlockPos pos1, BlockPos pos2) {
        return pos1.getX() == pos2.getX() && pos1.getZ() == pos2.getZ();
    }

    static boolean playerHandIsEmpty(PlayerEntity player) {
        return player.getMainHandStack().isEmpty() && player.getOffHandStack().isEmpty();
    }
}
