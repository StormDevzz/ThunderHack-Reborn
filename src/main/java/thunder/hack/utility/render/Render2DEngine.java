package thunder.hack.utility.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.Texture;
import thunder.hack.utility.math.MathUtility;


import net.minecraft.util.Identifier;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

import net.minecraft.client.texture.NativeImageBackedTexture;

import static thunder.hack.features.modules.Module.mc;

public class Render2DEngine {
    private static final java.util.ArrayDeque<DrawContext> contextStack = new java.util.ArrayDeque<>();

    public static void begin(DrawContext ctx) { contextStack.push(ctx); }
    public static void end() { contextStack.pop(); }
    public static DrawContext ctx() { return contextStack.peek(); }

    public static HashMap<Integer, BlurredShadow> shadowCache = new HashMap<>();
    public static HashMap<Integer, BlurredShadow> shadowCache1 = new HashMap<>();

    public static Matrix4f toMatrix4f(Matrix3x2fStack m3) {
        Matrix4f out = new Matrix4f();
        out.m00(m3.m00); out.m01(m3.m01); out.m02(0); out.m03(m3.m20);
        out.m10(m3.m10); out.m11(m3.m11); out.m12(0); out.m13(m3.m21);
        out.m20(0);      out.m21(0);      out.m22(1); out.m23(0);
        out.m30(0);      out.m31(0);      out.m32(0); out.m33(1);
        return out;
    }

    private static MatrixStack toMatrixStack(Matrix3x2fStack m3) {
        MatrixStack ms = new MatrixStack();
        Matrix4f m4 = ms.peek().getPositionMatrix();
        m4.m00(m3.m00); m4.m01(m3.m01); m4.m02(0); m4.m03(m3.m20);
        m4.m10(m3.m10); m4.m11(m3.m11); m4.m12(0); m4.m13(m3.m21);
        m4.m20(0);      m4.m21(0);      m4.m22(1); m4.m23(0);
        m4.m30(0);      m4.m31(0);      m4.m32(0); m4.m33(1);
        return ms;
    }

    public static void addWindow(DrawContext context, Rectangle r1) {
        Matrix4f matrix = toMatrix4f(context.getMatrices());
        Vector4f coord = new Vector4f(r1.x, r1.y, 0, 1);
        Vector4f end = new Vector4f(r1.x1, r1.y1, 0, 1);
        coord.mul(matrix);
        end.mul(matrix);
        float x = coord.x();
        float y = coord.y();
        float endX = end.x();
        float endY = end.y();
        beginScissor(context, x, y, endX, endY);
    }

    public static void popWindow(DrawContext context) {
        endScissor(context);
    }

    // Matrix3x2fStack overloads for DrawContext.getMatrices() compatibility
    public static void horizontalGradient(Matrix3x2fStack matrices, float x1, float y1, float x2, float y2, Color startColor, Color endColor) {
        DrawContext c = ctx();
        if (c != null) horizontalGradient(c, x1, y1, x2, y2, startColor, endColor);
    }

    public static void verticalGradient(Matrix3x2fStack matrices, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        DrawContext c = ctx();
        if (c != null) verticalGradient(c, left, top, right, bottom, startColor, endColor);
    }

    public static void drawRect(Matrix3x2fStack matrices, float x, float y, float width, float height, Color c) {
        DrawContext dc = ctx();
        if (dc != null) drawRect(dc, x, y, width, height, c);
    }

    public static void drawBlurredShadow(Matrix3x2fStack matrices, float x, float y, float width, float height, int blurRadius, Color color) {
        DrawContext c = ctx();
        if (c != null) drawBlurredShadow(c, x, y, width, height, blurRadius, color);
    }

    public static void drawGradientBlurredShadow(Matrix3x2fStack matrices, float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4) {
        DrawContext c = ctx();
        if (c != null) drawGradientBlurredShadow(c, x, y, width, height, blurRadius, color1, color2, color3, color4);
    }

    public static void drawRound(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, Color color) {
        DrawContext c = ctx();
        if (c != null) drawRound(c, x, y, width, height, radius, color);
    }

    public static void renderRoundedQuad(Matrix3x2fStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        DrawContext dc = ctx();
        if (dc != null) renderRoundedQuad(dc, c, fromX, fromY, toX, toY, radius, samples);
    }

    public static void renderRoundedQuad2(Matrix3x2fStack matrices, Color c, Color c2, Color c3, Color c4, double fromX, double fromY, double toX, double toY, double radius) {
        DrawContext dc = ctx();
        if (dc != null) renderRoundedQuad2(dc, c, c2, c3, c4, fromX, fromY, toX, toY, radius);
    }

