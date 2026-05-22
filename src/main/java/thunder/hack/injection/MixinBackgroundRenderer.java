package thunder.hack.injection;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.entity.Entity;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.misc.Weather;
import thunder.hack.features.modules.render.WorldTweaks;
import thunder.hack.setting.impl.ColorSetting;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {

    @Inject(method = "applyFog", at = @At("RETURN"), cancellable = true)
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, Vector4f fogColor, float viewDistance, boolean thickFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
        if (ModuleManager.worldTweaks.isEnabled() && WorldTweaks.fogModify.getValue().isEnabled()) {
            Fog original = cir.getReturnValue();
            float start = WorldTweaks.fogStart.getValue();
            float end = WorldTweaks.fogEnd.getValue();
            ColorSetting c = WorldTweaks.fogColor.getValue();
            cir.setReturnValue(new Fog(start, end, original.shape(), c.getGlRed(), c.getGlGreen(), c.getGlBlue(), 1.0f));
        }
        if (ModuleManager.weather.isEnabled() && ModuleManager.weather.weatherMode.is(Weather.WeatherMode.Fog)) {
            Fog original = cir.getReturnValue();
            cir.setReturnValue(new Fog(ModuleManager.weather.fogStart.getValue(), ModuleManager.weather.fogEnd.getValue(), original.shape(), original.red(), original.green(), original.blue(), 1.0f));
        }
    }

    @Inject(method = "getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;", at = @At("HEAD"), cancellable = true)
    private static void onGetFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable<Object> cir) {
        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.blindness.getValue()) cir.setReturnValue(null);
    }
}
