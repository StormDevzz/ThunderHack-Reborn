package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.*;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;

public final class PacketCanceler extends Module {

    // — Группа: Движение (C2S) —
    private final Setting<SettingGroup> movementGroup = new Setting<>("Movement (C2S)", new SettingGroup(false, 0));
    private final Setting<Boolean> cancelPosition     = new Setting<>("Position",          false).addToGroup(movementGroup);
    private final Setting<Boolean> cancelLook         = new Setting<>("Look",              false).addToGroup(movementGroup);
    private final Setting<Boolean> cancelOnGround     = new Setting<>("OnGround",          false).addToGroup(movementGroup);
    private final Setting<Boolean> cancelSwing        = new Setting<>("Hand Swing",        false).addToGroup(movementGroup);
    private final Setting<Boolean> cancelSprint       = new Setting<>("Sprint",            false).addToGroup(movementGroup);

    // — Группа: Инвентарь (C2S) —
    private final Setting<SettingGroup> inventoryGroup = new Setting<>("Inventory (C2S)", new SettingGroup(false, 0));
    private final Setting<Boolean> cancelClickSlot    = new Setting<>("Click Slot",       false).addToGroup(inventoryGroup);
    private final Setting<Boolean> cancelDropItem     = new Setting<>("Drop Item",        false).addToGroup(inventoryGroup);
    private final Setting<Boolean> cancelCloseScreen  = new Setting<>("Close Screen",     false).addToGroup(inventoryGroup);

    // — Группа: Взаимодействие (C2S) —
    private final Setting<SettingGroup> interactGroup = new Setting<>("Interaction (C2S)", new SettingGroup(false, 0));
    private final Setting<Boolean> cancelInteract     = new Setting<>("Block Interact",   false).addToGroup(interactGroup);
    private final Setting<Boolean> cancelAttack       = new Setting<>("Attack Entity",    false).addToGroup(interactGroup);
    private final Setting<Boolean> cancelChat         = new Setting<>("Chat",             false).addToGroup(interactGroup);

    // — Группа: Сервер→Клиент (S2C) —
    private final Setting<SettingGroup> serverGroup = new Setting<>("Server (S2C)", new SettingGroup(false, 0));
    private final Setting<Boolean> cancelExplosion    = new Setting<>("Explosion",        false).addToGroup(serverGroup);
    private final Setting<Boolean> cancelEntityVelocity = new Setting<>("Entity Velocity", false).addToGroup(serverGroup);
    private final Setting<Boolean> cancelTimeUpdate   = new Setting<>("Time Update",      false).addToGroup(serverGroup);
    private final Setting<Boolean> cancelGameMessage  = new Setting<>("Chat Messages",    false).addToGroup(serverGroup);

    public PacketCanceler() {
        super("PacketCanceler", Category.MISC);
    }

    @EventHandler
    private void onPacketSend(PacketEvent.@NotNull Send e) {
        if (cancelPosition.getValue()    && e.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround) e.cancel();
        if (cancelLook.getValue()        && e.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround)     e.cancel();
        if (cancelOnGround.getValue()    && e.getPacket() instanceof PlayerMoveC2SPacket.OnGroundOnly)        e.cancel();
        if (cancelSwing.getValue()       && e.getPacket() instanceof HandSwingC2SPacket)                      e.cancel();
        if (cancelSprint.getValue()      && e.getPacket() instanceof ClientCommandC2SPacket p
                && (p.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING
                 || p.getMode() == ClientCommandC2SPacket.Mode.STOP_SPRINTING))                               e.cancel();
        if (cancelClickSlot.getValue()   && e.getPacket() instanceof ClickSlotC2SPacket)                      e.cancel();
        if (cancelDropItem.getValue()    && (e.getPacket() instanceof net.minecraft.network.packet.c2s.play.PickItemFromBlockC2SPacket || e.getPacket() instanceof net.minecraft.network.packet.c2s.play.PickItemFromEntityC2SPacket))               e.cancel();
        if (cancelCloseScreen.getValue() && e.getPacket() instanceof CloseHandledScreenC2SPacket)             e.cancel();
        if (cancelInteract.getValue()    && e.getPacket() instanceof PlayerInteractBlockC2SPacket)            e.cancel();
        if (cancelAttack.getValue()      && e.getPacket() instanceof PlayerInteractEntityC2SPacket)           e.cancel();
        if (cancelChat.getValue()        && e.getPacket() instanceof ChatMessageC2SPacket)                    e.cancel();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.@NotNull Receive e) {
        if (cancelExplosion.getValue()       && e.getPacket() instanceof ExplosionS2CPacket)       e.cancel();
        if (cancelEntityVelocity.getValue()  && e.getPacket() instanceof EntityVelocityUpdateS2CPacket) e.cancel();
        if (cancelTimeUpdate.getValue()      && e.getPacket() instanceof WorldTimeUpdateS2CPacket)  e.cancel();
        if (cancelGameMessage.getValue()     && e.getPacket() instanceof GameMessageS2CPacket)      e.cancel();
    }
}