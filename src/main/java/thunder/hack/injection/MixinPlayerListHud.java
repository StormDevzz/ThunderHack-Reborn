package thunder.hack.injection;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Nullables;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.misc.ExtraTab;

import java.util.Comparator;
import java.util.List;

import static thunder.hack.features.modules.Module.mc;

@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {
    private static final Comparator<Object> ENTRY_ORDERING = Comparator.comparingInt((entry) -> ((PlayerListEntry) entry).getGameMode() == GameMode.SPECTATOR ? 1 : 0)
            .thenComparing((entry) -> Nullables.mapOrElse(((PlayerListEntry) entry).getScoreboardTeam(), Team::getName, ""))
            .thenComparing((entry) -> ((PlayerListEntry) entry).getProfile().name(), String::compareToIgnoreCase);

    @Inject(method = "collectPlayerEntries", at = @At("HEAD"), cancellable = true)
    private void collectPlayerEntriesHook(CallbackInfoReturnable<List<PlayerListEntry>> cir) {
        if (ClientSettings.futureCompatibility.getValue())
            return;

        if (ThunderHack.isFuturePresent())
            return;

        if (ModuleManager.extraTab.isEnabled())
            cir.setReturnValue(mc.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).limit(ModuleManager.extraTab.size.getValue()).toList());
        else
            cir.setReturnValue(mc.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).limit(80).toList());
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    private void getPlayerNameHook(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (ModuleManager.extraTab.isEnabled()) {
            cir.setReturnValue(ExtraTab.getPlayerName(entry));
        }
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 20))
    private int changeMaxPlayersPerColumn(int original) {
        return ModuleManager.extraTab.isEnabled() ? ModuleManager.extraTab.rows.getValue() : original;
    }
}