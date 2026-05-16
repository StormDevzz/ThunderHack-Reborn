package thunder.hack.injection;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
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

    private int ticks = 0;

    protected MixinReconnectScreen(Text title) {
        super(title);
    }

    private long startTime = -1;

    @Inject(method = "init", at = @At("TAIL"))
    public void initHook(CallbackInfo ci) {
        startTime = System.currentTimeMillis();
        if (ModuleManager.autoReconnect.isEnabled()) {
            this.addDrawableChild(ButtonWidget.builder(
                            Text.of("Reconnect"),
                            (button) -> {
                                ModuleManager.autoReconnect.reconnect(parent);
                            })
                    .dimensions(this.width / 2 - 100, this.height / 2 + 50, 200, 20)
                    .build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (ModuleManager.autoReconnect.isEnabled() && ModuleManager.autoReconnect.isAuto()) {
            if (startTime == -1) startTime = System.currentTimeMillis();
            
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            long remaining = ModuleManager.autoReconnect.getDelay() - elapsed;
            
            if (remaining <= 0) {
                startTime = System.currentTimeMillis() + 1000000; // Предотвращаем повторный запуск
                ModuleManager.autoReconnect.reconnect(parent);
            } else {
                String text = "Auto-reconnecting in " + remaining + "s...";
                context.drawCenteredTextWithShadow(this.textRenderer, text, this.width / 2, this.height / 2 + 80, 0xFFFFFF);
            }
        }
    }
}