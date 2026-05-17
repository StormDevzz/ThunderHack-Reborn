package thunder.hack.features.modules.client;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class Capes extends Module {
    public Capes() {
        super("Capes", Category.CLIENT);
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.starcape);

    public enum Mode {
        bkgroup, dev, fbgroup, starcape, tester
    }
}
