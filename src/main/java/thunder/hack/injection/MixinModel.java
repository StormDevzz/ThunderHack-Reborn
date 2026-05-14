package thunder.hack.injection;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.client.ModuleManager;

@Mixin(BipedEntityModel.class)
public abstract class MixinModel {

    @Inject(method = "setAngles", at = @At("HEAD"))
    private void setAnglesHook(LivingEntity livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (ModuleManager.smallUser != null && ModuleManager.smallUser.isEnabled()) {
            if (livingEntity instanceof PlayerEntity player) {
                if (ModuleManager.smallUser.shouldMakeSmall(player)) {
                    // Уменьшаем модель через child = true
                    ((BipedEntityModel<?>) (Object) this).child = true;
                }
            }
        }
    }
}