package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

import static thunder.hack.features.modules.combat.Criticals.getInteractType;
import static thunder.hack.features.modules.combat.Criticals.InteractType;

public final class MaceSwap extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Normal);

    private boolean guard = false;

    public MaceSwap() {
        super("MaceSwap", Category.COMBAT);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.@NotNull Send event) {
        if (guard) return;
        if (!(event.getPacket() instanceof PlayerInteractEntityC2SPacket packet)) return;
        if (getInteractType(packet) != InteractType.ATTACK) return;
        if (mc.player == null) return;
        if (mc.player.getInventory().getMainHandStack().getItem() == Items.MACE) return;

        int maceSlot = findMaceSlot();
        if (maceSlot == -1 || maceSlot == mc.player.getInventory().getSelectedSlot()) return;

        if (mode.is(Mode.Normal)) {
            mc.player.getInventory().setSelectedSlot(maceSlot);
            sendPacket(new UpdateSelectedSlotC2SPacket(maceSlot));
        } else if (mode.is(Mode.Silent)) {
            event.cancel();
            guard = true;
            int prevSlot = mc.player.getInventory().getSelectedSlot();
            sendPacket(new UpdateSelectedSlotC2SPacket(maceSlot));
            sendPacket(packet);
            sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            guard = false;
        }
    }

    private int findMaceSlot() {
        SearchInvResult result = InventoryUtility.findItemInHotBar(Items.MACE);
        if (result.found()) return result.slot();
        return -1;
    }

    public enum Mode {
        Normal, Silent
    }
}
