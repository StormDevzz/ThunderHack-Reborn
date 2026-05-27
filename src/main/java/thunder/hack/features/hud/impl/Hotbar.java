package thunder.hack.features.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.gui.windows.WindowsScreen;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class Hotbar extends HudElement {
    public Hotbar() {
        super("Hotbar", 0, 0);
    }

    public static final Setting<Mode> lmode = new Setting<>("LeftHandMode", Mode.Merged);

    public enum Mode {
        Merged, Separately
    }

    public void onRender2D(DrawContext context) {
        // stubbed for 1.21.9
    }

    // Bake only items
    public static void renderHotBarItems(float tickDelta, DrawContext context) {
        // stubbed for 1.21.9
    }

    private static void renderHotbarItem(DrawContext context, int i, int j, ItemStack itemStack) {
        // stubbed for 1.21.9
    }

    public static void renderXpBar(int x, MatrixStack matrices) {
        // stubbed for 1.21.9
    }
}
