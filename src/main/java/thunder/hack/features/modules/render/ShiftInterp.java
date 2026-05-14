package thunder.hack.features.modules.render;

import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class ShiftInterp extends Module {
    public ShiftInterp() {
        super("ShiftInterp", Category.RENDER);
    }

    private final Setting<Boolean> everyone = new Setting<>("Everyone", true);
    private final Setting<Boolean> self = new Setting<>("Self", false);

    public boolean shouldShift(PlayerEntity player) {
        if (player == null) return false;
        if (player == mc.player) return self.getValue();
        return everyone.getValue();
    }
}