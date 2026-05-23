package thunder.hack.features.modules.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class TotemAnimation extends Module {
    public TotemAnimation() {
        super("TotemAnimation", Category.RENDER);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.FadeOut);
    private final Setting<Integer> speed = new Setting<>("Speed", 40, 1, 100);

    private ItemStack floatingItem = null;
    private int floatingItemTimeLeft;

    public void showFloatingItem(ItemStack floatingItem) {
        this.floatingItem = floatingItem;
        floatingItemTimeLeft = getTime();
    }

    @Override
    public void onUpdate() {
        if (floatingItemTimeLeft > 0) {
            --floatingItemTimeLeft;
            if (floatingItemTimeLeft == 0) {
                floatingItem = null;
            }
        }
    }

    public void renderFloatingItem(float tickDelta) {
        // stubbed for 1.21.9
    }

    private int getTime() {
        int invertedSpeed = 101 - speed.getValue();

        if (mode.is(Mode.FadeOut))
            return invertedSpeed / 4;

        if (mode.is(Mode.Insert))
            return invertedSpeed / 2;

        return invertedSpeed;
    }

    private enum Mode {
        FadeOut, Size, Otkisuli, Insert, Fall, Rocket, Roll, Off
    }
}
