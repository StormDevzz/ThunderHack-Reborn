package thunder.hack.features.hud.impl;
import net.minecraft.client.gl.RenderPipelines;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;

public class Crosshair extends Module {
    public Crosshair() {
        super("Crosshair", Category.HUD);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Circle);
    private final Setting<Boolean> animated = new Setting<>("Animated", true, v -> mode.is(Mode.Default));
    private final Setting<Boolean> dot = new Setting<>("Dot", false, v -> mode.is(Mode.Default));
    private final Setting<Boolean> t = new Setting<>("T", false, v -> mode.is(Mode.Default));
    private final Setting<ColorMode> colorMode = new Setting<>("ColorMode", ColorMode.Sync);
    private final Setting<ColorSetting> crossColor = new Setting<>("CrossColor", new ColorSetting(new Color(80, 180, 180)));
    private final Setting<Boolean> dynamic = new Setting<>("Dynamic", true);
    private final Setting<Float> range = new Setting<>("Range", 30.0f, 0.1f, 120f);
    private final Setting<Float> speed = new Setting<>("Speed", 3.0f, 0.1f, 20f);
    private final Setting<Float> backSpeed = new Setting<>("BackSpeed", 5.0f, 0.1f, 20f);

    private enum ColorMode {
        Custom, Sync
    }

    private enum Mode {
        Circle, WiseTree, Dot, Default
    }

    private float xAnim, yAnim, prevPitch, prevProgress;

    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}

    public float getAnimatedPosX() {
        if (xAnim == 0)
            return mc.getWindow().getScaledWidth() / 2f;
        return xAnim;
    }

    public float getAnimatedPosY() {
        if (yAnim == 0)
            return mc.getWindow().getScaledHeight() / 2f;
        return yAnim;
    }
}