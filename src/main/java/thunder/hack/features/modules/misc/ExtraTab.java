package thunder.hack.features.modules.misc;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class ExtraTab extends Module {
    public ExtraTab() {
        super("ExtraTab", Category.MISC);
    }

    public final Setting<Integer> size = new Setting<>("Size", 300, 80, 1000);
}