    public static void draw2DGradientRect(Matrix3x2fStack matrices, float left, float top, float right, float bottom, Color leftBottomColor, Color leftTopColor, Color rightBottomColor, Color rightTopColor) {
        DrawContext c = ctx();
        if (c != null) draw2DGradientRect(c, left, top, right, bottom, leftBottomColor, leftTopColor, rightBottomColor, rightTopColor);
    }

    public static void drawTracerPointer(Matrix3x2fStack matrices, float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color) {
        DrawContext c = ctx();
        if (c != null) drawTracerPointer(c, x, y, size, tracerWidth, downHeight, down, glow, color);
    }

    public static void drawNewArrow(Matrix3x2fStack matrices, float x, float y, float size, Color color) {
        DrawContext c = ctx();
        if (c != null) drawNewArrow(c, x, y, size, color);
    }

    public static void drawDefaultArrow(Matrix3x2fStack matrices, float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color) {
        DrawContext c = ctx();
        if (c != null) drawDefaultArrow(c, x, y, size, tracerWidth, downHeight, down, glow, color);
    }

    public static void drawRect(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, float alpha) {
        DrawContext c = ctx();
        if (c != null) drawRect(c, x, y, width, height, radius, alpha);
    }

    public static void drawRect(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, float alpha, Color c1, Color c2, Color c3, Color c4) {
        DrawContext c = ctx();
        if (c != null) drawRect(c, x, y, width, height, radius, alpha, c1, c2, c3, c4);
    }

    public static void drawHudBase(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius) {
        DrawContext c = ctx();
        if (c != null) drawHudBase(c, x, y, width, height, radius);
    }

    public static void drawHudBase2(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, float blurStrenth, float blurOpacity, float animationFactor) {
        DrawContext c = ctx();
        if (c != null) drawHudBase2(c, x, y, width, height, radius, blurStrenth, blurOpacity, animationFactor);
    }

    public static void drawHudBase(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, boolean hud) {
        DrawContext c = ctx();
        if (c != null) drawHudBase(c, x, y, width, height, radius, hud);
    }

    public static void drawRoundedBlur(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, Color c1) {
        DrawContext c = ctx();
        if (c != null) drawRoundedBlur(c, x, y, width, height, radius, c1);
    }

    public static void drawRoundedBlur(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, Color c1, float blurStrenth, float blurOpacity) {
        DrawContext c = ctx();
        if (c != null) drawRoundedBlur(c, x, y, width, height, radius, c1, blurStrenth, blurOpacity);
    }

    public static void drawHudBase(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, float alpha) {
        DrawContext c = ctx();
        if (c != null) drawHudBase(c, x, y, width, height, radius, alpha);
    }

    public static void drawGuiBase(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, float opacity) {
        DrawContext c = ctx();
        if (c != null) drawGuiBase(c, x, y, width, height, radius, opacity);
    }

    public static void drawMainMenuShader(Matrix3x2fStack matrices, float x, float y, float width, float height) {
        DrawContext c = ctx();
        if (c != null) drawMainMenuShader(c, x, y, width, height);
    }

    public static void drawArc(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, float thickness, float start, float end, Color c1, Color c2) {
        DrawContext c = ctx();
        if (c != null) drawArc(c, x, y, width, height, radius, thickness, start, end, c1, c2);
    }

    public static void drawGradientRound(Matrix3x2fStack ms, float v, float v1, float i, float i1, float v2, Color darker, Color darker1, Color darker2, Color darker3) {
        DrawContext c = ctx();
        if (c != null) drawGradientRound(c, v, v1, i, i1, v2, darker, darker1, darker2, darker3);
    }

    public static void drawOrbiz(Matrix3x2fStack matrices, float z, final double r, Color c) {
        DrawContext dc = ctx();
        if (dc != null) drawOrbiz(dc, z, r, c);
    }

    public static void drawRectDumbWay(Matrix3x2fStack matrices, float x, float y, float x1, float y1, Color c1) {
        DrawContext c = ctx();
        if (c != null) drawRectDumbWay(c, x, y, x1, y1, c1);
    }

    public static void renderGradientTexture(Matrix3x2fStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, Color c1, Color c2, Color c3, Color c4) {
        DrawContext c = ctx();
        if (c != null) renderGradientTexture(c, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, c1, c2, c3, c4);
    }

