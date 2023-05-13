package com.lx.takeaseat.data;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;


public class SittingData {
    public BlockPos blockPos;
    public Entity seatEntity;

    public SittingData(BlockPos blockPos, Entity seatEntity) {
        this.blockPos = blockPos;
        this.seatEntity = seatEntity;
    }
}
