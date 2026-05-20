package thunder.hack.injection;

import net.minecraft.world.dimension.DimensionType;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.render.Fullbright;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {

    @Inject(method = "getDarknessFactor(F)F", at = @At("HEAD"), cancellable = true)
    private void getDarknessFactor(float tickDelta, CallbackInfoReturnable<Float> info) {
        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.darkness.getValue()) info.setReturnValue(0.0f);
        if (ModuleManager.fullbright.isEnabled()) info.setReturnValue(0.0f);
    }

    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private static void getBrightnessHook(DimensionType type, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.fullbright.isEnabled()) {
            cir.setReturnValue(Fullbright.minBright.getValue());
        }
    }
}
