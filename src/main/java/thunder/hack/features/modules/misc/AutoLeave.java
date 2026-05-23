package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventTick;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;

import java.util.List;

public final class AutoLeave extends Module {

    // ─────────────── Общие ───────────────
    private final Setting<String>  leaveReason    = new Setting<>("Reason", "AutoLeave");
    private final Setting<Boolean> warnInChat     = new Setting<>("Warn in Chat", true);
    private final Setting<Integer> warnBeforeSec  = new Setting<>("Warn Before (s)", 5, 1, 30,
            v -> warnInChat.getValue());
    private final Setting<Boolean> disableOnLeave = new Setting<>("Disable on Leave", true);

    // ─────────────── AFK ─────────────────
    private final Setting<SettingGroup> afkGroup      = new Setting<>("AFK", new SettingGroup(false, 0));
    private final Setting<Boolean>  afkEnabled        = new Setting<>("AFK Enable",            false).addToGroup(afkGroup);
    private final Setting<Integer>  afkSeconds        = new Setting<>("AFK Seconds",           60, 5, 600,
            v -> afkEnabled.getValue()).addToGroup(afkGroup);
    private final Setting<Boolean>  afkCheckRotation  = new Setting<>("Check Rotation",        true,
            v -> afkEnabled.getValue()).addToGroup(afkGroup);
    private final Setting<Boolean>  afkCheckPosition  = new Setting<>("Check Position",        true,
            v -> afkEnabled.getValue()).addToGroup(afkGroup);
    private final Setting<Boolean>  afkIgnoreSneak    = new Setting<>("Ignore while Sneaking", false,
            v -> afkEnabled.getValue()).addToGroup(afkGroup);
    private final Setting<Boolean>  afkIgnoreElytra   = new Setting<>("Ignore while Flying",   false,
            v -> afkEnabled.getValue()).addToGroup(afkGroup);
    private final Setting<Boolean>  afkResetOnDamage  = new Setting<>("Reset on Damage",       true,
            v -> afkEnabled.getValue()).addToGroup(afkGroup);

    // ─────────────── Health ───────────────
    private final Setting<SettingGroup> healthGroup  = new Setting<>("Health", new SettingGroup(false, 0));
    private final Setting<Boolean> hpEnabled         = new Setting<>("Health Enable",          false).addToGroup(healthGroup);
    private final Setting<Float>   hpThreshold       = new Setting<>("HP Threshold",           4f, 0.5f, 20f,
            v -> hpEnabled.getValue()).addToGroup(healthGroup);
    private final Setting<Boolean> hpIncludeAbsorb   = new Setting<>("Include Absorption",     true,
            v -> hpEnabled.getValue()).addToGroup(healthGroup);
    private final Setting<Boolean> hpOnlyOnDamage    = new Setting<>("Only on Damage",         false,
            v -> hpEnabled.getValue()).addToGroup(healthGroup);
    private final Setting<Boolean> hpOnFire          = new Setting<>("Also on Fire",           false,
            v -> hpEnabled.getValue()).addToGroup(healthGroup);
    private final Setting<Boolean> hpOnPoison        = new Setting<>("Also on Poison/Wither",  false,
            v -> hpEnabled.getValue()).addToGroup(healthGroup);

    // ─────────────── Timer ────────────────
    private final Setting<SettingGroup> timerGroup   = new Setting<>("Timer", new SettingGroup(false, 0));
    private final Setting<Boolean> timerEnabled      = new Setting<>("Timer Enable",           false).addToGroup(timerGroup);
    private final Setting<Integer> timerMinutes      = new Setting<>("Session Minutes",        30, 1, 480,
            v -> timerEnabled.getValue()).addToGroup(timerGroup);
    private final Setting<Boolean> timerShowInChat   = new Setting<>("Show in Chat",           true,
            v -> timerEnabled.getValue()).addToGroup(timerGroup);
    private final Setting<Integer> timerChatInterval = new Setting<>("Chat Interval (min)",    5, 1, 60,
            v -> timerEnabled.getValue() && timerShowInChat.getValue()).addToGroup(timerGroup);

    // ─────────────── Players ─────────────
    private final Setting<SettingGroup> playersGroup    = new Setting<>("Players", new SettingGroup(false, 0));
    private final Setting<Boolean> playersEnabled       = new Setting<>("Players Enable",          false).addToGroup(playersGroup);
    private final Setting<Float>   playersRange         = new Setting<>("Range",                   32f, 1f, 128f,
            v -> playersEnabled.getValue()).addToGroup(playersGroup);
    private final Setting<Integer> playersMinCount      = new Setting<>("Min Players Count",        1, 1, 20,
            v -> playersEnabled.getValue()).addToGroup(playersGroup);
    private final Setting<Boolean> playersIgnoreFriends = new Setting<>("Ignore Friends",           true,
            v -> playersEnabled.getValue()).addToGroup(playersGroup);
    private final Setting<Boolean> playersOnlyFriends   = new Setting<>("Only Friends",             false,
            v -> playersEnabled.getValue()).addToGroup(playersGroup);
    private final Setting<Boolean> playersCheckLoS      = new Setting<>("Check Line of Sight",      false,
            v -> playersEnabled.getValue()).addToGroup(playersGroup);
    private final Setting<Boolean> playersIgnoreTeam    = new Setting<>("Ignore Teammates",         false,
            v -> playersEnabled.getValue()).addToGroup(playersGroup);
    private final Setting<Integer> playersTriggerDelay  = new Setting<>("Trigger Delay (ticks)",    20, 0, 100,
            v -> playersEnabled.getValue()).addToGroup(playersGroup);

