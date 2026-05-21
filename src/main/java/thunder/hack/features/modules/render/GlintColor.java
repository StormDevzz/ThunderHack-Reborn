package thunder.hack.features.modules.render;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;

import java.awt.*;

public class GlintColor extends Module {
    public GlintColor() {
        super("GlintColor", Category.RENDER);
    }

    private final Setting<Boolean> customGlint = new Setting<>("CustomGlint", false);
    private final Setting<ColorSetting> glintColor = new Setting<>("GlintColor", new ColorSetting(new Color(0x932DD8E8, true)), v -> customGlint.getValue());
    
    private final Setting<Boolean> customEnchantTable = new Setting<>("EnchantTableEffect", false);
    private final Setting<ColorSetting> enchantTableColor = new Setting<>("EnchantTableColor", new ColorSetting(new Color(0x932DE830, true)), v -> customEnchantTable.getValue());

    public boolean shouldOverrideGlint() {
        return isEnabled() && customGlint.getValue();
    }

    public Color getGlintColor() {
        return glintColor.getValue().getColorObject();
    }

    public boolean shouldOverrideEnchantTable() {
        return isEnabled() && customEnchantTable.getValue();
    }

    public Color getEnchantTableColor() {
        return enchantTableColor.getValue().getColorObject();
    }
}
