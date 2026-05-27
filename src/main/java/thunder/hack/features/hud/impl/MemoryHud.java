package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

public class MemoryHud extends HudElement {
    public MemoryHud() {
        super("MemoryHud", 100, 10);
    }

    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}

    private long toMiB(long bytes) {
        return bytes / 1024L / 1024L;
    }
}
