package thunder.hack.features.modules.render;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class SpawnerManager extends Module {
    public SpawnerManager() {
        super("SpawnerManager", Category.RENDER);
    }

    private final Setting<ColorSetting> fillColor = new Setting<>("FillColor", new ColorSetting(0x80000000));
    private final Setting<ColorSetting> outlineColor = new Setting<>("OutlineColor", new ColorSetting(0x80000000));
    private final Setting<OutlineMode> outline = new Setting<>("Outline", OutlineMode.None);

    public void onRender2D(DrawContext context) {
        for (BlockEntity blockEntity : StorageEsp.getBlockEntities()) {
            if (blockEntity instanceof MobSpawnerBlockEntity spawner) {
                Vec3d vector = new Vec3d(spawner.getPos().getX() + 0.5, spawner.getPos().getY() + 1.5, spawner.getPos().getZ() + 0.5);
                Vector4d position = null;
                vector = Render3DEngine.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
                if (vector.z > 0 && vector.z < 1) {
                    position = new Vector4d(vector.x, vector.y, vector.z, 0);
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                }
                if (spawner.getLogic() == null || spawner.getLogic().getRenderedEntity(mc.world, spawner.getPos()) == null)
                    continue;
                String final_string = spawner.getLogic().getRenderedEntity(mc.world, spawner.getPos()).getName().getString() + " " + String.format("%.1f", ((float) spawner.getLogic().spawnDelay / 20f)) + "s";

                if (spawner.getLogic().getRotation() == spawner.getLogic().getLastRotation() && spawner.getLogic().getRotation() == 0f && (float) spawner.getLogic().spawnDelay / 20f == 1f)
                    final_string = spawner.getLogic().getRenderedEntity(mc.world, spawner.getPos()).getName().getString() + " loot!";

                if (position != null) {
                    double posX = position.x;
                    double posY = position.y;
                    double endPosX = position.z;

                    float diff = (float) (endPosX - posX) / 2;
                    float textWidth = (FontRenderers.sf_bold.getStringWidth(final_string) * 1);
                    float tagX = (float) ((posX + diff - textWidth / 2) * 1);

                    Render2DEngine.drawRect(context.getMatrices(), tagX - 2, (float) (posY - 13f), textWidth + 4, 11, fillColor.getValue().getColorObject());

                    switch (outline.getValue()) {
                        case Sync -> {
                            Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 14f), textWidth + 6, 1, HudEditor.getColor(270));
                            Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 3f), textWidth + 6, 1, HudEditor.getColor(0));
                            Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 14f), 1, 11, HudEditor.getColor(180));
                            Render2DEngine.drawRect(context.getMatrices(), tagX + textWidth + 2, (float) (posY - 14f), 1, 11, HudEditor.getColor(90));
                        }
                        case Custom -> {
                            Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 14f), textWidth + 6, 1, outlineColor.getValue().getColorObject());
                            Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 3f), textWidth + 6, 1, outlineColor.getValue().getColorObject());
                            Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 14f), 1, 11, outlineColor.getValue().getColorObject());
                            Render2DEngine.drawRect(context.getMatrices(), tagX + textWidth + 2, (float) (posY - 14f), 1, 11, outlineColor.getValue().getColorObject());
                        }
                    }

                    FontRenderers.sf_bold.drawString(context.getMatrices(), final_string, tagX, (float) posY - 10, -1);
                }
            }
        }
    }

    private enum OutlineMode {
        None, Sync, Custom
    }
}