    public static void renderTexture(Matrix3x2fStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
        DrawContext c = ctx();
        if (c != null) renderTexture(c, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    public static void drawRectWithOutline(Matrix3x2fStack matrices, float x, float y, float width, float height, Color c, Color c2) {
        DrawContext dc = ctx();
        if (dc != null) drawRectWithOutline(dc, x, y, width, height, c, c2);
    }

    public static void beginScissor(DrawContext context, double x, double y, double endX, double endY) {
        double width = endX - x;
        double height = endY - y;
        width = Math.max(0, width);
        height = Math.max(0, height);
        context.enableScissor((int) x, (int) y, (int) (x + width), (int) (y + height));
    }

    public static void endScissor(DrawContext context) {
        context.disableScissor();
    }

    // === DrawContext-based implementations ===

    public static void horizontalGradient(DrawContext context, float x1, float y1, float x2, float y2, Color startColor, Color endColor) {
        context.fillGradient((int) x1, (int) y1, (int) x2, (int) y2, startColor.getRGB(), endColor.getRGB());
    }

    public static void verticalGradient(DrawContext context, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        context.fillGradient((int) left, (int) top, (int) right, (int) bottom, startColor.getRGB(), endColor.getRGB());
    }

    public static void drawRect(DrawContext context, float x, float y, float width, float height, Color c) {
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), c.getRGB());
    }

    public static void drawRectWithOutline(DrawContext context, float x, float y, float width, float height, Color c, Color c2) {
        drawRect(context, x, y, width, height, c);
        drawRect(context, x - 1, y - 1, width + 2, 1, c2);
        drawRect(context, x - 1, y + height, width + 2, 1, c2);
        drawRect(context, x - 1, y, 1, height, c2);
        drawRect(context, x + width, y, 1, height, c2);
    }

    public static void drawRectDumbWay(DrawContext context, float x, float y, float x1, float y1, Color c1) {
        context.fill((int) x, (int) y, (int) x1, (int) y1, c1.getRGB());
    }

    public static void drawRect(DrawContext context, float x, float y, float width, float height, float radius, float alpha) {
        drawRound(context, x, y, width, height, radius, new Color(1f, 1f, 1f, alpha));
    }

    public static void drawRect(DrawContext context, float x, float y, float width, float height, float radius, float alpha, Color c1, Color c2, Color c3, Color c4) {
        renderRoundedQuad2(context, c1, c2, c3, c4, x, y, x + width, y + height, radius);
    }

