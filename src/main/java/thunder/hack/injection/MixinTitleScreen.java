package thunder.hack.injection;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;

import java.net.URI;

import static thunder.hack.features.modules.Module.mc;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {
    protected MixinTitleScreen(Text title) {
        super(title);
    }

    private static boolean ravexRecommended = false;

    @Inject(method = "init", at = @At("RETURN"))
    public void postInitHook(CallbackInfo ci) {
        if (ModuleManager.clickGui.getBind().getKey() == -1) {
            ModuleManager.clickGui.setBind(InputUtil.fromTranslationKey("key.keyboard.p").getCode(), false, false);
        }

        if (!ravexRecommended) {
            ravexRecommended = true;
            mc.setScreen(new ConfirmScreen(
                    confirm -> {
                        if (confirm) {
                            Util.getOperatingSystem().open(URI.create("https://ravex.serveousercontent.com/"));
                            Util.getOperatingSystem().open(URI.create("https://github.com/StormDevzz/RaveX"));
                        }
                        mc.setScreen(this);
                    },
                    Text.of(Formatting.RED + (isRu() ? "Внимание: Рекомендуем перейти на RaveX!" : "Warning: We recommend switching to RaveX!")),
                    Text.of((isRu() ?
                        "ThunderHack устарел и работает нестабильно. Рекомендуем перейти на RaveX - новый клиент от StormDevzz, который намного стабильнее и не поломан!" :
                        "ThunderHack is outdated and unstable. We recommend switching to RaveX - a new client by StormDevzz that is much more stable and less broken!")
                        + "\n\nWebsite: https://ravex.serveousercontent.com/\nGitHub: https://github.com/StormDevzz/RaveX"),
                    Text.of(isRu() ? "Перейти на RaveX" : "Switch to RaveX"),
                    Text.of(isRu() ? "Продолжить" : "Continue")
            ));
        }
    }
}
