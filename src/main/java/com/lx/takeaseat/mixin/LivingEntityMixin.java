package com.lx.takeaseat.mixin;

import com.lx.takeaseat.SittingManager;
import com.lx.takeaseat.TakeASeat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(LivingEntity.class)
/*
 * Kill the seat entity after the player dismounted.
 */
public class LivingEntityMixin {

    @Inject(method = "onDismounted", at = @At("TAIL"))
    public void onDismounted(Entity vehicle, CallbackInfo ci) {
        LivingEntity thisEntity = ((LivingEntity)(Object)this);
        UUID thisUuid = thisEntity.getUuid();

        if(thisEntity.isPlayer() && SittingManager.playerSitting.containsKey(thisUuid)) {
            Entity seatEntity = SittingManager.playerSitting.get(thisUuid).seatEntity;
            if(vehicle.getUuid() == seatEntity.getUuid()) {
                seatEntity.kill();
                SittingManager.playerSitting.remove(thisUuid);
                TakeASeat.LOGGER.info("Killing seat entity as player dismounted");
            }
        }
    }
}