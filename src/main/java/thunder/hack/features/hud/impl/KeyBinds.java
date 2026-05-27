package thunder.hack.features.hud.impl;

import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;
import java.util.Objects;

public class KeyBinds extends HudElement {
    public final Setting<ColorSetting> oncolor = new Setting<>("OnColor", new ColorSetting(-1));
    public final Setting<ColorSetting> offcolor = new Setting<>("OffColor", new ColorSetting(1));
    public final Setting<Boolean> onlyEnabled = new Setting<>("OnlyEnabled", false);

    public KeyBinds() {
        super("KeyBinds", 100, 100);
    }

    private float vAnimation, hAnimation;

    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}

    @NotNull
    public static String getShortKeyName(Module feature) {
        String sbind = feature.getBind().getBind();
        return switch (feature.getBind().getBind()) {
            case "LEFT_CONTROL" -> "LCtrl";
            case "RIGHT_CONTROL" -> "RCtrl";
            case "LEFT_SHIFT" -> "LShift";
            case "RIGHT_SHIFT" -> "RShift";
            case "LEFT_ALT" -> "LAlt";
            case "RIGHT_ALT" -> "RAlt";
            default -> sbind.toUpperCase();
        };
    }
}
