package thunder.hack.features.hud.impl;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import thunder.hack.features.cmd.impl.StaffCommand;
import thunder.hack.features.hud.HudElement;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StaffBoard extends HudElement {
    private static final Pattern validUserPattern = Pattern.compile("^\\w{3,16}$");
    private List<String> players = new ArrayList<>();
    private List<String> notSpec = new ArrayList<>();
    private Map<String, Identifier> skinMap = new HashMap<>();

    private float vAnimation, hAnimation;

    public StaffBoard() {
        super("StaffBoard", 50, 50);
    }

    public static List<String> getOnlinePlayer() {
        return mc.player.networkHandler.getPlayerList().stream()
                .map(PlayerListEntry::getProfile)
                .map(GameProfile::name)
                .filter(profileName -> validUserPattern.matcher(profileName).matches())
                .collect(Collectors.toList());
    }

    public static List<String> getOnlinePlayerD() {
        List<String> S = new ArrayList<>();
        for (PlayerListEntry player : mc.player.networkHandler.getPlayerList()) {
            if (mc.isInSingleplayer() || player.getScoreboardTeam() == null) break;
            String prefix = player.getScoreboardTeam().getPrefix().getString();
            if (check(Formatting.strip(prefix).toLowerCase())
                    || StaffCommand.staffNames.toString().toLowerCase().contains(player.getProfile().name().toLowerCase())
                    || player.getProfile().name().toLowerCase().contains("1danil_mansoru1")
                    || player.getProfile().name().toLowerCase().contains("barslan_")
                    || player.getProfile().name().toLowerCase().contains("timmings")
                    || player.getProfile().name().toLowerCase().contains("timings")
                    || player.getProfile().name().toLowerCase().contains("ruthless")
                    || player.getScoreboardTeam().getPrefix().getString().contains("YT")
                    || (player.getScoreboardTeam().getPrefix().getString().contains("Y") && player.getScoreboardTeam().getPrefix().getString().contains("T"))) {
                String name = Arrays.asList(player.getScoreboardTeam().getPlayerList().toArray()).toString().replace("[", "").replace("]", "");

                if (player.getGameMode() == GameMode.SPECTATOR) {
                    S.add(player.getScoreboardTeam().getPrefix().getString() + name + ":gm3");
                    continue;
                }
                S.add(player.getScoreboardTeam().getPrefix().getString() + name + ":active");
            }
        }
        return S;
    }

    public List<String> getVanish() {
        List<String> list = new ArrayList<>();
        for (Team s : mc.world.getScoreboard().getTeams()) {
            if (s.getPrefix().getString().isEmpty() || mc.isInSingleplayer()) continue;
            String name = Arrays.asList(s.getPlayerList().toArray()).toString().replace("[", "").replace("]", "");

            if (getOnlinePlayer().contains(name) || name.isEmpty())
                continue;
            if (StaffCommand.staffNames.toString().toLowerCase().contains(name.toLowerCase())
                    && check(s.getPrefix().getString().toLowerCase())
                    || check(s.getPrefix().getString().toLowerCase())
                    || name.toLowerCase().contains("1danil_mansoru1")
                    || name.toLowerCase().contains("barslan_")
                    || name.toLowerCase().contains("timmings")
                    || name.toLowerCase().contains("timings")
                    || name.toLowerCase().contains("ruthless")
                    || s.getPrefix().getString().contains("YT")
                    || (s.getPrefix().getString().contains("Y") && s.getPrefix().getString().contains("T"))
            )
                list.add(s.getPrefix().getString() + name + ":vanish");
        }
        return list;
    }

    public static boolean check(String name) {
        if (mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().address.contains("mcfunny")) {
            return name.contains("helper") || name.contains("moder") || name.contains("модер") || name.contains("хелпер");
        }
        return name.contains("helper") || name.contains("moder") || name.contains("admin") || name.contains("owner") || name.contains("curator") || name.contains("куратор") || name.contains("модер") || name.contains("админ") || name.contains("хелпер") || name.contains("поддержка") || name.contains("сотрудник") || name.contains("зам") || name.contains("стажёр");
    }

    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}

    @Override
    public void onUpdate() {
        if (mc.player != null && mc.player.age % 10 == 0) {
            players = getVanish();
            notSpec = getOnlinePlayerD();
            players.sort(String::compareTo);
            notSpec.sort(String::compareTo);
        }
    }

    private Identifier getTexture(String n) {
        Identifier id = skinMap.get(n);
        if (id == null) {
            id = Identifier.of("minecraft:textures/mob/no_bg.png");
        }
        return id;
    }
}
