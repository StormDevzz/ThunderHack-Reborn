package thunder.hack.features.modules.render;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class Ambience extends Module {
    public Ambience() {
        super("Ambience", Category.RENDER);
    }

    public final Setting<Integer> alpha = new Setting<>("Alpha", 50, 0, 255);
    public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(255, 128, 128, 50)));
    public final Setting<Boolean> gradient = new Setting<>("Gradient", true);
    public final Setting<ColorSetting> secondColor = new Setting<>("SecondColor", new ColorSetting(new Color(128, 128, 255, 50)), v -> gradient.getValue());

    public void onRender2D(DrawContext context) {
        if (mc.player == null || mc.world == null) return;

        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();
        int a = alpha.getValue();

        Color c1 = color.getValue().getColorObject();
        Color c2 = gradient.getValue() ? secondColor.getValue().getColorObject() : c1;

        Color topColor = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), a);
        Color bottomColor = new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), a);

        Render2DEngine.draw2DGradientRect(
            context.getMatrices(),
            0, 0, width, height,
            topColor, topColor,
            bottomColor, bottomColor
        );
    }
}