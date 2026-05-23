package thunder.hack.features.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import thunder.hack.features.hud.HudElement;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class InventoryPreview extends HudElement {
    private final Setting<Float> scale = new Setting<>("Scale", 1f, 0.5f, 2f);

    private final Setting<SettingGroup> backgroundGroup = new Setting<>("Background", new SettingGroup(false, 0));
    private final Setting<Boolean> background = new Setting<>("Background", true).addToGroup(backgroundGroup);
    private final Setting<ColorSetting> bgColor = new Setting<>("BG Color", new ColorSetting(new Color(0, 0, 0, 100))).addToGroup(backgroundGroup);
    private final Setting<Float> bgRound = new Setting<>("BG Round", 4f, 0f, 12f).addToGroup(backgroundGroup);

    private final Setting<SettingGroup> slotsGroup = new Setting<>("Slots", new SettingGroup(false, 0));
    private final Setting<Boolean> slotBgr = new Setting<>("Slot Bg", true).addToGroup(slotsGroup);
    private final Setting<ColorSetting> slotColor = new Setting<>("Slot Color", new ColorSetting(new Color(30, 30, 30, 120))).addToGroup(slotsGroup);
    private final Setting<Float> slotRound = new Setting<>("Slot Round", 2f, 0f, 6f).addToGroup(slotsGroup);

    private static final int SLOT_SIZE = 18;
    private static final int SLOT_GAP = 1;
    private static final int ROWS = 4;
    private static final int COLS = 9;
    private static final int HOTBAR_OFFSET = 2;

    public InventoryPreview() {
        super("InventoryPreview", 162, 72);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        if (mc.player == null) return;

        // stubbed for 1.21.9
    }

    private void drawSlot(DrawContext context, int index, float x, float y, float s) {
        // stubbed for 1.21.9
    }
}
