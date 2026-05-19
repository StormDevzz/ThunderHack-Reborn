package thunder.hack.injection;

import net.minecraft.client.render.DimensionEffects;
import thunder.hack.core.manager.client.ModuleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionEffects.class)
public class MixinDimensionEffects {

    @Inject(method = "shouldBrightenLighting", at = @At("HEAD"), cancellable = true)
    private void shouldBrightenLightingHook(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.fullbright.isEnabled()) {
            cir.setReturnValue(true);
        }
    }
}
