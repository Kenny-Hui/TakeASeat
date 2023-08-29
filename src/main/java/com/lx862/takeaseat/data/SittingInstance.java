package com.lx862.takeaseat.data;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;


public class SittingInstance {
    public BlockPos blockPos;
    public Entity seatEntity;

    public SittingInstance(BlockPos blockPos, Entity seatEntity) {
        this.blockPos = blockPos;
        this.seatEntity = seatEntity;
    }
}
