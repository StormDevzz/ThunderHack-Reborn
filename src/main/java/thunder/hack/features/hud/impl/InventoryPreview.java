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

        float s = scale.getValue();
        float slot = SLOT_SIZE * s;
        float gap = SLOT_GAP * s;
        float totalW = COLS * slot + (COLS - 1) * gap;
        float totalH = ROWS * slot + (ROWS - 1) * gap + HOTBAR_OFFSET * s;
        float bx = getPosX();
        float by = getPosY();

        if (background.getValue())
            Render2DEngine.drawRound(context.getMatrices(), bx - 4, by - 4, totalW + 8, totalH + 8, bgRound.getValue(), bgColor.getValue().getColorObject());

        context.getMatrices().push();
        context.getMatrices().translate(bx, by, 0);

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < COLS; col++)
                drawSlot(context, row * COLS + col, col * (slot + gap), row * (slot + gap), s);

        float hy = 3 * (slot + gap) + HOTBAR_OFFSET * s;
        for (int col = 0; col < COLS; col++)
            drawSlot(context, col + 27, col * (slot + gap), hy, s);

        context.getMatrices().pop();
        setBounds(bx, by, totalW, totalH);
    }

    private void drawSlot(DrawContext context, int index, float x, float y, float s) {
        ItemStack stack = mc.player.getInventory().main.get(index);

        if (slotBgr.getValue())
            Render2DEngine.drawRound(context.getMatrices(), x, y, SLOT_SIZE * s, SLOT_SIZE * s, slotRound.getValue(), slotColor.getValue().getColorObject());

        if (!stack.isEmpty()) {
            context.getMatrices().push();
            context.getMatrices().translate(x + 1 * s, y + 1 * s, 0);
            context.getMatrices().scale(s, s, 1f);
            context.drawItemWithoutEntity(stack, 0, 0);
            context.drawStackOverlay(mc.textRenderer, stack, 0, 0);
            context.getMatrices().pop();
        }
    }
}