    // ─────────────── Crystals ────────────
    private final Setting<SettingGroup> crystalsGroup  = new Setting<>("Crystals", new SettingGroup(false, 0));
    private final Setting<Boolean> crystalsEnabled     = new Setting<>("Crystals Enable",       false).addToGroup(crystalsGroup);
    private final Setting<Float>   crystalsRange       = new Setting<>("Crystal Range",         10f, 1f, 64f,
            v -> crystalsEnabled.getValue()).addToGroup(crystalsGroup);
    private final Setting<Integer> crystalsMinCount    = new Setting<>("Min Crystal Count",     1, 1, 50,
            v -> crystalsEnabled.getValue()).addToGroup(crystalsGroup);
    private final Setting<Integer> crystalsTriggerDelay = new Setting<>("Trigger Delay (ticks)", 10, 0, 100,
            v -> crystalsEnabled.getValue()).addToGroup(crystalsGroup);

    // ─────────────── Totems ──────────────
    private final Setting<SettingGroup> totemsGroup   = new Setting<>("Totems", new SettingGroup(false, 0));
    private final Setting<Boolean> totemsEnabled      = new Setting<>("Totems Enable",         false).addToGroup(totemsGroup);
    private final Setting<Integer> totemsMinCount     = new Setting<>("Min Totem Count",       1, 0, 20,
            v -> totemsEnabled.getValue()).addToGroup(totemsGroup);
    private final Setting<Boolean> totemsCheckOffhand = new Setting<>("Check Offhand",         true,
            v -> totemsEnabled.getValue()).addToGroup(totemsGroup);
    private final Setting<Boolean> totemsCheckInventory = new Setting<>("Check Inventory",     true,
            v -> totemsEnabled.getValue()).addToGroup(totemsGroup);

    // ─────────────── Внутреннее ──────────
    private final Timer sessionTimer   = new Timer();
    private final Timer afkTimer       = new Timer();
    private final Timer hudTimer       = new Timer();

    private double  lastX, lastY, lastZ;
    private float   lastYaw, lastPitch, lastHealth;
    private int     playerTicks, crystalTicks;
    private boolean warnSent, triggered;

    public AutoLeave() {
        super("AutoLeave", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) return;
        triggered = false; warnSent = false;
        playerTicks = 0; crystalTicks = 0;
        sessionTimer.reset(); afkTimer.reset(); hudTimer.reset();

        lastX = mc.player.getX(); lastY = mc.player.getY(); lastZ = mc.player.getZ();
        lastYaw = mc.player.getYaw(); lastPitch = mc.player.getPitch();
        lastHealth = mc.player.getHealth();
    }

    @Override
    public void onDisable() {
        triggered = false; warnSent = false;
        playerTicks = 0; crystalTicks = 0;
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck() || triggered) return;

        if (afkEnabled.getValue())     checkAFK();
        if (hpEnabled.getValue())      checkHealth();
        if (timerEnabled.getValue())   checkTimer();
        if (playersEnabled.getValue()) checkPlayers();
        if (crystalsEnabled.getValue()) checkCrystals();
        if (totemsEnabled.getValue())  checkTotems();
    }

    // ────── AFK ──────
    private void checkAFK() {
        if (afkIgnoreSneak.getValue()  && mc.player.isSneaking())    { afkTimer.reset(); return; }
        if (afkIgnoreElytra.getValue() && mc.player.isGliding())  { afkTimer.reset(); return; }

        double cx = mc.player.getX(), cy = mc.player.getY(), cz = mc.player.getZ();
        float  cYaw = mc.player.getYaw(), cPitch = mc.player.getPitch();

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

        if (count < totemsMinCount.getValue()) leave("Low Totems (" + count + ")");
    }

    // ────── Helpers ──────
    private void tryWarn(long remainingMs) {
        if (!warnInChat.getValue() || warnSent) return;
        if (remainingMs <= warnBeforeSec.getValue() * 1000L) {
            sendMessage("[AutoLeave] Leaving in " + (remainingMs / 1000) + "s!");
            warnSent = true;
        }
    }

    private void leave(String trigger) {
        triggered = true;
        String reason = leaveReason.getValue().isBlank() ? trigger : leaveReason.getValue();
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().getConnection().disconnect(Text.literal(reason));
        }
        if (disableOnLeave.getValue()) disable(reason);
    }
}
