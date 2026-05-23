package thunder.hack.features.modules.render;
import net.minecraft.client.gl.RenderPipelines;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.model.EndCrystalEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EndCrystalEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

public class Chams extends Module {
    public Chams() {
        super("Chams", Category.RENDER);
    }

    public final Setting<Boolean> crystals = new Setting<>("Crystals", false);
    private final Setting<ColorSetting> crystalColor = new Setting<>("CrystalColor", new ColorSetting(new Color(0x932DD8E8, true)), v -> crystals.getValue());
    private final Setting<Boolean> staticCrystal = new Setting<>("StaticCrystal", true, v -> crystals.getValue());
    private final Setting<CMode> crystalMode = new Setting<>("CrystalMode", CMode.One, v -> crystals.getValue());

    public final Setting<Boolean> players = new Setting<>("Players", false);
    private final Setting<ColorSetting> playerColor = new Setting<>("PlayerColor", new ColorSetting(new Color(0x932DD8E8, true)), v -> players.getValue());
    private final Setting<ColorSetting> friendColor = new Setting<>("FriendColor", new ColorSetting(new Color(0x932DE830, true)), v -> players.getValue());
    private final Setting<Boolean> playerTexture = new Setting<>("PlayerTexture", true, v -> players.getValue());
    private final Setting<Boolean> simple = new Setting<>("Simple", false, v -> players.getValue());

    private final Setting<Boolean> alternativeBlending = new Setting<>("AlternativeBlending", true);

    private enum CMode {
        One, Two, Three
    }

    private final Identifier crystalTexture = Identifier.of("textures/entity/end_crystal/end_crystal.png");

    public void renderCrystal(EndCrystalEntityRenderState state, MatrixStack matrixStack, int i, EndCrystalEntityModel model) {
        RenderSystem.enableBlend();
        if (alternativeBlending.getValue())
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        else RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        BufferBuilder buffer;

        if (crystalMode.getValue() != CMode.One) {
            if (crystalMode.getValue() == CMode.Three) {
                RenderSystem.setShaderTexture(0, crystalTexture);
            } else {
                RenderSystem.setShaderTexture(0, TextureStorage.crystalTexture2);
            }
            RenderSystem.setShader(RenderPipelines.POSITION_TEX);
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        } else {
            RenderSystem.setShader(RenderPipelines.POSITION);
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        }

        matrixStack.push();
        RenderSystem.setShaderColor(crystalColor.getValue().getGlRed(), crystalColor.getValue().getGlGreen(), crystalColor.getValue().getGlBlue(), crystalColor.getValue().getGlAlpha());
        matrixStack.scale(2.0f, 2.0f, 2.0f);
        matrixStack.translate(0.0f, -0.5f, 0.0f);

        model.setAngles(state);
        model.render(matrixStack, buffer, i, OverlayTexture.DEFAULT_UV);

        matrixStack.pop();
        Render2DEngine.endBuilding(buffer);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

    public void renderPlayer(net.minecraft.client.render.entity.state.LivingEntityRenderState state, MatrixStack matrixStack, int light, EntityModel model, PlayerEntity player) {
        RenderSystem.enableBlend();
        if (alternativeBlending.getValue())
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        else RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        Color color;
        if (Managers.FRIEND.isFriend(player))
            color = friendColor.getValue().getColorObject();
        else
            color = playerColor.getValue().getColorObject();

        boolean useTexture = playerTexture.getValue() && !simple.getValue();

        BufferBuilder buffer;
        if (useTexture) {
            AbstractClientPlayerEntity acp = (AbstractClientPlayerEntity) player;
            RenderSystem.setShaderTexture(0, acp.getSkinTextures().texture());
            RenderSystem.setShader(RenderPipelines.POSITION_TEX);
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        } else {
            RenderSystem.setShader(RenderPipelines.POSITION);
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        }

        matrixStack.push();
        matrixStack.translate(0, 1.501f, 0);
        matrixStack.scale(-1, -1, 1);
        RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        model.setAngles(state);
        model.render(matrixStack, buffer, light, OverlayTexture.DEFAULT_UV);

        matrixStack.pop();
        Render2DEngine.endBuilding(buffer);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

}