    public static void drawRound(DrawContext context, float x, float y, float width, float height, float radius, Color color) {
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), color.getRGB());
    }

    public static void renderRoundedQuad(DrawContext context, Color c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        context.fill((int) fromX, (int) fromY, (int) toX, (int) toY, c.getRGB());
    }

    public static void renderRoundedQuad2(DrawContext context, Color c, Color c2, Color c3, Color c4, double fromX, double fromY, double toX, double toY, double radius) {
        context.fill((int) fromX, (int) fromY, (int) toX, (int) toY, c.getRGB());
    }

    public static void renderRoundedQuadInternal(DrawContext context, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        int color = new Color(cr, cg, cb, ca).getRGB();
        context.fill((int) fromX, (int) fromY, (int) toX, (int) toY, color);
    }

    public static void renderRoundedQuadInternal2(DrawContext context, float cr, float cg, float cb, float ca, float cr1, float cg1, float cb1, float ca1, float cr2, float cg2, float cb2, float ca2, float cr3, float cg3, float cb3, float ca3, double fromX, double fromY, double toX, double toY, double radC1) {
        int color = new Color(cr, cg, cb, ca).getRGB();
        context.fill((int) fromX, (int) fromY, (int) toX, (int) toY, color);
    }

    public static void draw2DGradientRect(DrawContext context, float left, float top, float right, float bottom, Color leftBottomColor, Color leftTopColor, Color rightBottomColor, Color rightTopColor) {
        context.fillGradient((int) left, (int) top, (int) right, (int) bottom, leftTopColor.getRGB(), leftBottomColor.getRGB());
    }

    public static void drawBlurredShadow(DrawContext context, float x, float y, float width, float height, int blurRadius, Color color) {
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), color.getRGB());
    }

    public static void drawGradientBlurredShadow(DrawContext context, float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4) {
        context.fillGradient((int) x, (int) y, (int) (x + width), (int) (y + height), color1.getRGB(), color2.getRGB());
    }

    public static void drawGradientBlurredShadow1(DrawContext context, float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4) {
        context.fillGradient((int) x, (int) y, (int) (x + width), (int) (y + height), color1.getRGB(), color2.getRGB());
    }

    public static void renderTexture(DrawContext context, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
        // Texture rendering via DrawContext.drawGuiTexture requires Identifier; skip for now
    }

    public static void renderTextureNoSetup(DrawContext context, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
    }

    public static void renderTextureInternal(DrawContext context, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
    }

    public static void renderGradientTexture(DrawContext context, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, Color c1, Color c2, Color c3, Color c4) {
        context.fillGradient((int) x0, (int) y0, (int) (x0 + width), (int) (y0 + height), c1.getRGB(), c2.getRGB());
    }

    public static void renderGradientTextureNoSetup(DrawContext context, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, Color c1, Color c2, Color c3, Color c4) {
        context.fillGradient((int) x0, (int) y0, (int) (x0 + width), (int) (y0 + height), c1.getRGB(), c2.getRGB());
    }

    public static void renderGradientTextureInternal(DrawContext context, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, Color c1, Color c2, Color c3, Color c4) {
        context.fillGradient((int) x0, (int) y0, (int) (x0 + width), (int) (y0 + height), c1.getRGB(), c2.getRGB());
    }

    public static void registerBufferedImageTexture(Texture i, BufferedImage bi) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", baos);
            byte[] bytes = baos.toByteArray();
            registerTexture(i, bytes);
        } catch (Exception ignored) {
        }
    }

    private static void registerTexture(Texture i, byte[] content) {
    }

    public static void setRectanglePoints(DrawContext context, float x, float y, float x1, float y1) {
        context.fill((int) x, (int) y, (int) x1, (int) y1, 0xFFFFFFFF);
    }

    public static void endBuilding(BufferBuilder bb) {
    }

    public static void setupRender() {
        GlStateManager._enableBlend();
    }

    public static void drawTracerPointer(DrawContext context, float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color) {
        switch (HudEditor.arrowsStyle.getValue()) {
            case Default -> drawDefaultArrow(context, x, y, size, tracerWidth, downHeight, down, glow, color);
            case New -> drawNewArrow(context, x, y, size + 8, new Color(color));
        }
    }

    public static void drawNewArrow(DrawContext context, float x, float y, float size, Color color) {
    }

    public static void drawDefaultArrow(DrawContext context, float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color) {
    }

    public static void endRender() {
    }

    public static void drawGradientRound(DrawContext context, float v, float v1, float i, float i1, float v2, Color darker, Color darker1, Color darker2, Color darker3) {
        renderRoundedQuad2(context, darker, darker1, darker2, darker3, v, v1, v + i, v1 + i1, v2);
    }

    // === MatrixStack overloads (look up context from stack) ===

    public static void horizontalGradient(MatrixStack matrices, float x1, float y1, float x2, float y2, Color startColor, Color endColor) {
        DrawContext c = ctx();
        if (c != null) horizontalGradient(c, x1, y1, x2, y2, startColor, endColor);
    }

    public static void verticalGradient(MatrixStack matrices, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        DrawContext c = ctx();
        if (c != null) verticalGradient(c, left, top, right, bottom, startColor, endColor);
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, Color c) {
        DrawContext dc = ctx();
        if (dc != null) drawRect(dc, x, y, width, height, c);
    }

    public static void drawRectWithOutline(MatrixStack matrices, float x, float y, float width, float height, Color c, Color c2) {
        DrawContext dc = ctx();
        if (dc != null) drawRectWithOutline(dc, x, y, width, height, c, c2);
    }

    public static void drawRectDumbWay(MatrixStack matrices, float x, float y, float x1, float y1, Color c1) {
        DrawContext dc = ctx();
        if (dc != null) drawRectDumbWay(dc, x, y, x1, y1, c1);
    }

    public static void drawBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color) {
        DrawContext c = ctx();
        if (c != null) drawBlurredShadow(c, x, y, width, height, blurRadius, color);
    }

    public static void drawGradientBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4) {
        DrawContext c = ctx();
        if (c != null) drawGradientBlurredShadow(c, x, y, width, height, blurRadius, color1, color2, color3, color4);
    }

    public static void drawGradientBlurredShadow1(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4) {
        DrawContext c = ctx();
        if (c != null) drawGradientBlurredShadow1(c, x, y, width, height, blurRadius, color1, color2, color3, color4);
    }

    public static void drawRound(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color) {
        DrawContext c = ctx();
        if (c != null) drawRound(c, x, y, width, height, radius, color);
    }

    public static void renderRoundedQuad(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        DrawContext dc = ctx();
        if (dc != null) renderRoundedQuad(dc, c, fromX, fromY, toX, toY, radius, samples);
    }

    public static void renderRoundedQuad2(MatrixStack matrices, Color c, Color c2, Color c3, Color c4, double fromX, double fromY, double toX, double toY, double radius) {
        DrawContext dc = ctx();
        if (dc != null) renderRoundedQuad2(dc, c, c2, c3, c4, fromX, fromY, toX, toY, radius);
    }

    public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        DrawContext dc = ctx();
        if (dc != null) renderRoundedQuadInternal(dc, cr, cg, cb, ca, fromX, fromY, toX, toY, radius, samples);
    }

    public static void renderRoundedQuadInternal2(Matrix4f matrix, float cr, float cg, float cb, float ca, float cr1, float cg1, float cb1, float ca1, float cr2, float cg2, float cb2, float ca2, float cr3, float cg3, float cb3, float ca3, double fromX, double fromY, double toX, double toY, double radC1) {
        DrawContext dc = ctx();
        if (dc != null) renderRoundedQuadInternal2(dc, cr, cg, cb, ca, cr1, cg1, cb1, ca1, cr2, cg2, cb2, ca2, cr3, cg3, cb3, ca3, fromX, fromY, toX, toY, radC1);
    }

    public static void draw2DGradientRect(MatrixStack matrices, float left, float top, float right, float bottom, Color leftBottomColor, Color leftTopColor, Color rightBottomColor, Color rightTopColor) {
        DrawContext c = ctx();
        if (c != null) draw2DGradientRect(c, left, top, right, bottom, leftBottomColor, leftTopColor, rightBottomColor, rightTopColor);
    }

    public static void drawTracerPointer(MatrixStack matrices, float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color) {
        DrawContext c = ctx();
        if (c != null) drawTracerPointer(c, x, y, size, tracerWidth, downHeight, down, glow, color);
    }

    public static void drawNewArrow(MatrixStack matrices, float x, float y, float size, Color color) {
        DrawContext c = ctx();
        if (c != null) drawNewArrow(c, x, y, size, color);
    }

    public static void drawDefaultArrow(MatrixStack matrices, float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color) {
        DrawContext c = ctx();
        if (c != null) drawDefaultArrow(c, x, y, size, tracerWidth, downHeight, down, glow, color);
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, float radius, float alpha) {
        DrawContext c = ctx();
        if (c != null) drawRect(c, x, y, width, height, radius, alpha);
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, float radius, float alpha, Color c1, Color c2, Color c3, Color c4) {
        DrawContext c = ctx();
        if (c != null) drawRect(c, x, y, width, height, radius, alpha, c1, c2, c3, c4);
    }

    public static void drawHudBase(MatrixStack matrices, float x, float y, float width, float height, float radius) {
        DrawContext c = ctx();
        if (c != null) drawHudBase(c, x, y, width, height, radius);
    }

    public static void drawHudBase2(MatrixStack matrices, float x, float y, float width, float height, float radius, float blurStrenth, float blurOpacity, float animationFactor) {
        DrawContext c = ctx();
        if (c != null) drawHudBase2(c, x, y, width, height, radius, blurStrenth, blurOpacity, animationFactor);
    }

    public static void drawHudBase(MatrixStack matrices, float x, float y, float width, float height, float radius, boolean hud) {
        DrawContext c = ctx();
        if (c != null) drawHudBase(c, x, y, width, height, radius, hud);
    }

    public static void drawRoundedBlur(MatrixStack matrices, float x, float y, float width, float height, float radius, Color c1) {
        DrawContext c = ctx();
        if (c != null) drawRoundedBlur(c, x, y, width, height, radius, c1);
    }

    public static void drawRoundedBlur(MatrixStack matrices, float x, float y, float width, float height, float radius, Color c1, float blurStrenth, float blurOpacity) {
        DrawContext c = ctx();
        if (c != null) drawRoundedBlur(c, x, y, width, height, radius, c1, blurStrenth, blurOpacity);
    }

    public static void drawHudBase(MatrixStack matrices, float x, float y, float width, float height, float radius, float alpha) {
        DrawContext c = ctx();
        if (c != null) drawHudBase(c, x, y, width, height, radius, alpha);
    }

    public static void drawGuiBase(MatrixStack matrices, float x, float y, float width, float height, float radius, float opacity) {
        DrawContext c = ctx();
        if (c != null) drawGuiBase(c, x, y, width, height, radius, opacity);
    }

    public static void drawMainMenuShader(MatrixStack matrices, float x, float y, float width, float height) {
        DrawContext c = ctx();
        if (c != null) drawMainMenuShader(c, x, y, width, height);
    }

    public static void drawArc(MatrixStack matrices, float x, float y, float width, float height, float radius, float thickness, float start, float end, Color c1, Color c2) {
        DrawContext c = ctx();
        if (c != null) drawArc(c, x, y, width, height, radius, thickness, start, end, c1, c2);
    }

    public static void drawGradientRound(MatrixStack ms, float v, float v1, float i, float i1, float v2, Color darker, Color darker1, Color darker2, Color darker3) {
        DrawContext c = ctx();
        if (c != null) drawGradientRound(c, v, v1, i, i1, v2, darker, darker1, darker2, darker3);
    }

    public static void drawOrbiz(MatrixStack matrices, float z, final double r, Color c) {
        DrawContext dc = ctx();
        if (dc != null) drawOrbiz(dc, z, r, c);
    }

    public static void drawStar(MatrixStack matrices, Color c, float scale) {
        DrawContext dc = ctx();
        if (dc != null) drawStar(dc, c, scale);
    }

    public static void drawHeart(MatrixStack matrices, Color c, float scale) {
        DrawContext dc = ctx();
        if (dc != null) drawHeart(dc, c, scale);
    }

    public static void drawBloom(MatrixStack matrices, Color c, float scale) {
        DrawContext dc = ctx();
        if (dc != null) drawBloom(dc, c, scale);
    }

    public static void drawBubble(MatrixStack matrices, float angle, float factor) {
        DrawContext dc = ctx();
        if (dc != null) drawBubble(dc, angle, factor);
    }

    public static void drawLine(float x, float y, float x1, float y1, int color) {
        DrawContext c = ctx();
        if (c != null) {
            c.fill((int) x, (int) y, (int) x1, (int) y + 1, color);
        }
    }

    public static void renderRoundedGradientRect(MatrixStack matrices, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float Radius) {
        DrawContext c = ctx();
        if (c != null) drawRect(c, x, y, width, height, Radius, 1f, color1, color2, color3, color4);
    }

    // === DrawContext-based helpers for less-used methods ===

    public static void drawRoundedBlur(DrawContext context, float x, float y, float width, float height, float radius, Color c1) {
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), c1.getRGB());
    }

    public static void drawRoundedBlur(DrawContext context, float x, float y, float width, float height, float radius, Color c1, float blurStrenth, float blurOpacity) {
        float a = Math.min(1f, blurOpacity * 1.5f);
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), (int)(a * 255)).getRGB());
    }

    public static void drawArc(DrawContext context, float x, float y, float width, float height, float radius, float thickness, float start, float end, Color c1, Color c2) {
    }

    public static void drawOrbiz(DrawContext context, float z, final double r, Color c) {
    }

    public static void drawStar(DrawContext context, Color c, float scale) {
    }

    public static void drawHeart(DrawContext context, Color c, float scale) {
    }

    public static void drawBloom(DrawContext context, Color c, float scale) {
    }

    public static void drawBubble(DrawContext context, float angle, float factor) {
    }

    public static void drawHudBase(DrawContext context, float x, float y, float width, float height, float radius) {
        drawHudBase(context, x, y, width, height, radius, HudEditor.alpha.getValue());
    }

    public static void drawHudBase2(DrawContext context, float x, float y, float width, float height, float radius, float blurStrenth, float blurOpacity, float animationFactor) {
        int a = (int) (240 * animationFactor);
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), new Color(0, 0, 0, a).getRGB());
    }

    public static void drawHudBase(DrawContext context, float x, float y, float width, float height, float radius, boolean hud) {
        drawHudBase(context, x, y, width, height, radius, HudEditor.alpha.getValue());
    }

    public static void drawHudBase(DrawContext context, float x, float y, float width, float height, float radius, float alpha) {
        Color c = HudEditor.plateColor.getValue().getColorObject();
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * c.getAlpha())).getRGB());
    }

    public static void drawGuiBase(DrawContext context, float x, float y, float width, float height, float radius, float opacity) {
        Color c = HudEditor.plateColor.getValue().getColorObject();
        int a = opacity > 0 ? (int)(opacity * 255) : c.getAlpha();
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.min(255, a)).getRGB());
    }

    public static void drawMainMenuShader(DrawContext context, float x, float y, float width, float height) {
        verticalGradient(context, x, y, x + width, y + height, new Color(0x070015), new Color(0x1a0a2e));
    }

    public static void renderTexture(MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
        DrawContext c = ctx();
        if (c != null) renderTexture(c, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    public static void renderTextureNoSetup(MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
        DrawContext c = ctx();
        if (c != null) renderTextureNoSetup(c, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    public static void renderTextureInternal(BufferBuilder buffer, MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
        DrawContext c = ctx();
        if (c != null) renderTextureInternal(c, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    public static void renderGradientTexture(MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, Color c1, Color c2, Color c3, Color c4) {
        DrawContext c = ctx();
        if (c != null) renderGradientTexture(c, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, c1, c2, c3, c4);
    }

    public static void renderGradientTextureNoSetup(MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, Color c1, Color c2, Color c3, Color c4) {
        DrawContext c = ctx();
        if (c != null) renderGradientTextureNoSetup(c, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, c1, c2, c3, c4);
    }

    public static void renderGradientTextureInternal(BufferBuilder buff, MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, Color c1, Color c2, Color c3, Color c4) {
        DrawContext c = ctx();
        if (c != null) renderGradientTextureInternal(c, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, c1, c2, c3, c4);
    }

    public static void renderGradientTextureInternal(BufferBuilder buff, Matrix3x2fStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, Color c1, Color c2, Color c3, Color c4) {
        renderGradientTextureInternal(buff, toMatrixStack(matrices), x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, c1, c2, c3, c4);
    }

    public static void setRectanglePoints(BufferBuilder buffer, Matrix4f matrix, float x, float y, float x1, float y1) {
    }

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX - width <= x && mouseY >= y && mouseY - height <= y;
    }

    public static float scrollAnimate(float endPoint, float current, float speed) {
        boolean shouldContinueAnimation = endPoint > current;
        if (speed < 0.0f) {
            speed = 0.0f;
        } else if (speed > 1.0f) {
            speed = 1.0f;
        }

        float dif = Math.max(endPoint, current) - Math.min(endPoint, current);
        float factor = dif * speed;
        return current + (shouldContinueAnimation ? factor : -factor);
    }

    public static Color injectAlpha(final Color color, final int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 0, 255));
    }

    public static Color TwoColoreffect(Color cl1, Color cl2, double speed, double count) {
        int angle = (int) (((System.currentTimeMillis()) / speed + count) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return interpolateColorC(cl1, cl2, angle / 360f);
    }

    public static Color astolfo(boolean clickgui, int yOffset) {
        float speed = clickgui ? 35 * 100 : 30 * 100;
        float hue = (System.currentTimeMillis() % (int) speed) + yOffset;
        if (hue > speed) {
            hue -= speed;
        }
        hue /= speed;
        if (hue > 0.5F) {
            hue = 0.5F - (hue - 0.5F);
        }
        hue += 0.5F;
        return Color.getHSBColor(hue, 0.4F, 1F);
    }

    public static Color rainbow(int delay, float saturation, float brightness) {
        double rainbow = Math.ceil((System.currentTimeMillis() + delay) / 16f);
        rainbow %= 360;
        return Color.getHSBColor((float) (rainbow / 360), saturation, brightness);
    }

    public static Color skyRainbow(int speed, int index) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        return Color.getHSBColor((double) ((float) ((angle %= 360) / 360.0)) < 0.5 ? -((float) (angle / 360.0)) : (float) (angle / 360.0), 0.5F, 1.0F);
    }

    public static Color fade(int speed, int index, Color color, float alpha) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;

        Color colorHSB = new Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360f));

        return new Color(colorHSB.getRed(), colorHSB.getGreen(), colorHSB.getBlue(), Math.max(0, Math.min(255, (int) (alpha * 255))));
    }

    public static Color getAnalogousColor(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float degree = 0.84f;
        float newHueSubtracted = hsb[0] - degree;
        return new Color(Color.HSBtoRGB(newHueSubtracted, hsb[1], hsb[2]));
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static int applyOpacity(int color_int, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        Color color = new Color(color_int);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity)).getRGB();
    }

    public static Color darker(Color color, float factor) {
        return new Color(Math.max((int) (color.getRed() * factor), 0), Math.max((int) (color.getGreen() * factor), 0), Math.max((int) (color.getBlue() * factor), 0), color.getAlpha());
    }

    public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, (int) (opacity * 255))));
    }

    public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return trueColor ? interpolateColorHue(start, end, angle / 360f) : interpolateColorC(start, end, angle / 360f);
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount), interpolateInt(color1.getGreen(), color2.getGreen(), amount), interpolateInt(color1.getBlue(), color2.getBlue(), amount), interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);

        Color resultColor = Color.getHSBColor(interpolateFloat(color1HSB[0], color2HSB[0], amount), interpolateFloat(color1HSB[1], color2HSB[1], amount), interpolateFloat(color1HSB[2], color2HSB[2], amount));

        return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(), interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return (float) interpolate(oldValue, newValue, (float) interpolationValue);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return (int) interpolate(oldValue, newValue, (float) interpolationValue);
    }

    public static boolean isDark(Color color) {
        return isDark(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
    }

    public static boolean isDark(float r, float g, float b) {
        return colorDistance(r, g, b, 0f, 0f, 0f) < colorDistance(r, g, b, 1f, 1f, 1f);
    }

    public static float colorDistance(float r1, float g1, float b1, float r2, float g2, float b2) {
        float a = r2 - r1;
        float b = g2 - g1;
        float c = b2 - b1;
        return (float) Math.sqrt(a * a + b * b + c * c);
    }

    public static @NotNull Color getColor(@NotNull Color start, @NotNull Color end, float progress, boolean smooth) {
        if (!smooth)
            return progress >= 0.95 ? end : start;

        final int rDiff = end.getRed() - start.getRed();
        final int gDiff = end.getGreen() - start.getGreen();
        final int bDiff = end.getBlue() - start.getBlue();
        final int aDiff = end.getAlpha() - start.getAlpha();

        return new Color(
                fixColorValue(start.getRed() + (int) (rDiff * progress)),
                fixColorValue(start.getGreen() + (int) (gDiff * progress)),
                fixColorValue(start.getBlue() + (int) (bDiff * progress)),
                fixColorValue(start.getAlpha() + (int) (aDiff * progress)));
    }

    private static int fixColorValue(int colorVal) {
        return colorVal > 255 ? 255 : Math.max(colorVal, 0);
    }

    public static class BlurredShadow {
        Texture id;

        public BlurredShadow(BufferedImage bufferedImage) {
            this.id = new Texture("texture/remote/" + RandomStringUtils.randomAlphanumeric(16));
            registerBufferedImageTexture(id, bufferedImage);
        }

        public void bind() {
        }
    }

    public static final HashMap<Identifier, Identifier> cleanedTextureCache = new HashMap<>();

    public static Identifier getCleanedTexture(Identifier original) {
        return getCleanedTexture(original, 0);
    }

    public static Identifier getCleanedTexture(Identifier original, int threshold) {
        Identifier cached = cleanedTextureCache.get(original);
        if (cached != null) return cached;
        try {
            var mc = net.minecraft.client.MinecraftClient.getInstance();
            var resource = mc.getResourceManager().getResource(original).orElse(null);
            if (resource == null) return original;
            NativeImage image;
            try (InputStream in = resource.getInputStream()) {
                image = NativeImage.read(in);
            }
            if (image == null) return original;
            NativeImage cleaned = removeBlackBackground(image, threshold);
            image.close();
            String cleanPath = "cleaned/" + original.getPath().replace('/', '_');
            Identifier cleanId = Identifier.of(original.getNamespace(), cleanPath);
            mc.getTextureManager().registerTexture(cleanId, new NativeImageBackedTexture(() -> cleanPath, cleaned));
            cleanedTextureCache.put(original, cleanId);
            return cleanId;
        } catch (Exception e) {
            return original;
        }
    }

    public static BufferedImage removeBlackBackground(BufferedImage image) {
        return removeBlackBackground(image, 0);
    }

    public static BufferedImage removeBlackBackground(BufferedImage image, int threshold) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int brightness = (r + g + b) / 3;
                if (brightness <= threshold || (r <= threshold && g <= threshold && b <= threshold)) {
                    result.setRGB(x, y, 0);
                } else if (a == 0) {
                    result.setRGB(x, y, 0);
                } else {
                    result.setRGB(x, y, rgb);
                }
            }
        }
        return result;
    }

    public static NativeImage removeBlackBackground(NativeImage image) {
        return removeBlackBackground(image, 0);
    }

    public static NativeImage removeBlackBackground(NativeImage image, int threshold) {
        NativeImage result = new NativeImage(NativeImage.Format.RGBA, image.getWidth(), image.getHeight(), false);
        long srcPtr = ((thunder.hack.injection.accesors.INativeImage) (Object) image).getPointer();
        long dstPtr = ((thunder.hack.injection.accesors.INativeImage) (Object) result).getPointer();
        java.nio.IntBuffer srcBuffer = org.lwjgl.system.MemoryUtil.memIntBuffer(srcPtr, image.getWidth() * image.getHeight());
        java.nio.IntBuffer dstBuffer = org.lwjgl.system.MemoryUtil.memIntBuffer(dstPtr, image.getWidth() * image.getHeight());
        srcBuffer.rewind();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int abgr = srcBuffer.get();
                int a = (abgr >> 24) & 0xFF;
                int b = (abgr >> 16) & 0xFF;
                int g = (abgr >> 8) & 0xFF;
                int r = abgr & 0xFF;
                int brightness = (r + g + b) / 3;
                if (brightness <= threshold || (r <= threshold && g <= threshold && b <= threshold)) {
                    dstBuffer.put(0);
                } else if (a == 0) {
                    dstBuffer.put(0);
                } else {
                    dstBuffer.put(abgr);
                }
            }
        }
        return result;
    }

    public record Rectangle(float x, float y, float x1, float y1) {
        public boolean contains(double x, double y) {
            return x >= this.x && x <= x1 && y >= this.y && y <= y1;
        }
    }

}
