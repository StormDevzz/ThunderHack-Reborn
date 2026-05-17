package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;

public class PacketLogger extends Module {
    public PacketLogger() {
        super("PacketLogger", Category.MISC);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        sendMessage("RECV: " + event.getPacket().getClass().getSimpleName());
    }
}
