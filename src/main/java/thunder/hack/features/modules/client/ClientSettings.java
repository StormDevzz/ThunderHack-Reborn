package thunder.hack.features.modules.client;

import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.Timer;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public final class ClientSettings extends Module {
    public static Setting<Boolean> futureCompatibility = new Setting<>("FutureCompatibility", false);
    public static Setting<Boolean> customMainMenu = new Setting<>("CustomMainMenu", true);
    public static Setting<Boolean> customPanorama = new Setting<>("CustomPanorama", true);
    public static Setting<Boolean> customLoadingScreen = new Setting<>("CustomLoadingScreen", true);
    public static Setting<Boolean> scaleFactorFix = new Setting<>("ScaleFactorFix", false);
    public static Setting<Float> scaleFactorFixValue = new Setting<>("ScaleFactorFixValue", 2f, 0f, 4f);
    public static Setting<Boolean> renderRotations = new Setting<>("RenderRotations", true);
    public static Setting<Boolean> skullEmoji = new Setting<>("SkullEmoji", true);
    public static Setting<Boolean> clientMessages = new Setting<>("ClientMessages", true);
    public static Setting<Boolean> debug = new Setting<>("Debug", false);
    public static Setting<Boolean> customBob = new Setting<>("CustomBob", true);
    public static Setting<Boolean> telemetry = new Setting<>("Telemetry", true);
    public static Setting<Language> language = new Setting<>("Language", Language.ENG);
    public static Setting<String> prefix = new Setting<>("Prefix", "@");
    public static Setting<ClipCommandMode> clipCommandMode = new Setting<>("ClipCommandMode", ClipCommandMode.Matrix);

    private final Timer updateCheckTimer = new Timer();
    private boolean firstCheck = true;

    public enum Language {
        RU,
        ENG
    }

    public enum ClipCommandMode {
        Default,
        Matrix
    }

    public ClientSettings() {
        super("ClientSettings", Category.CLIENT);
    }

    public static boolean isRu() {
        return language.is(Language.RU);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    @Override
    public void onUpdate() {
        if (!futureCompatibility.getValue())
            return;

        if (ThunderHack.isOutdated)
            return;

        if (firstCheck || updateCheckTimer.passedMs(1_800_000)) {
            firstCheck = false;
            updateCheckTimer.reset();
            checkForUpdate();
        }
    }

    private void checkForUpdate() {
        Managers.ASYNC.run(() -> {
            try {
                if (ThunderUtility.checkLatestVersion()) {
                    ThunderHack.isOutdated = true;
                    String msg = isRu() ? "Доступна новая версия! Скачайте её на GitHub." : "New version available! Download it on GitHub.";
                    Managers.NOTIFICATION.publicity("FutureCompatibility", msg, 8, Notification.Type.WARNING);
                }
            } catch (Exception ignored) {
            }
        });
    }
}
