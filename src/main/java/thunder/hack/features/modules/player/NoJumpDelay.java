package thunder.hack.features.modules.player;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class NoJumpDelay extends Module {
    public NoJumpDelay() {
        super("NoJumpDelay", Category.PLAYER);
    }

    private final Setting<Integer> delay = new Setting<>("Delay", 1, 0, 4);

    @Override
    public void onUpdate() {
        // jumpingCooldown was removed in 1.21.11 - functionality disabled
    }
}
