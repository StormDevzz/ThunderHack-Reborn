package thunder.hack.injection;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.core.manager.client.ModuleManager;

@Mixin(DisconnectedScreen.class)
public abstract class MixinReconnectScreen extends Screen {

    @Shadow
    private Screen parent;

    // Время начала отсчёта в миллисекундах
    private long autoStartTime = -1;
    // Флаг — идёт ли авто-реконнект
    private boolean autoReconnectActive = false;
    // Кнопка отмены
    private ButtonWidget cancelButton = null;

    protected MixinReconnectScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void initHook(CallbackInfo ci) {
        if (!ModuleManager.autoReconnect.isEnabled()) return;

        // Кнопка ручного реконнекта
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Reconnect"),
                (button) -> {
                    autoReconnectActive = false;
                    ModuleManager.autoReconnect.reconnect(parent);
                })
                .dimensions(this.width / 2 - 100, this.height / 2 + 24, 200, 20)
                .build());

        // Запускаем авто-реконнект если включено
        if (ModuleManager.autoReconnect.isAuto() && ModuleManager.autoReconnect.hasServer()) {
            autoStartTime = System.currentTimeMillis();
            autoReconnectActive = true;

            cancelButton = ButtonWidget.builder(
                    Text.literal("Cancel"),
                    (button) -> {
                        autoReconnectActive = false;
                        cancelButton.visible = false;
                    })
                    .dimensions(this.width / 2 - 100, this.height / 2 + 48, 200, 20)
                    .build();
            this.addDrawableChild(cancelButton);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (!ModuleManager.autoReconnect.isEnabled()) return;
        if (!autoReconnectActive) return;

        long delayMs = ModuleManager.autoReconnect.getDelay() * 1000L;
        long elapsed = System.currentTimeMillis() - autoStartTime;
        long remaining = delayMs - elapsed;

        if (remaining <= 0) {
            // Время вышло — реконнектим
            autoReconnectActive = false;
            ModuleManager.autoReconnect.reconnect(parent);
            return;
        }

        // Показываем живой обратный отсчёт в десятых долях секунды
        double secondsLeft = remaining / 1000.0;
        String timerText = String.format("Reconnecting in %.1fs...", secondsLeft);

        // Прогресс-бар
        int barWidth = 200;
        int barX = this.width / 2 - barWidth / 2;
        int barY = this.height / 2 + 75;
        float progress = (float) remaining / delayMs;

        // Фон бара
        context.fill(barX, barY, barX + barWidth, barY + 4, 0xFF333333);
        // Заполнение бара (зелёный → красный по мере убывания)
        int barColor = progress > 0.5f ? 0xFF00CC00 : (progress > 0.25f ? 0xFFFFAA00 : 0xFFCC0000);
        context.fill(barX, barY, barX + (int)(barWidth * progress), barY + 4, barColor);

        // Текст таймера
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                timerText,
                this.width / 2,
                this.height / 2 + 82,
                0xFFFFFF
        );
    }
}