package thunder.hack.injection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.manager.client.ModuleManager;

@Mixin(Entity.class)
public class MixinLivingEntity {
    @Inject(method = "getPose", at = @At("HEAD"), cancellable = true)
    private void getPoseHook(CallbackInfoReturnable<EntityPose> cir) {
        if (ModuleManager.shiftInterp != null && ModuleManager.shiftInterp.isEnabled()) {
            if ((Object) this instanceof PlayerEntity player) {
                if (ModuleManager.shiftInterp.shouldShift(player)) {
                    cir.setReturnValue(EntityPose.CROUCHING);
                }
            }
        }
    }
}