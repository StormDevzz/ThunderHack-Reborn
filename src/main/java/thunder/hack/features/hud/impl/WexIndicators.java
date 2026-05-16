package thunder.hack.features.hud.impl;

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
        super.onRender2D(context);

        List<Indicator> activeIndicators = new ArrayList<>();
        for (Indicator ind : indicators) {
            if (ind.enabled.getValue()) {
                activeIndicators.add(ind);
            }
        }

        int count = activeIndicators.size();
        if (count == 0) return;

        float totalWidth = count * rangeBetween.getValue() - 8;
        float startX = getPosX();
        float startY = getPosY();

        Render2DEngine.drawRect(
            context.getMatrices(),
            startX, startY,
            totalWidth, 44,
            rectColor.getValue().getColorObject()
        );

        for (int i = 0; i < count; i++) {
            Indicator ind = activeIndicators.get(i);
            float x = startX + i * rangeBetween.getValue();
            float y = startY;

            drawIndicator(context, x + 22, y + 22, ind);
        }

        setBounds(startX, startY, totalWidth, 44);
    }

    private void drawIndicator(DrawContext context, float centerX, float centerY, Indicator indicator) {
        float radius = 11f;

        // Тёмный фон-окружность
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bgBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; i += 2) {
            double rad = Math.toRadians(i);
            float x = (float) (Math.cos(rad) * radius);
            float y = (float) (Math.sin(rad) * radius);
            bgBuffer.vertex(centerX, centerY, 0).color(0.1f, 0.1f, 0.1f, 0.5f);
            bgBuffer.vertex(centerX + x, centerY + y, 0).color(0.1f, 0.1f, 0.1f, 0.5f);
        }
        BufferRenderer.drawWithGlobalProgram(bgBuffer.end());

        // Дуга прогресса
        double progress = indicator.getSmoothProgress();
        int startAngle = -90;
        int endAngle = (int) (startAngle + 360.0 * progress);

        BufferBuilder progressBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = startAngle; i <= endAngle; i += 2) {
            double rad = Math.toRadians(i);
            float x = (float) (Math.cos(rad) * radius);
            float y = (float) (Math.sin(rad) * radius);

            Color col = getProgressColor(i, progress);
            progressBuffer.vertex(centerX, centerY, 0).color(col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f, 1f);
            progressBuffer.vertex(centerX + x, centerY + y, 0).color(col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f, 1f);
        }
        BufferRenderer.drawWithGlobalProgram(progressBuffer.end());


        // Текст в центре
        String displayText;
        if (indicator.getName().equals("TPS")) {
            displayText = String.valueOf(Math.round(progress * 20.0));
        } else {
            displayText = Math.round(progress * 100) + "%";
        }

        FontRenderers.sf_bold_mini.drawCenteredString(
            context.getMatrices(),
            displayText,
            centerX,
            centerY + 1,
            new Color(200, 200, 200).getRGB()
        );
        FontRenderers.sf_bold_mini.drawCenteredString(
            context.getMatrices(),
            indicator.getName(),
            centerX,
            centerY - 14,
            new Color(200, 200, 200).getRGB()
        );
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