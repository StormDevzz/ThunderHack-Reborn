package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import org.lwjgl.opengl.GL11;
import net.minecraft.util.Formatting;
import thunder.hack.core.Managers;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

public class TPSCounter extends HudElement {
    public TPSCounter() {
        super("TPS", 50, 10);
    }

    private final Setting<Boolean> extraTps = new Setting<>("ExtraTPS", true);

    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}
}
