package thunder.hack.modules.render;

import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import thunder.hack.core.ModuleManager;
import thunder.hack.modules.Module;

public class GlintColor extends Module {
    private final SettingGroup sgGeneral = settings.createGroup("General");

    private final Setting<Color> color = sgGeneral.add(new ColorSetting.Builder()
            .name("glint-color")
            .description("Color of the enchantment glint.")
            .defaultValue(new Color(255, 0, 0, 255))
            .build());

    public GlintColor() {
        super(ModuleManager.Category.RENDER, "GlintColor", "Changes the color of enchantment glint.");
    }

    public Color getColor() {
        return color.get();
    }
}
