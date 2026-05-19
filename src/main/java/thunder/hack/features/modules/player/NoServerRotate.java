package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.accessors.IPlayerPositionLookS2CPacket;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class NoServerRotate extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Default);

    public NoServerRotate() {
        super("NoServerRotate", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck()) return;
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket pac) {
            switch (mode.getValue()) {
                case Default -> {
                    ((IPlayerPositionLookS2CPacket) (Object) pac).setYaw(mc.player.getYaw());
                    ((IPlayerPositionLookS2CPacket) (Object) pac).setPitch(mc.player.getPitch());
                }
                case Alternative -> {
                    ((IPlayerPositionLookS2CPacket) (Object) pac).setPitch(mc.player.getPitch());
                }
            }
        }
    }

    public enum Mode {
        Default, Alternative
    }
}
