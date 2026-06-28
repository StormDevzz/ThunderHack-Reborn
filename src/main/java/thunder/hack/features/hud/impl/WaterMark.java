package thunder.hack.features.hud.impl;

// RaveX Team — code interaction (https://github.com/StormDevzz/RaveX)

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.client.Media;
import thunder.hack.features.modules.misc.NameProtect;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextUtil;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WaterMark extends HudElement {
        public WaterMark() {
                super("WaterMark", 100, 35);
        }

        public static final Setting<Mode> mode = new Setting<>("Mode", Mode.Big);
        private final Setting<Boolean> ru = new Setting<>("RU", false);

        // Настройки для Old-режима
        private final Setting<ColorSetting> oldTextColor = new Setting<>("OldTextColor",
                        new ColorSetting(new Color(-1)),
                        v -> mode.is(Mode.Old));
        private final Setting<ColorSetting> oldBgColor = new Setting<>("OldBgColor",
                        new ColorSetting(new Color(0xFF0F0F10, true)), v -> mode.is(Mode.Old));
        private final Setting<ColorSetting> oldShadowColor = new Setting<>("OldShadowColor",
                        new ColorSetting(new Color(0xFF0F0F10, true)), v -> mode.is(Mode.Old));

        private final TextUtil textUtil = new TextUtil(
                        "ТандерХак",
                        "ГромХак",
                        "ГрозаКлиент",
                        "ТандерХуй",
                        "ТандерХряк",
                        "ТандерХрюк",
                        "ТиндерХак",
                        "ТундраХак",
                        "ГромВзлом");

        // Для анимации Old
        private int animIndex = 0;
        private final Timer animTimer = new Timer();

        private enum Mode {
                Big, Small, Classic, BaltikaClient, Rifk, Old
        }

        private static final String[] ANIM_FRAMES = {
                        "_", "T_", "Th_", "Thu_", "Thun_", "Thund_", "Thunde_", "Thunder_",
                        "ThunderH_", "ThunderHa_", "ThunderHac_", "ThunderHack",
                        "ThunderHack", "ThunderHack", "ThunderHack",
                        "ThunderHac_", "ThunderHa_", "ThunderH_", "Thunder_",
                        "Thunde_", "Thund_", "Thun_", "Thu_", "Th_", "T_", "_"
        };

        public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}

        @Override
        public void onUpdate() {
                textUtil.tick();
        }
}