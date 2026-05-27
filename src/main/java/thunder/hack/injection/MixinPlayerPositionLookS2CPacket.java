package thunder.hack.injection;

import net.minecraft.entity.EntityPosition;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.accessors.IPlayerPositionLookS2CPacket;

@Mixin(PlayerPositionLookS2CPacket.class)
public abstract class MixinPlayerPositionLookS2CPacket implements IPlayerPositionLookS2CPacket {
    @Shadow
    public abstract EntityPosition change();

    @Unique
    private Float customYaw = null;

    @Unique
    private Float customPitch = null;

    @Override
    public void setYaw(float yaw) {
        this.customYaw = yaw;
    }

    @Override
    public void setPitch(float pitch) {
        this.customPitch = pitch;
    }

    @Inject(method = "change", at = @At("HEAD"), cancellable = true)
    private void changeHook(CallbackInfoReturnable<EntityPosition> cir) {
        if (customYaw != null || customPitch != null) {
            EntityPosition original = change();
            float y = customYaw != null ? customYaw : original.yaw();
            float p = customPitch != null ? customPitch : original.pitch();
            cir.setReturnValue(new EntityPosition(original.position(), original.deltaMovement(), y, p));
        }
    }
}
