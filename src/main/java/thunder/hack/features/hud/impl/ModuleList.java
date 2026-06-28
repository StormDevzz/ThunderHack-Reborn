package thunder.hack.features.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import thunder.hack.core.Managers;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModuleList extends HudElement {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.ColorText);
    private final Setting<Integer> gste = new Setting<>("GS", 30, 1, 50);
    private final Setting<Boolean> glow = new Setting<>("glow", false);
    private final Setting<Boolean> hrender = new Setting<>("HideHud", true);
    private final Setting<Boolean> hhud = new Setting<>("HideRender", true);
    private final Setting<ColorSetting> color3 = new Setting<>("RectColor", new ColorSetting(-16777216));
    private final Setting<ColorSetting> color4 = new Setting<>("SideRectColor", new ColorSetting(-16777216));

    public ModuleList() {
        super("ArrayList", 50, 30);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        boolean reverse = getPosX() > (mc.getWindow().getScaledWidth() / 2f);
        float reversedX = getPosX();

        List<Module> list;
        try {
            list = Managers.MODULE.getEnabledModules().stream()
                .filter(this::shouldRender)
                .sorted(Comparator.comparing(module -> FontRenderers.modules.getStringWidth(module.getFullArrayString()) * -1))
                .toList();
        } catch (IllegalArgumentException ex) {
            return;
        }

        if (list.isEmpty()) return;

        List<Entry> entries = new ArrayList<>(list.size());
        float maxWidth = 0;

        for (int i = 0; i < list.size(); i++) {
            Module module = list.get(i);
            String text = module.getDisplayName() + Formatting.GRAY
                + (module.getDisplayInfo() != null
                    ? " [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]"
                    : "");
            int sw = (int) FontRenderers.modules.getStringWidth(text) + 3;
            entries.add(new Entry(text, sw, HudEditor.getColor(i * 9)));
            if (sw > maxWidth) maxWidth = sw;
        }

        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            int yOff = i * 9;

            if (glow.getValue()) {
                Render2DEngine.drawBlurredShadow(context.getMatrices(),
                    reverse ? reversedX - e.width - 3 : getPosX(),
                    getPosY() + yOff - 1,
                    e.width + 4, 9f, gste.getValue(), e.color);
            }

            Render2DEngine.drawRound(context.getMatrices(),
                reverse ? reversedX - e.width : getPosX(),
                getPosY() + yOff,
                e.width + 1.0f, 9.0f, 2.0f,
                mode.getValue() == Mode.ColorRect ? e.color : color3.getValue().getColorObject());

            FontRenderers.modules.drawString(context.getMatrices(), e.text,
                reverse ? reversedX - e.width + 2.0f : getPosX() + 3.0f,
                getPosY() + 3.0f + yOff,
                mode.getValue() == Mode.ColorRect ? -1 : e.color.getRGB());
        }

        setBounds(getPosX(), getPosY(), (int) maxWidth * (reverse ? -1 : 1), entries.size() * 9);
    }

    private record Entry(String text, int width, Color color) {}

    private boolean shouldRender(Module m) {
        return m.isDrawn() && (!hrender.getValue() || m.getCategory() != Category.RENDER) && (!hhud.getValue() || m.getCategory() != Category.HUD);
    }

    private enum Mode {
        ColorText, ColorRect
    }
}
