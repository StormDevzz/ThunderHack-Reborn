package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class Particles {
    public double x, y, deltaX, deltaY, size, opacity;
    public Color color;

    public static Color mixColors(final Color color1, final Color color2, final double percent) {
        final double inverse_percent = 1.0 - percent;
        final int redPart = (int) (color1.getRed() * percent + color2.getRed() * inverse_percent);
        final int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inverse_percent);
        final int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inverse_percent);
        return new Color(redPart, greenPart, bluePart);
    }

    public void render2D(MatrixStack matrixStack) {
        matrixStack.push();
        matrixStack.translate(x, y, 0);
        Render2DEngine.drawStar(matrixStack, Render2DEngine.injectAlpha(color, (int) opacity), (float) size);
        matrixStack.pop();
    }

    public void updatePosition() {
        x += deltaX;
        y += deltaY;

        deltaY *= 0.95;
        deltaX *= 0.95;

        opacity -= 2f;
        size /= 1.01;

        if (opacity < 1)
            opacity = 1;
    }

    public void init(final double x, final double y, final double deltaX, final double deltaY, final double size, final Color color) {
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.size = size;
        this.opacity = 254;
        this.color = color;
    }
}