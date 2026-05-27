package thunder.hack.features.modules.misc;

import thunder.hack.features.modules.Module;

public class ExtraTab extends Module {
    public ExtraTab() {
        super("ExtraTab", Category.MISC);
    }

    public final Setting<Integer> size = new Setting<>("Size", 300, 80, 1000);
    public final Setting<Integer> rows = new Setting<>("Rows", 20, 1, 100);
    public final Setting<Boolean> friends = new Setting<>("Friends", true);
    public final Setting<ColorSetting> friendColor = new Setting<>("FriendColor", new ColorSetting(new Color(0x00FF00).getRGB()), v -> friends.getValue());
    public final Setting<Boolean> ping = new Setting<>("Ping", true);
    public final Setting<ColorSetting> pingColor = new Setting<>("PingColor", new ColorSetting(new Color(0xFFFF00).getRGB()), v -> ping.getValue());

    public static Text getPlayerName(PlayerListEntry entry) {
        String name = entry.getDisplayName() != null ? entry.getDisplayName().getString() : Team.decorateName(entry.getScoreboardTeam(), Text.literal(entry.getProfile().name())).getString();
        MutableText text = Text.literal(name);

        if (ModuleManager.extraTab.friends.getValue() && Managers.FRIEND.isFriend(name)) {
            text.setStyle(text.getStyle().withColor(ModuleManager.extraTab.friendColor.getValue().getColor()));
        }

        if (ModuleManager.extraTab.ping.getValue()) {
            text.append(Text.literal(" " + entry.getLatency() + "ms").setStyle(text.getStyle().withColor(ModuleManager.extraTab.pingColor.getValue().getColor())));
        }

        return text;
    }
}