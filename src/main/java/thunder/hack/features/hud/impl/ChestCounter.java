package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.lwjgl.opengl.GL11;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

import static thunder.hack.features.modules.render.StorageEsp.getBlockEntities;

public class ChestCounter extends HudElement {
    public ChestCounter() {
        super("ChestCounter", 50, 10);
    }
    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        Pair<Integer, Integer> chests = getChestCount();
        String str = "Chests: " + Formatting.WHITE + "S:" + chests.getLeft() + " D:" + chests.getRight();
        float pX = getPosX() > mc.getWindow().getScaledWidth() / 2f ? getPosX() - FontRenderers.getModulesRenderer().getStringWidth(str) : getPosX();

        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), str, pX, getPosY(), HudEditor.textColor.getValue().getColor());
        setBounds(pX, getPosY(), FontRenderers.getModulesRenderer().getStringWidth(str), FontRenderers.getModulesRenderer().getFontHeight(str));
    }

    public Pair<Integer, Integer> getChestCount() {
        int singleCount = 0;
        int doubleCount = 0;

        for (BlockEntity be : getBlockEntities()) {
            if (be instanceof ChestBlockEntity chest) {
                ChestType chestType = mc.world.getBlockState(chest.getPos()).get(ChestBlock.CHEST_TYPE);
                if (chestType == ChestType.SINGLE) {
                    singleCount++;
                } else doubleCount++;
            }
        }
        return new Pair<>(singleCount, doubleCount / 2);
    }
}
