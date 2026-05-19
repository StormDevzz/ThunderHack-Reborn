package thunder.hack.injection;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventPlayerJump;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(method = "jump", at = @At("HEAD"))
    private void onJumpPre(CallbackInfo ci) {
        ThunderHack.EVENT_BUS.post(new EventPlayerJump(true));
    }

    @Inject(method = "jump", at = @At("RETURN"))
    private void onJumpPost(CallbackInfo ci) {
        ThunderHack.EVENT_BUS.post(new EventPlayerJump(false));
    }
}
