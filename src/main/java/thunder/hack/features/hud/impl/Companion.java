package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.combat.AntiBot;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class Companion extends HudElement {
    public Companion() {
        super("2DCompanion", 50, 10);
    }

    public Setting<Integer> scale = new Setting<>("Scale", 50, 0, 100);
    public Setting<Integer> alpha = new Setting<>("Alpha", 255, 0, 255);
    public Setting<Mode> mode = new Setting<>("Mode", Mode.Boykisser);

    public static int currentFrame;
    private String message = "";
    private Timer lastPop = new Timer();
    private Timer frameRate = new Timer();

    @Override
    public void onUpdate() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || AntiBot.bots.contains(player) || player.getHealth() > 0 || !Managers.COMBAT.popList.containsKey(player.getName().getString()))
                continue;

            if (isRu())
                message = player.getName().getString() + " попнул " + (Managers.COMBAT.popList.get(player.getName().getString()) > 1 ? Managers.COMBAT.popList.get(player.getName().getString()) + " тотемов и сдох! ИЗЗЗЗИИ" : "тотем и сдох! ИЗЗЗЗИИ");
            else
                message = player.getName().getString() + " popped " + (Managers.COMBAT.popList.get(player.getName().getString()) > 1 ? Managers.COMBAT.popList.get(player.getName().getString()) + " totems and died EZ LMAO!" : "totem and died EZ LMAO!");
            lastPop.reset();
        }
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        context.getMatrices().push();
        context.getMatrices().translate((int) getPosX() + 100, (int) getPosY() + 100, 0);
        context.getMatrices().scale((float) scale.getValue() / 100f, (float) scale.getValue() / 100f, 1);
        context.getMatrices().translate(-((int) getPosX() + 100), -((int) getPosY() + 100), 0);

        float a = alpha.getValue() / 255f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, a);

        if (mode.getValue() == Mode.Boykisser)
            drawTextureImmediate(context.getMatrices().peek().getPositionMatrix(), TextureStorage.boykisser, (int) getPosX(), (int) getPosY(), 0, currentFrame * 128, 130, 128, 130, 6784);
        else if (mode.getValue() == Mode.Paimon)
            drawTextureImmediate(context.getMatrices().peek().getPositionMatrix(), TextureStorage.paimon, (int) getPosX(), (int) getPosY(), 0, currentFrame * 200, 200, 200, 200, 10600);
        else if (mode.getValue() == Mode.Baltika)
            drawTextureImmediate(context.getMatrices().peek().getPositionMatrix(), TextureStorage.baltika, (int) getPosX(), (int) getPosY(), 0, 0, 421, 800, 421, 800);
        else if (mode.getValue() == Mode.Kowk)
            drawTextureImmediate(context.getMatrices().peek().getPositionMatrix(), TextureStorage.kowk, (int) getPosX(), (int) getPosY(), 0, 0, 287, 252, 287, 252);
        else if (mode.getValue() == Mode.ClearSkyGirl)
            drawTextureImmediate(context.getMatrices().peek().getPositionMatrix(), TextureStorage.clearSkyGirl, (int) getPosX(), (int) getPosY(), 0, 0, 578, 432, 578, 432);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();

        context.getMatrices().pop();

        if (!lastPop.passedMs(2000)) {
            float w = FontRenderers.sf_bold.getStringWidth(message) + 8;
            float factor = MathUtility.clamp(lastPop.getPassedTimeMs(), 0, 500) / 500f;
            Render2DEngine.drawRound(context.getMatrices(), getPosX() + scale.getValue() / 3f, getPosY() + 70 - scale.getValue(), factor * w, 10, 3, new Color(0xFCD7DD));

            Render2DEngine.addWindow(context, getPosX() + scale.getValue() / 3f, getPosY() + 72 - scale.getValue(), factor * w + getPosX() + scale.getValue() / 3f, 20 + getPosY() + 72 - scale.getValue(), 1f);
            FontRenderers.sf_bold.drawString(context.getMatrices(), message, getPosX() + 2 + scale.getValue() / 3f, getPosY() + 72 - scale.getValue(), new Color(0x484848).getRGB());
            Render2DEngine.popWindow(context);
        }

        if (frameRate.passedMs(64)) {
            frameRate.reset();
            currentFrame++;
            if (currentFrame > 52)
                currentFrame = 0;
        }

        if (mode.getValue() == Mode.Baltika)
            setBounds(getPosX() + 100, getPosY() + 100, (scale.getValue() * 3f), (scale.getValue() * 3f));
        else
            setBounds(getPosX(), getPosY(), (scale.getValue() * 3f), (scale.getValue() * 3f));
    }


    @EventHandler
    public void onTotemPop(@NotNull TotemPopEvent event) {
        if (event.getEntity() == mc.player) return;

        if (isRu())
            message = event.getEntity().getName().getString() + " попнул " + (event.getPops() > 1 ? event.getPops() + " тотемов!" : "тотем!");
        else
            message = event.getEntity().getName().getString() + " popped " + (event.getPops() > 1 ? event.getPops() + " totems!" : " a totem!");
        lastPop.reset();
    }

    private void drawTextureImmediate(Matrix4f matrix, Identifier tex, int x, int y, float u, float v, int width, int height, int texWidth, int texHeight) {
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        int x1 = x + width;
        int y1 = y + height;
        float minU = u / texWidth;
        float maxU = (u + width) / texWidth;
        float minV = v / texHeight;
        float maxV = (v + height) / texHeight;
        buf.vertex(matrix, x, y1, 0).texture(minU, maxV);
        buf.vertex(matrix, x1, y1, 0).texture(maxU, maxV);
        buf.vertex(matrix, x1, y, 0).texture(maxU, minV);
        buf.vertex(matrix, x, y, 0).texture(minU, minV);
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    private enum Mode {
        Boykisser, Paimon, Baltika, Kowk, ClearSkyGirl
    }
}
