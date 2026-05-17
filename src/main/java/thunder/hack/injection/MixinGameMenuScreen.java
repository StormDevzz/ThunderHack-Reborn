package thunder.hack.injection;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.client.ModuleManager;

@Mixin(GameMenuScreen.class)
public abstract class MixinGameMenuScreen extends Screen {

    protected MixinGameMenuScreen(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void addReconnectButton(CallbackInfo ci) {
        if (!ModuleManager.autoReconnect.isEnabled()) return;
        if (!ModuleManager.autoReconnect.buttonInGameMenu.getValue()) return;
        if (!ModuleManager.autoReconnect.hasServer()) return;

        // Ищем кнопку Disconnect, чтобы разместить кнопку прямо под ней
        int disconnectY = -1;
        int disconnectX = this.width / 2 - 102;
        int disconnectW = 204;

        for (var element : this.children()) {
            if (element instanceof ButtonWidget btn) {
                String label = btn.getMessage().getString().toLowerCase();
                if (label.contains("disconnect")) {
                    disconnectY = btn.getY();
                    disconnectX = btn.getX();
                    disconnectW = btn.getWidth();
                    break;
                }
            }
        }

        // Если не нашли — ставим в дефолтное место
        int buttonY = disconnectY >= 0 ? disconnectY + 24 : this.height / 4 + 120 + 72 + 24;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aReconnect to " + ModuleManager.autoReconnect.getLastAddress()),
                (button) -> ModuleManager.autoReconnect.forceReconnect())
                .dimensions(disconnectX, buttonY, disconnectW, 20)
                .build());
    }
}
