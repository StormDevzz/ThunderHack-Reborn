// RaveX Team — code interaction (https://github.com/StormDevzz/RaveX)
package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityPosition;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;

public class NoServerRotate extends Module {
    public NoServerRotate() {
        super("NoServerRotate", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck()) return;
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket pac) {
            EntityPosition newChange = new EntityPosition(
                pac.change().position(), pac.change().deltaMovement(),
                mc.player.getYaw(), mc.player.getPitch()
            );
            PlayerPositionLookS2CPacket newPac = PlayerPositionLookS2CPacket.of(pac.teleportId(), newChange, pac.relatives());
            mc.player.networkHandler.onPlayerPositionLook(newPac);
            e.cancel();
        }
    }
}