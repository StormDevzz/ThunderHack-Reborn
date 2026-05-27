package thunder.hack.features.hud.impl;
import net.minecraft.client.gl.RenderPipelines;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WexIndicators extends HudElement {
    public WexIndicators() {
        super("WexIndicators", 200, 50);
    }

    private final Setting<Boolean> memory = new Setting<>("Memory", true);
    private final Setting<Boolean> timer = new Setting<>("Timer", true);
    private final Setting<Boolean> tps = new Setting<>("TPS", true);
    private final Setting<Boolean> speed = new Setting<>("Speed", true);

    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(0x8800FF00, true)));
    private final Setting<ColorSetting> rectColor = new Setting<>("RectColor", new ColorSetting(new Color(0x88000000, true)));
    private final Setting<Float> rangeBetween = new Setting<>("RangeBetween", 46.0f, 46.0f, 100.0f);
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Astolfo);

    private enum Mode {
        Static, StateBased, Astolfo
    }

    private final List<Indicator> indicators = new ArrayList<>();
    private boolean init;

    @Override
    public void onUpdate() {
        if (!init) {
            initIndicators();
            init = true;
        }
        indicators.forEach(Indicator::update);
    }

    private void initIndicators() {
        indicators.add(new Indicator(timer, "Timer", () -> {
            float val = ThunderHack.TICK_TIMER;
            float min = 0.1f;
            float max = 10f;
            return Math.max(0, Math.min(1, (val - min) / (max - min)));
        }));

        indicators.add(new Indicator(memory, "Memory", () -> {
            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            long max = Runtime.getRuntime().maxMemory();
            long used = total - free;
            return (double) used / (double) max;
        }));

        indicators.add(new Indicator(tps, "TPS", () -> {
            float currentTps = Managers.SERVER.getTPS();
            return currentTps / 20.0f;
        }));

        indicators.add(new Indicator(speed, "Speed", () -> {
            double currentSpeed = Managers.PLAYER.currentPlayerSpeed;
            return Math.min(1, currentSpeed / 1.5);
        }));
    }

    @Override
    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}

    private void drawIndicator(DrawContext context, float centerX, float centerY, Indicator indicator) {
        // stubbed for 1.21.9
    }

    private Color getProgressColor(int angle, double progress) {
        float stage = (float) (angle + 90) / 360.0f;
        switch (mode.getValue()) {
            case StateBased:
                int r = (int) (255f - 255f * progress);
                int g = (int) (255f * progress);
                int b = (int) (100f * progress);
                return new Color(r, g, b);
            case Astolfo:
                double hue = (System.currentTimeMillis() % 10000) / 10000.0 + stage;
                return Color.getHSBColor((float) (hue % 1.0), 0.8f, 1.0f);
            case Static:
            default:
                return color.getValue().getColorObject();
        }
    }

    private static class Indicator {
        private final Setting<Boolean> enabled;
        private final String name;
        private final ProgressSupplier progressSupplier;
        private float smoothProgress = 0f;

        interface ProgressSupplier {
            double get();
        }

        Indicator(Setting<Boolean> enabled, String name, ProgressSupplier supplier) {
            this.enabled = enabled;
            this.name = name;
            this.progressSupplier = supplier;
        }

        void update() {
            double target = Math.max(progressSupplier.get(), 0.0);
            smoothProgress = AnimationUtility.fast(smoothProgress, (float) target, 15f);
        }

        float getSmoothProgress() {
            return smoothProgress;
        }

        String getName() {
            return name;
        }
    }
}