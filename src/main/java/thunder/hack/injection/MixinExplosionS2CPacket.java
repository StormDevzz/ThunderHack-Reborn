package thunder.hack.injection;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.injection.accesors.IExplosionS2CPacket;

import java.util.Optional;

@Mixin(ExplosionS2CPacket.class)
public abstract class MixinExplosionS2CPacket implements IExplosionS2CPacket {
    @Shadow
    public abstract Optional<Vec3d> playerKnockback();

    @Unique
    private Float customX = null;
    @Unique
    private Float customY = null;
    @Unique
    private Float customZ = null;

    @Override
    public void setMotionX(float x) {
        this.customX = x;
    }

    @Override
    public void setMotionY(float y) {
        this.customY = y;
    }

    @Override
    public void setMotionZ(float z) {
        this.customZ = z;
    }

    @Override
    public float getMotionX() {
        return customX != null ? customX : playerKnockback().map(v -> (float) v.x).orElse(0.0f);
    }

    @Override
    public float getMotionY() {
        return customY != null ? customY : playerKnockback().map(v -> (float) v.y).orElse(0.0f);
    }

    @Override
    public float getMotionZ() {
        return customZ != null ? customZ : playerKnockback().map(v -> (float) v.z).orElse(0.0f);
    }

    @Inject(method = "playerKnockback", at = @At("HEAD"), cancellable = true)
    private void playerKnockbackHook(CallbackInfoReturnable<Optional<Vec3d>> cir) {
        if (customX != null || customY != null || customZ != null) {
            double x = customX != null ? customX : playerKnockback().map(v -> v.x).orElse(0.0);
            double y = customY != null ? customY : playerKnockback().map(v -> v.y).orElse(0.0);
            double z = customZ != null ? customZ : playerKnockback().map(v -> v.z).orElse(0.0);
            cir.setReturnValue(Optional.of(new Vec3d(x, y, z)));
        }
    }
}
