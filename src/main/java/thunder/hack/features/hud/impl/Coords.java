package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

public class Coords extends HudElement {
    public Coords() {
        super("Coords", 100, 10);
    }

    private final Setting<NetherCoords> netherCoords = new Setting<>("NetherCoords", NetherCoords.On);

    private enum NetherCoords {
        Off, On, OnlyNether
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        if (mc.player == null || mc.world == null) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        String coordsStr = String.format("XYZ: " + Formatting.WHITE + "%.1f, %.1f, %.1f" + Formatting.RESET, x, y, z);
        String netherCoordsStr = "";

        if (netherCoords.getValue() != NetherCoords.Off) {
            if (PlayerUtility.isInHell()) {
                double ox = x * 8.0;
                double oz = z * 8.0;
                netherCoordsStr = String.format(" [OW: " + Formatting.WHITE + "%.1f, %.1f" + Formatting.RESET + "]", ox, oz);
            } else if (PlayerUtility.isInOver()) {
                double nx = x / 8.0;
                double nz = z / 8.0;
                netherCoordsStr = String.format(" [N: " + Formatting.WHITE + "%.1f, %.1f" + Formatting.RESET + "]", nx, nz);
            }
        }

        String fullStr = coordsStr + netherCoordsStr;

        FontRenderers.sf_bold.drawString(context.getMatrices(), fullStr, getPosX(), getPosY(), HudEditor.textColor.getValue().getColor());
        setBounds(getPosX(), getPosY(), FontRenderers.sf_bold.getStringWidth(fullStr), FontRenderers.sf_bold.getFontHeight(fullStr));
    }
}