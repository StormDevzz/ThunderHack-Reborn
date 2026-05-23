package thunder.hack.injection;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

import static thunder.hack.features.modules.Module.mc;

@Mixin(SplashOverlay.class)
public abstract class MixinSplashOverlay {
    private static final int BG_COLOR = 0xff0a0018;
    private static final int BAR_BG = 0x1effffff;
    private static final int BAR_FILL = 0xffb982ff;
    private static final int TEXT_MAIN = 0xb0ffffff;
    private static final int TEXT_SUB = 0x8cb0b0b0;
    private static final String[] LOADING_TEXTS = {
            "Загружаем ресурсы...",
            "Настраиваем модули...",
            "Компилируем хуки...",
            "Инициализируем рендер...",
            "Подключаем магию...",
            "Готовим кофе...",
            "Калибруем кристаллы...",
            "Отрисовываем гуи...",
            "Запускаем эвент бас...",
            "Чистим мусор...",
            "Грузим текстуры...",
            "Взламываем матрицу..."
    };

    @Unique private long lastTextSwitch;
    @Unique private int textIndex;

    @Final @Shadow private boolean reloading;
    @Shadow private float progress;
    @Shadow private long reloadCompleteTime = -1L;
    @Shadow private long reloadStartTime = -1L;
    @Final @Shadow private ResourceReload reload;
    @Final @Shadow private Consumer<Optional<Throwable>> exceptionHandler;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();
        try {
            renderCustom(context, mouseX, mouseY, delta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Unique
    public void renderCustom(DrawContext context, int mouseX, int mouseY, float delta) {
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        long now = Util.getMeasuringTimeMs();

        if (reloading && reloadStartTime == -1L)
            reloadStartTime = now;

        float f = reloadCompleteTime > -1L ? (float) (now - reloadCompleteTime) / 1000.0F : -1.0F;
        float g = reloadStartTime > -1L ? (float) (now - reloadStartTime) / 500.0F : -1.0F;

        if (lastTextSwitch == 0) lastTextSwitch = now;
        if (now - lastTextSwitch > 5000) {
            textIndex = (textIndex + 1) % LOADING_TEXTS.length;
            lastTextSwitch = now;
        }

        float p = MathHelper.clamp(progress, 0f, 1f);
        float fade;

        if (f >= 1.0F) {
            // fade-out phase — render current screen underneath, draw our overlay fading out
            if (mc.currentScreen != null) {
                mc.currentScreen.render(context, 0, 0, delta);
            }
            fade = 1.0F - MathHelper.clamp(f - 1.0F, 0f, 1f);
        } else if (reloading) {
            // loading phase — fade in from 0 to 1, render current screen behind
            if (mc.currentScreen != null && g < 1.0F) {
                mc.currentScreen.render(context, mouseX, mouseY, delta);
            }
            fade = MathHelper.clamp(g, 0f, 1f);
        } else {
            // initial — just solid background
            fade = 1.0F;
        }

        // draw our overlay
        context.fill(0, 0, sw, sh, withAlpha(BG_COLOR, (int) (fade * 255)));

        // progress bar
        int cx = sw / 2;
        int barW = 260;
        int barH = 5;
        int barX = cx - barW / 2;
        int barY = sh / 2 + 30;

        context.fill(barX, barY, barX + barW, barY + barH, withAlpha(BAR_BG, (int) (fade * 255)));
        if (p > 0f) {
            int fillW = Math.round(barW * p);
            context.fill(barX, barY, barX + fillW, barY + barH, withAlpha(BAR_FILL, (int) (fade * 255)));
        }

        // percentage
        String pct = (int) (p * 100) + "%";
        if (mc.textRenderer != null) {
            int tw = mc.textRenderer.getWidth(pct);
            context.drawText(mc.textRenderer, pct, cx - tw / 2, barY + barH + 10, withAlpha(TEXT_MAIN, (int) (fade * 255)), false);
        }

        // loading tip
        String tip = LOADING_TEXTS[textIndex];
        if (mc.textRenderer != null) {
            int tw = mc.textRenderer.getWidth(tip);
            context.drawText(mc.textRenderer, tip, cx - tw / 2, barY + barH + 26, withAlpha(TEXT_SUB, (int) (fade * 200)), false);
        }

        // progress
        float t = reload.getProgress();
        progress = MathHelper.clamp(progress * 0.95F + t * 0.050000012F, 0f, 1f);

        // lifecycle — same as vanilla
        if (f >= 2.0F) {
            mc.setOverlay(null);
        }

        if (reloadCompleteTime == -1L && reload.isComplete() && (!reloading || g >= 2.0F)) {
            try {
                reload.throwException();
                exceptionHandler.accept(Optional.empty());
            } catch (Throwable ex) {
                exceptionHandler.accept(Optional.of(ex));
            }
            reloadCompleteTime = Util.getMeasuringTimeMs();
            if (mc.currentScreen != null)
                mc.currentScreen.init(mc, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
        }
    }

    @Unique
    private static int withAlpha(int color, int alpha) {
        return color & 0x00ffffff | MathHelper.clamp(alpha, 0, 255) << 24;
    }
}

