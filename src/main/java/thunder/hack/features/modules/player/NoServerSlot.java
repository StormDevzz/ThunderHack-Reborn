package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;

public class NoServerSlot extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Default);

    public NoServerSlot() {
        super("NoServerSlot", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof UpdateSelectedSlotS2CPacket) {
            switch (mode.getValue()) {
                case Default -> {
                    event.cancel();
                    sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().getSelectedSlot()));
                }
                case Alternative -> {
                    event.cancel();
                }
            }
        }
    }

    public enum Mode {
        Default, Alternative
    }
}
