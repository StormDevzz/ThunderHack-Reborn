package thunder.hack.injection;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class MixinModel {

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/EntityRenderState;)V", at = @At("HEAD"))
    private void setAnglesHook(EntityRenderState state, CallbackInfo ci) {
        // 1.21.4 использует EntityRenderState вместо LivingEntity + float параметров
    }
}