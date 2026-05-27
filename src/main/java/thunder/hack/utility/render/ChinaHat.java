package thunder.hack.utility.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;

import static thunder.hack.features.modules.Module.mc;

public final class ChinaHat {
    public static void render(PlayerEntity player, MatrixStack stack, float height, float radius, int segments, Color color) {
        Vec3d base = getInterpolatedPos(player);
        double yOff = player.getHeight() + 0.1;
        Vec3d tip = base.add(0, yOff + height, 0);

        stack.push();
        double dx = base.x - mc.getEntityRenderDispatcher().camera.getPos().x;
        double dy = base.y - mc.getEntityRenderDispatcher().camera.getPos().y;
        double dz = base.z - mc.getEntityRenderDispatcher().camera.getPos().z;
        stack.translate(dx, dy, dz);

        Matrix4f mat = stack.peek().getPositionMatrix();
        double tipX = 0, tipY = yOff + height, tipZ = 0;

        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;

            double x1 = radius * Math.cos(angle1);
            double z1 = radius * Math.sin(angle1);
            double x2 = radius * Math.cos(angle2);
            double z2 = radius * Math.sin(angle2);

            Render3DEngine.drawLine(
                    new Vec3d(dx + x1, dy + yOff, dz + z1),
                    new Vec3d(dx + x2, dy + yOff, dz + z2),
                    color
            );

            Render3DEngine.drawLine(
                    new Vec3d(dx + x1, dy + yOff, dz + z1),
                    new Vec3d(dx + tipX, dy + tipY, dz + tipZ),
                    color
            );
        }

        stack.pop();
    }

    private static Vec3d getInterpolatedPos(PlayerEntity player) {
        return player.getEntityPos();
    }
}
