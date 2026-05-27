package thunder.hack.features.modules.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class NoBob extends Module {
    public NoBob() {
        super("NoBob", Category.RENDER);
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.Sexy);

    public void bobView(MatrixStack matrices, float tickDelta) {
        // stubbed for 1.21.9 (prevStrideDistance/strideDistance removed)
    }

    public enum Mode {
        Sexy,
        Off
    }
}