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
        // stubbed for 1.21.9
    }
}