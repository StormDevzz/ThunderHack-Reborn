package thunder.hack.injection;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticlesMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.client.ModuleManager;

@Mixin(WeatherRendering.class)
public class MixinWeatherRendering {
    @Inject(method = "addParticlesAndSound", at = @At("HEAD"), cancellable = true)
    private void addParticlesAndSoundHook(ClientWorld world, Camera camera, int ticks, ParticlesMode particlesMode, CallbackInfo ci) {
        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.noWeather.getValue()) {
            ci.cancel();
        }
    }
}
