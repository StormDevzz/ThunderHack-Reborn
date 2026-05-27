package thunder.hack.features.modules.client;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;

import java.awt.*;

public final class Notifications extends Module {
    public Notifications() {
        super("Notifications", Category.CLIENT);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.CrossHair);

    public final Setting<ColorSetting> bgColor = new Setting<>("BgColor", new ColorSetting(new Color(0xDD002040, true).getRGB()));
    public final Setting<ColorSetting> successColor = new Setting<>("SuccessColor", new ColorSetting(new Color(0, 170, 0).getRGB()));
    public final Setting<ColorSetting> infoColor = new Setting<>("InfoColor", new ColorSetting(new Color(0, 255, 255).getRGB()));
    public final Setting<ColorSetting> warningColor = new Setting<>("WarningColor", new ColorSetting(new Color(255, 170, 0).getRGB()));
    public final Setting<ColorSetting> errorColor = new Setting<>("ErrorColor", new ColorSetting(new Color(255, 0, 0).getRGB()));
    public final Setting<ColorSetting> enabledColor = new Setting<>("EnabledColor", new ColorSetting(new Color(0, 170, 0).getRGB()));
    public final Setting<ColorSetting> disabledColor = new Setting<>("DisabledColor", new ColorSetting(new Color(170, 0, 0).getRGB()));

    public enum Mode {
        Default, CrossHair, Text
    }
}
