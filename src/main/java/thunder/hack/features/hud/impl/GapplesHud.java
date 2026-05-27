package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventEatFood;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class GapplesHud extends HudElement {
    public GapplesHud() {
        super("GapplesHud", 0, 0);
    }

    private float angle, prevAngle;

    private final Setting<Boolean> crapple = new Setting<>("Crapple", true);

    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}

    @EventHandler
    public void onEatFood(EventEatFood e) {
        if (e.getFood().getItem() == Items.GOLDEN_APPLE || e.getFood().getItem() == Items.ENCHANTED_GOLDEN_APPLE)
            angle = 15;
    }

    @Override
    public void onUpdate() {
        prevAngle = angle;
        if (angle > 0)
            angle--;
    }

    public int getItemCount(Item item) {
        if (mc.player == null) return 0;
        int n = 0;
        int n2 = 44;
        for (int i = 0; i <= n2; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (itemStack.getItem() != item) continue;
            n += itemStack.getCount();
        }
        return n;
    }
}
