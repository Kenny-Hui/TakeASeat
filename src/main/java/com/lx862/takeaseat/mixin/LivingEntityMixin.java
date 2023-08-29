package com.lx862.takeaseat.mixin;

import com.lx862.takeaseat.SittingManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)

public class LivingEntityMixin {
    // On player dismounting any vehicle
    @Inject(method = "onDismounted", at = @At("TAIL"))
    public void onDismounted(Entity vehicle, CallbackInfo ci) {
        LivingEntity thisEntity = ((LivingEntity)(Object)this);

        if(!(thisEntity instanceof PlayerEntity)) return;

        SittingManager.removePlayerFromSeat((PlayerEntity)thisEntity, vehicle);
    }
}