package thunder.hack.features.modules.render;
import net.minecraft.client.gl.RenderPipelines;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4d;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class ItemESP extends Module {
    public ItemESP() {
        super("ItemESP", Category.RENDER);
    }

    private final Setting<Boolean> shadow = new Setting<>("Shadow", true);
    private final Setting<ColorSetting> scolor = new Setting<>("ShadowColor", new ColorSetting(new Color(0x000000).getRGB()));
    private final Setting<ColorSetting> tcolor = new Setting<>("TextColor", new ColorSetting(new Color(-1).getRGB()));

    private final Setting<ESPMode> espMode = new Setting<>("Mode", ESPMode.Rect);

    private final Setting<Float> radius = new Setting<>("Radius", 1f, 0.1f, 5f, v -> espMode.getValue() == ESPMode.Circle);
    private final Setting<Boolean> useHudColor = new Setting<>("UseHudColor", true, v -> espMode.getValue() == ESPMode.Circle);
    private final Setting<Integer> cOffset = new Setting<>("ColorOffset", 2, 1, 50, v -> espMode.getValue() == ESPMode.Circle && useHudColor.getValue());
    private final Setting<ColorSetting> circleColor = new Setting<>("CircleColor", new ColorSetting(new Color(-1).getRGB()), v -> espMode.getValue() == ESPMode.Circle && !useHudColor.getValue());
    private final Setting<Integer> cPoints = new Setting<>("CirclePoints", 12, 3, 32, v -> espMode.getValue() == ESPMode.Circle);

    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}

    public void onRender3D(MatrixStack stack) {
    // stubbed for 1.21.9
}

    private void drawRect(BufferBuilder bufferBuilder, Matrix4f stack, float posX, float posY, float endPosX, float endPosY) {
    // stubbed for 1.21.9
}

    @NotNull
    private static Vec3d[] getPoints(Entity ent) {
        Box axisAlignedBB = getBox(ent);
        Vec3d[] vectors = new Vec3d[]{new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)};
        return vectors;
    }

    @NotNull
    private static Box getBox(Entity ent) {
        double x = ent.lastX + (ent.getX() - ent.lastX) * Render3DEngine.getTickDelta();
        double y = ent.lastY + (ent.getY() - ent.lastY) * Render3DEngine.getTickDelta();
        double z = ent.lastZ + (ent.getZ() - ent.lastZ) * Render3DEngine.getTickDelta();
        Box axisAlignedBB2 = ent.getBoundingBox();
        Box axisAlignedBB = new Box(axisAlignedBB2.minX - ent.getX() + x - 0.05, axisAlignedBB2.minY - ent.getY() + y, axisAlignedBB2.minZ - ent.getZ() + z - 0.05, axisAlignedBB2.maxX - ent.getX() + x + 0.05, axisAlignedBB2.maxY - ent.getY() + y + 0.15, axisAlignedBB2.maxZ - ent.getZ() + z + 0.05);
        return axisAlignedBB;
    }

    private enum ESPMode {
        Rect, Circle, None
    }
}