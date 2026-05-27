package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Formatting;
import org.lwjgl.opengl.GL11;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

public class Speedometer extends HudElement {
    public float speed = 0f;
    private final Setting<Boolean> bps = new Setting<>("BPS", false);
    private final Setting<Boolean> average = new Setting<>("Average", false);

    public Speedometer() {
        super("Speedometer", 50, 10);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        String str = "Speed: " + Formatting.WHITE;
        if (!bps.getValue()) {
            str += MathUtility.round(getSpeedKpH() * ThunderHack.TICK_TIMER) + " km/h";
        } else {
            str += MathUtility.round(getSpeedMpS() * ThunderHack.TICK_TIMER) + " b/s";
        }

        float pX = getPosX() > mc.getWindow().getScaledWidth() / 2f ? getPosX() - FontRenderers.getModulesRenderer().getStringWidth(str) : getPosX();

        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), str, pX, getPosY(), HudEditor.textColor.getValue().getColor());
        setBounds(pX, getPosY(), FontRenderers.getModulesRenderer().getStringWidth(str), FontRenderers.getModulesRenderer().getFontHeight(str));
    }

    public float getSpeedKpH() {
        return (average.getValue() ? Managers.PLAYER.averagePlayerSpeed : Managers.PLAYER.currentPlayerSpeed) * 72f;
    }

    public float getSpeedMpS() {
        return (average.getValue() ? Managers.PLAYER.averagePlayerSpeed : Managers.PLAYER.currentPlayerSpeed) * 20f;
    }
}