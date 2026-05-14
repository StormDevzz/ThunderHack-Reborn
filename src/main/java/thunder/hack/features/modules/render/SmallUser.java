package thunder.hack.features.modules.render;

import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class SmallUser extends Module {
    public SmallUser() {
        super("SmallUser", Category.RENDER);
    }

    private final Setting<Boolean> everyone = new Setting<>("Everyone", true);
    private final Setting<Boolean> self = new Setting<>("Self", false);

    public boolean shouldMakeSmall(PlayerEntity player) {
        if (player == null) return false;
        if (player == mc.player && !self.getValue()) return false;
        return everyone.getValue();
    }
}