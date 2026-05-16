package thunder.hack.injection;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.features.modules.misc.Weather;

@Mixin(ClientWorld.class)
public abstract class MixinCustomWeather {

    @Shadow
    private float rainGradient;
    
    @Shadow
    private float prevRainGradient;
    
    @Shadow
    private float thunderGradient;
    
    @Shadow
    private float prevThunderGradient;
    
    // Перехватываем получение уровня дождя
    @Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true)
    private void onGetRainGradient(float delta, CallbackInfoReturnable<Float> cir) {
        Weather weather = (Weather) ThunderHack.moduleManager.getModule(Weather.class);
        
        if (weather != null && weather.isEnabled()) {
            switch (weather.mode.getValue()) {
                case Clear:
                    cir.setReturnValue(0.0f);
                    break;
                case Rain:
                case Thunder:
                case Snow:
                    cir.setReturnValue(1.0f);
                    break;
            }
        }
    }
    
    // Перехватываем получение уровня грозы
    @Inject(method = "getThunderGradient", at = @At("HEAD"), cancellable = true)
    private void onGetThunderGradient(float delta, CallbackInfoReturnable<Float> cir) {
        Weather weather = (Weather) ThunderHack.moduleManager.getModule(Weather.class);
        
        if (weather != null && weather.isEnabled()) {
            switch (weather.mode.getValue()) {
                case Clear:
                case Rain:
                case Snow:
                    cir.setReturnValue(0.0f);
                    break;
                case Thunder:
                    cir.setReturnValue(1.0f);
                    break;
            }
        }
    }
}