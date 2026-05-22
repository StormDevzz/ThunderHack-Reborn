package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class IndicatorsPlus extends HudElement {
    public IndicatorsPlus() {
        super("Indicators+", 200, 50);
    }

    private final Setting<Boolean> memory = new Setting<>("Memory", true);
    private final Setting<Boolean> timer = new Setting<>("Timer", true);
    private final Setting<Boolean> tps = new Setting<>("TPS", true);
    private final Setting<Boolean> speed = new Setting<>("Speed", true);

    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(0x8800FF00, true)));
    private final Setting<ColorSetting> rectColor = new Setting<>("RectColor", new ColorSetting(new Color(0x88000000, true)));
    private final Setting<Float> rangeBetween = new Setting<>("RangeBetween", 46.0f, 46.0f, 100.0f);
    private final Setting<ColorMode> colorMode = new Setting<>("Mode", ColorMode.Astolfo);

    private enum ColorMode {
        Static, StateBased, Astolfo
    }

    private final List<PlusIndicator> indicators = new ArrayList<>();
    private boolean init;

    @Override
    public void onUpdate() {
        if (!init) {
            initIndicators();
            init = true;
        }
        indicators.forEach(PlusIndicator::update);
    }

    private void initIndicators() {
        indicators.add(new PlusIndicator(timer, "Timer", () -> {
            float val = ThunderHack.TICK_TIMER;
            return (double) Math.max(0, Math.min(1, (val - 0.1f) / (10f - 0.1f)));
        }, () -> Math.round(ThunderHack.TICK_TIMER * 100) + "%"));

        indicators.add(new PlusIndicator(memory, "Memory", () -> {
            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            long max = Runtime.getRuntime().maxMemory();
            return (double) (total - free) / (double) max;
        }, () -> {
            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            long max = Runtime.getRuntime().maxMemory();
            return Math.round((double) (total - free) / max * 100) + "%";
        }));

        indicators.add(new PlusIndicator(tps, "TPS", () -> (double) (Managers.SERVER.getTPS() / 20.0f),
                () -> String.valueOf(Math.round(Managers.SERVER.getTPS()))));

        indicators.add(new PlusIndicator(speed, "Speed", () -> (double) Math.min(1, Managers.PLAYER.currentPlayerSpeed / 1.5),
                () -> String.format("%.1f", Managers.PLAYER.currentPlayerSpeed * 20)));
    }

    @Override
    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        List<PlusIndicator> active = new ArrayList<>();
        for (PlusIndicator ind : indicators)
            if (ind.enabled.getValue()) active.add(ind);

        int count = active.size();
        if (count == 0) return;

        float startX = getPosX();
        float startY = getPosY();
        Color bgCol = rectColor.getValue().getColorObject();

        for (int i = 0; i < count; i++) {
            float x = startX + i * rangeBetween.getValue();
            Render2DEngine.drawRect(context.getMatrices(), x, startY, 44, 44, bgCol);
            drawCircle(context, x + 22, startY + 24, active.get(i));
        }

        float totalWidth = count * rangeBetween.getValue() - 8;
        setBounds(startX, startY, totalWidth, 44);
    }

    private void drawCircle(DrawContext context, float cx, float cy, PlusIndicator ind) {
        double progress = ind.getSmoothProgress();
        float radius = 11f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder bgBuf = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; i++) {
            double rad = Math.toRadians(i);
            bgBuf.vertex((float) (cx + Math.cos(rad) * radius), (float) (cy + Math.sin(rad) * radius), 0).color(0.1f, 0.1f, 0.1f, 0.5f);
        }
        BufferRenderer.drawWithGlobalProgram(bgBuf.end());

        if (progress > 0) {
            int start = -90;
            int end = (int) (start + 360.0 * progress);
            BufferBuilder progBuf = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = start; i <= end; i++) {
                double rad = Math.toRadians(i);
                float r2 = radius - 0.5f;
                Color col = getColor(i, progress);
                progBuf.vertex((float) (cx + Math.cos(rad) * r2), (float) (cy + Math.sin(rad) * r2), 0)
                        .color(col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f, 1f);
            }
            BufferRenderer.drawWithGlobalProgram(progBuf.end());
        }

        FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), ind.getDisplayText(),
                cx, cy + 1, new Color(200, 200, 200).getRGB());
        FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), ind.getName(),
                cx, cy - 13, new Color(200, 200, 200).getRGB());
    }

    private Color getColor(int angle, double progress) {
        float stage = (float) (angle + 90) / 360.0f;
        return switch (colorMode.getValue()) {
            case StateBased -> {
                int r = (int) (255f - 255f * (float) progress);
                int g = (int) (255f * (float) progress);
                int b = (int) (100f * (float) progress);
                yield new Color(r, g, b);
            }
            case Astolfo -> {
                double hue = (System.currentTimeMillis() % 10000) / 10000.0 + stage;
                yield Color.getHSBColor((float) (hue % 1.0), 0.8f, 1.0f);
            }
            default -> color.getValue().getColorObject();
        };
    }

    private static class PlusIndicator {
        private final Setting<Boolean> enabled;
        private final String name;
        private final Supplier<Double> progressSupplier;
        private final Supplier<String> displaySupplier;
        private float smoothProgress = 0f;

        interface Supplier<T> { T get(); }

        PlusIndicator(Setting<Boolean> enabled, String name, Supplier<Double> ps, Supplier<String> ds) {
            this.enabled = enabled;
            this.name = name;
            this.progressSupplier = ps;
            this.displaySupplier = ds;
        }

        void update() {
            double target = Math.max(progressSupplier.get(), 0.0);
            smoothProgress = AnimationUtility.fast(smoothProgress, (float) target, 15f);
        }

        float getSmoothProgress() { return smoothProgress; }
        String getName() { return name; }
        String getDisplayText() { return displaySupplier.get(); }
    }
}
