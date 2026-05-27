package thunder.hack.features.modules.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtility;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class AutoLeave extends Module {
    public AutoLeave() {
        super("AutoLeave", Category.MISC);
    }

    private final Setting<Boolean> antiHelperLeave = new Setting<>("AntiHelperLeave", true);
    private final Setting<Boolean> antiKTLeave = new Setting<>("AntiKTLeave", true);
    private final Setting<Boolean> autoDisable = new Setting<>("AutoDisable", true);
    private final Setting<Boolean> fastLeave = new Setting<>("InstantLeave", true);
    public static Setting<String> command = new Setting<>("Command", "hub");
    private final Setting<SettingGroup> leaveIf = new Setting<>("Leave if", new SettingGroup(false, 0));
    private final Setting<Boolean> low_hp = new Setting<>("LowHp", false).addToGroup(leaveIf);
    private final Setting<Boolean> totems = new Setting<>("Totems", false).addToGroup(leaveIf);
    private final Setting<Integer> totemsCount = new Setting<>("TotemsCount", 2, 0, 10, v -> totems.getValue());
    private final Setting<Float> leaveHp = new Setting<>("HP", 8.0f, 1f, 20.0f, v -> low_hp.getValue());
    private final Setting<LeaveMode> staff = new Setting<>("Staff", LeaveMode.None).addToGroup(leaveIf);
    private final Setting<LeaveMode> players = new Setting<>("Players", LeaveMode.Leave).addToGroup(leaveIf);
    private final Setting<Integer> distance = new Setting<>("Distance", 256, 4, 256, v -> players.getValue() != LeaveMode.None).addToGroup(leaveIf);

    private final Timer chatDelay = new Timer();

    // Будет хуева если мы ливнем в кт
    private final Timer hurtTimer = new Timer();

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null)
            return;

        if (mc.player.hurtTime > 0)
            hurtTimer.reset();

        if (antiKTLeave.getValue() && !hurtTimer.passedMs(30000))
            return;

        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl.getScoreboardTeam() != null && antiHelperLeave.getValue()) {
                String prefix = pl.getScoreboardTeam().getPrefix().getString();
                if (isStaff(Formatting.strip(prefix)))
                    continue;
            }


        boolean moved =
                (afkCheckPosition.getValue() && (cx != lastX || cy != lastY || cz != lastZ)) ||
                (afkCheckRotation.getValue() && (cYaw != lastYaw || cPitch != lastPitch));

        if (moved) {
            afkTimer.reset();
            lastX = cx; lastY = cy; lastZ = cz; lastYaw = cYaw; lastPitch = cPitch;
        }

        long remaining = afkSeconds.getValue() * 1000L - (System.currentTimeMillis() - afkTimer.getTimeMs());
        tryWarn(remaining);
        if (remaining <= 0) leave("AFK");
    }

    // ────── Health ──────
    private void checkHealth() {
        float hp = mc.player.getHealth();
        float total = hpIncludeAbsorb.getValue() ? hp + mc.player.getAbsorptionAmount() : hp;

        if (hpOnlyOnDamage.getValue() && total >= lastHealth) { lastHealth = total; return; }
        lastHealth = total;

        boolean trigger = total <= hpThreshold.getValue();
        if (hpOnFire.getValue()   && mc.player.isOnFire())                                     trigger = true;
        if (hpOnPoison.getValue() && (mc.player.hasStatusEffect(StatusEffects.POISON)
                                   || mc.player.hasStatusEffect(StatusEffects.WITHER)))        trigger = true;

        if (trigger) leave("Low HP");
    }

    // ────── Timer ──────
    private void checkTimer() {
        long totalMs   = timerMinutes.getValue() * 60_000L;
        long remaining = totalMs - (System.currentTimeMillis() - sessionTimer.getTimeMs());

        if (timerShowInChat.getValue() && hudTimer.passedMs(timerChatInterval.getValue() * 60_000L)) {
            sendMessage(String.format("[AutoLeave] Time left: %d:%02d", remaining / 60_000, (remaining % 60_000) / 1000));
            hudTimer.reset();
        }

        tryWarn(remaining);
        if (remaining <= 0) leave("Timer");
    }

    // ────── Players ──────
    private void checkPlayers() {
        float range = playersRange.getValue();
        int count = 0;

        for (AbstractClientPlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            if (p.distanceTo(mc.player) > range) continue;
            String name = p.getName().getString();
            if (playersIgnoreFriends.getValue() && Managers.FRIEND.isFriend(name)) continue;
            if (playersOnlyFriends.getValue()   && !Managers.FRIEND.isFriend(name)) continue;
            if (playersIgnoreTeam.getValue() && mc.player.getScoreboardTeam() != null
                    && mc.player.getScoreboardTeam().equals(p.getScoreboardTeam())) continue;
            if (playersCheckLoS.getValue() && !mc.player.canSee(p)) continue;
            count++;
        }

        if (count >= playersMinCount.getValue()) {
            if (++playerTicks >= playersTriggerDelay.getValue()) leave("Player Nearby");
        } else {
            playerTicks = 0;
        }
    }

    // ────── Crystals ──────
    private void checkCrystals() {
        float range = crystalsRange.getValue();
        long count = mc.world.getEntitiesByType(
                EntityType.END_CRYSTAL,
                mc.player.getBoundingBox().expand(range),
                e -> e.distanceTo(mc.player) <= range
        ).size();

        if (count >= crystalsMinCount.getValue()) {
            if (++crystalTicks >= crystalsTriggerDelay.getValue()) leave("Crystals Nearby (" + count + ")");
        } else {
            crystalTicks = 0;
        }
    }

    // ────── Totems ──────
    private void checkTotems() {
        int count = 0;

        if (totemsCheckOffhand.getValue()
                && mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            count++;
        }

        if (totemsCheckInventory.getValue()) {
            for (var stack : mc.player.getInventory().getMainStacks()) {
                if (stack.getItem() == Items.TOTEM_OF_UNDYING) count++;
            }
        }

        if (totems.getValue() && InventoryUtility.getItemCount(Items.TOTEM_OF_UNDYING) <= totemsCount.getValue())
            leave(isRu() ? "Ливнул т.к. кончились тотемы" : "Logged out because out of totems");

        if (mc.player.getHealth() < leaveHp.getValue() && low_hp.getValue())
            leave(isRu() ? "Ливнул т.к. мало хп" : "Logged out because ur hp is low");

        if (staff.getValue() != LeaveMode.None && ModuleManager.staffBoard.isDisabled() && mc.player.age % 5 == 0)
            sendMessage(isRu() ? "Включи StaffBoard!" : "Turn on StaffBoard!");
    }

    private void leave(String message) {
        if (!chatDelay.passedMs(1000))
            return;
        chatDelay.reset();

        if (autoDisable.getValue())
            disable(message);

        if (fastLeave.getValue()) sendPacket(new UpdateSelectedSlotC2SPacket(228));
        else mc.player.networkHandler.getConnection().disconnect(Text.of("[AutoLeave] " + message));
    }

    /*public void onStaff() { todo
        if (!chatDelay.passedMs(1000))
            return;
        chatDelay.reset();

        if (hurtTimer.passedMs(30000) || !antiKTLeave.getValue()) {
            switch (staff.getValue()) {
                case Command -> {
                    sendMessage(isRu() ? "Ливнул т.к. хелпер в спеке!" : "Logged out because helper in vanish!");
                    mc.player.networkHandler.sendChatCommand(command.getValue());
                    if (autoDisable.getValue())
                        disable();
                }
                case Leave -> leave(isRu() ? "Ливнул т.к. хелпер в спеке!" : "Logged out because helper in vanish!");
            }
        }
    }*/

    public static boolean isStaff(String name) {
        if (name == null) return false;

        name = name.toLowerCase();

        return name.contains("helper") || name.contains("moder") || name.contains("admin") || name.contains("owner") || name.contains("curator") || name.contains("куратор") || name.contains("модер") || name.contains("админ") || name.contains("хелпер") || name.contains("поддержка");
    }

    private enum LeaveMode {
        None, Command, Leave
    }
}
