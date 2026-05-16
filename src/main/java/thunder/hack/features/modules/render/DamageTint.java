package thunder.hack.features.modules.render;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class DamageTint extends Module {
    public DamageTint() {
        super("DamageTint", Category.RENDER);
    }

    private final Setting<ColorSetting> color = new Setting<>("Tint", new ColorSetting(HudEditor.getColor(0)));
    private final Setting<Integer> maxAlpha = new Setting<>("MaxAlpha", 170, 0, 255);
    private final Setting<Float> healthThreshold = new Setting<>("HealthThreshold", 12f, 0f, 20f);

    public void onRender2D(DrawContext context) {
        if (mc.player == null) return;

        float hp = mc.player.getHealth();
        if (hp >= healthThreshold.getValue()) return;

        float factor = 1f - MathUtility.clamp(hp / healthThreshold.getValue(), 0f, 1f);
        int alpha = (int) (factor * maxAlpha.getValue());

        if (alpha <= 0) return;

        Color c = Render2DEngine.injectAlpha(color.getValue().getColorObject(), alpha);
        Render2DEngine.drawRect(context.getMatrices(), 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), c);
    }
}
