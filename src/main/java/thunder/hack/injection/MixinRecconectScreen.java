package thunder.hack.injection;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.client.ModuleManager;

@Mixin(DisconnectedScreen.class)
public abstract class MixinReconnectim extends Screen {

    @Shadow
    private Text reason;

    protected MixinReconnectim(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void initHook(CallbackInfo ci) {
        if (ModuleManager.autoReconnect.isEnabled()) {
            // Кнопка быстрого реконнекта
            this.addDrawableChild(ButtonWidget.builder(
                    Text.of("Reconnect"),
                    (button) -> {
                        ModuleManager.autoReconnect.reconnect();
                        this.close();
                    })
                    .dimensions(this.width / 2 - 100, this.height / 2 + 50, 200, 20)
                    .build());

            // Кнопка реконнекта через указанный таймер
            this.addDrawableChild(ButtonWidget.builder(
                    Text.of("Reconnect in " + ModuleManager.autoReconnect.getDelay() + "s"),
                    (button) -> ModuleManager.autoReconnect.scheduleReconnect())
                    .dimensions(this.width / 2 - 100, this.height / 2 + 74, 200, 20)
                    .build());
        }
    }
}