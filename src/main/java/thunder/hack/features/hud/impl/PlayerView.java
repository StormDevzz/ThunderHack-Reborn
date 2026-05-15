package thunder.hack.features.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.features.hud.HudElement;
import thunder.hack.setting.Setting;

public class PlayerView extends HudElement {
    public PlayerView() {
        super("PlayerView", 120, 160);
    }

    private final Setting<Integer> scale = new Setting<>("Scale", 80, 20, 220);
    private final Setting<Boolean> yaw = new Setting<>("Yaw", true);
    private final Setting<Boolean> pitch = new Setting<>("Pitch", true);

    @Override
    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        float posX = getPosX();
        float posY = getPosY();
        float width = scale.getValue() * 1.6f;
        float height = scale.getValue() * 2.0f;

        setBounds(posX, posY, width, height);

        drawPlayerOnScreen(
            context,
            (int) (posX + width / 2f),
            (int) (posY + height * 0.57f),
            scale.getValue(),
            yaw.getValue(),
            pitch.getValue()
        );
    }

    private void drawPlayerOnScreen(DrawContext context, int x, int y, int scale, boolean useYaw, boolean usePitch) {
        PlayerEntity player = mc.player;
        if (player == null) return;

        MatrixStack matrices = context.getMatrices();
        matrices.push();

        float scaleFactor = scale / 20f;
        matrices.translate(x, y, 100.0);
        matrices.scale(-scaleFactor, scaleFactor, scaleFactor);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(15.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0f));

        if (useYaw) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-player.getYaw()));
        }
        if (usePitch) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-player.getPitch()));
        }

        DiffuseLighting.enableForLevel();

        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        renderManager.setRenderShadows(false);
        renderManager.render(
            player,
            0.0,
            0.0,
            0.0,
            0.0f,
            1.0f,
            matrices,
            mc.getBufferBuilders().getEntityVertexConsumers(),
            0xF000F0
        );
        renderManager.setRenderShadows(true);

        DiffuseLighting.disableForLevel();
        matrices.pop();
    }
}
//Future