package thunder.hack.features.modules.player;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class NoInteract extends Module {
    public NoInteract() {
        super("NoInteract", Category.PLAYER);
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.Blocks);
    public static Setting<Boolean> onlyAura = new Setting<>("OnlyAura", false, v -> mode.getValue() == Mode.Blocks);

    public enum Mode {
        Blocks, All
    }
}
