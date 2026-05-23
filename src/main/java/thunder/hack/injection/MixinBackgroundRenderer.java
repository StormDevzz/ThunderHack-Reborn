package thunder.hack.injection;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.misc.Weather;
import thunder.hack.features.modules.render.WorldTweaks;
import thunder.hack.setting.impl.ColorSetting;

@Mixin(FogRenderer.class)
public class MixinBackgroundRenderer {

    @Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;", at = @At("RETURN"), cancellable = true)
    private void onApplyFog(Camera camera, int viewDistance, boolean thick, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world, CallbackInfoReturnable<Vector4f> cir) {
        if (ModuleManager.worldTweaks.isEnabled() && WorldTweaks.fogModify.getValue().isEnabled()) {
            ColorSetting c = WorldTweaks.fogColor.getValue();
            cir.setReturnValue(new Vector4f(c.getGlRed(), c.getGlGreen(), c.getGlBlue(), 1.0f));
        }
        if (ModuleManager.weather.isEnabled() && ModuleManager.weather.weatherMode.is(Weather.WeatherMode.Fog)) {
            Vector4f original = cir.getReturnValue();
            cir.setReturnValue(new Vector4f(original.x(), original.y(), original.z(), 1.0f));
        }
    }

    @ModifyVariable(method = "method_71110", at = @At("HEAD"), index = 3, argsOnly = true, remap = false)
    private float modifyFogEnvironmentalStart(float environmentalStart) {
        if (ModuleManager.worldTweaks.isEnabled() && WorldTweaks.fogModify.getValue().isEnabled()) {
            return WorldTweaks.fogStart.getValue();
        }
        if (ModuleManager.weather.isEnabled() && ModuleManager.weather.weatherMode.is(Weather.WeatherMode.Fog)) {
            return ModuleManager.weather.fogStart.getValue();
        }
        return environmentalStart;
    }

    @ModifyVariable(method = "method_71110", at = @At("HEAD"), index = 4, argsOnly = true, remap = false)
    private float modifyFogEnvironmentalEnd(float environmentalEnd) {
        if (ModuleManager.worldTweaks.isEnabled() && WorldTweaks.fogModify.getValue().isEnabled()) {
            return WorldTweaks.fogEnd.getValue();
        }
        if (ModuleManager.weather.isEnabled() && ModuleManager.weather.weatherMode.is(Weather.WeatherMode.Fog)) {
            return ModuleManager.weather.fogEnd.getValue();
        }
        return environmentalEnd;
    }
}
