package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.*;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;

/**
 * PacketLimiter — ограничивает частоту отправки пакетов движения и действий.
 * Полезно для обхода античита по кол-ву пакетов или для имитации лагов.
 */
public final class PacketLimiter extends Module {

    // — Движение —
    private final Setting<SettingGroup> movementGroup = new Setting<>("Movement", new SettingGroup(true, 0));
    private final Setting<Boolean> limitPosition = new Setting<>("Limit Position", true).addToGroup(movementGroup);
    private final Setting<Integer> positionRate  = new Setting<>("Position Rate (ms)", 50, 10, 500,
            v -> limitPosition.getValue()).addToGroup(movementGroup);

    private final Setting<Boolean> limitLook     = new Setting<>("Limit Look", false).addToGroup(movementGroup);
    private final Setting<Integer> lookRate      = new Setting<>("Look Rate (ms)", 50, 10, 500,
            v -> limitLook.getValue()).addToGroup(movementGroup);

    // — Действия —
    private final Setting<SettingGroup> actionsGroup = new Setting<>("Actions", new SettingGroup(false, 0));
    private final Setting<Boolean> limitSwing    = new Setting<>("Limit Swing", false).addToGroup(actionsGroup);
    private final Setting<Integer> swingRate     = new Setting<>("Swing Rate (ms)", 100, 10, 1000,
            v -> limitSwing.getValue()).addToGroup(actionsGroup);

    private final Setting<Boolean> limitInteract = new Setting<>("Limit Interact", false).addToGroup(actionsGroup);
    private final Setting<Integer> interactRate  = new Setting<>("Interact Rate (ms)", 100, 10, 1000,
            v -> limitInteract.getValue()).addToGroup(actionsGroup);

    private final Timer positionTimer = new Timer();
    private final Timer lookTimer     = new Timer();
    private final Timer swingTimer    = new Timer();
    private final Timer interactTimer = new Timer();

    public PacketLimiter() {
        super("PacketLimiter", Category.MISC);
    }

    @Override
    public void onEnable() {
        positionTimer.reset();
        lookTimer.reset();
        swingTimer.reset();
        interactTimer.reset();
    }

    @EventHandler
    private void onPacketSend(PacketEvent.@NotNull Send e) {
        if (limitPosition.getValue() && e.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
            if (!positionTimer.passedMs(positionRate.getValue())) {
                e.cancel();
            } else {
                positionTimer.reset();
            }
        }

        if (limitLook.getValue() && e.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround) {
            if (!lookTimer.passedMs(lookRate.getValue())) {
                e.cancel();
            } else {
                lookTimer.reset();
            }
        }

        if (limitSwing.getValue() && e.getPacket() instanceof HandSwingC2SPacket) {
            if (!swingTimer.passedMs(swingRate.getValue())) {
                e.cancel();
            } else {
                swingTimer.reset();
            }
        }

        if (limitInteract.getValue() && e.getPacket() instanceof PlayerInteractBlockC2SPacket) {
            if (!interactTimer.passedMs(interactRate.getValue())) {
                e.cancel();
            } else {
                interactTimer.reset();
            }
        }
    }
}
