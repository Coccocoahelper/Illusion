package org.example.illusion.feature.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import org.example.illusion.IllusionClient;
import org.example.illusion.feature.module.client.ClickGuiModule;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public final class Draw {
    public static final int BACKGROUND = new Color(24, 24, 24, 160).getRGB();
    public static final int BACKGROUND_DARK = new Color(12, 12, 12, 160).getRGB();
    public static final int BACKGROUND_HOVER = new Color(40, 40, 40, 160).getRGB();
    public static final int DIVIDER = 0x16FFFFFF;
    public static final int TRACK = 0x40FFFFFF;
    public static final int MUTED = 0xFFAAAAAA;

    private static final int ARC_SEGMENTS = 6;
    private static final int ARC_POINTS = ARC_SEGMENTS + 1;
    private static final int OUTLINE_POINTS = ARC_POINTS * 4;
    private static final float[] COS = new float[OUTLINE_POINTS];
    private static final float[] SIN = new float[OUTLINE_POINTS];
    private static final float[] EDGE_X = new float[OUTLINE_POINTS];
    private static final float[] EDGE_Y = new float[OUTLINE_POINTS];
    private static final float[] FRINGE_X = new float[OUTLINE_POINTS];
    private static final float[] FRINGE_Y = new float[OUTLINE_POINTS];

    static {
        for (int i = 0; i < OUTLINE_POINTS; i++) {
            double angle = (i / ARC_POINTS +(i % ARC_POINTS) / (double) ARC_SEGMENTS) * Math.PI / 2.0D;
            COS[i] = (float) Math.cos(angle);
            SIN[i] = (float) Math.sin(angle);
        }
    }

    private Draw() {
    }

    public static Color accent() {
        return IllusionClient.MODULES.get(ClickGuiModule.class).color;
    }

    public static void rect(int x, int y, int width, int height, int color) {
        GuiElement.fill(x, y, x + width, y + height, color);
    }

    public static void rounded(int x, int y, int width, int height, float radius, int color) {
        rounded(x, y, width, height, radius, radius, color);
    }

    public static void rounded(int x, int y, int width, int height, float top, float bottom, int color) {
        float limit = Math.min(width, height) / 2.0F;
        top = Math.min(top, limit);
        bottom = Math.min(bottom, limit);

        float feather = feather();
        arc(0, x + width - top, y + top, top, feather);
        arc(1, x + top, y + top, top, feather);
        arc(2, x + bottom, y + height - bottom, bottom, feather);
        arc(3, x + width - bottom, y + height - bottom, bottom, feather);

        BufferBuilder buffer = begin(GL11.GL_TRIANGLES);
        fan(buffer, x + width / 2.0F, y + height / 2.0F, 0, OUTLINE_POINTS, color);
        end();
    }

    public static void fill(float left, float top, float right, float bottom, int color) {
        fill(left, top, right, bottom, color, color);
    }

    public static void fill(float left, float top, float right, float bottom, int topColor, int bottomColor) {
        BufferBuilder buffer = begin(GL11.GL_QUADS);
        quad(buffer, left, top, right, bottom, topColor, topColor, bottomColor, bottomColor);
        end();
    }

    public static void gradient(int x, int y, int width, int height, int topLeft, int topRight, int bottomLeft, int bottomRight) {
        BufferBuilder buffer = begin(GL11.GL_QUADS);
        quad(buffer, x, y, x + width, y + height, topLeft, topRight, bottomLeft, bottomRight);
        end();
    }

    public static void roundedGradient(int x, int y, int width, int height, float topRadius, float bottomRadius, int topLeft, int topRight, int bottomLeft, int bottomRight) {
        float inset = Math.max(topRadius, bottomRadius);
        float left = x + inset;
        float right = x + width - inset;
        float top = y + topRadius;
        float bottom = y + height - bottomRadius;

        BufferBuilder buffer = begin(GL11.GL_QUADS);
        quad(buffer, left, top, right, bottom, topLeft, topRight, bottomLeft, bottomRight);
        quad(buffer, left, y, right, top, topLeft, topRight, topLeft, topRight);
        quad(buffer, left, bottom, right, y + height, bottomLeft, bottomRight, bottomLeft, bottomRight);
        quad(buffer, x, top, left, bottom, topLeft, topLeft, bottomLeft, bottomLeft);
        quad(buffer, right, top, x + width, bottom, topRight, topRight, bottomRight, bottomRight);
        end();

        float feather = feather();
        arc(0, right, top, topRadius, feather);
        arc(1, left, top, topRadius, feather);
        arc(2, left, bottom, bottomRadius, feather);
        arc(3, right, bottom, bottomRadius, feather);

        buffer = begin(GL11.GL_TRIANGLES);
        fan(buffer, right, top, 0, ARC_SEGMENTS, topRight);
        fan(buffer, left, top, ARC_POINTS, ARC_SEGMENTS, topLeft);
        fan(buffer, left, bottom, ARC_POINTS * 2, ARC_SEGMENTS, bottomLeft);
        fan(buffer, right, bottom, ARC_POINTS * 3, ARC_SEGMENTS, bottomRight);
        end();
    }

    public static void text(String text, float x, float y, int color) {
        Minecraft.getInstance().textRenderer.drawWithShadow(text, x, y, color);
    }

    public static void textRight(String text, float right, float y, int color) {
        text(text, right - Minecraft.getInstance().textRenderer.getWidth(text), y, color);
    }

    private static BufferBuilder begin(int mode) {
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        BufferBuilder buffer = Tesselator.getInstance().getBuffer();
        buffer.begin(mode, DefaultVertexFormat.POSITION_COLOR);
        return buffer;
    }

    private static void end() {
        Tesselator.getInstance().end();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
    }

    private static void quad(BufferBuilder buffer, float left, float top, float right, float bottom, int topLeft, int topRight, int bottomLeft, int bottomRight) {
        vertex(buffer, right, top, topRight);
        vertex(buffer, left, top, topLeft);
        vertex(buffer, left, bottom, bottomLeft);
        vertex(buffer, right, bottom, bottomRight);
    }

    private static float feather() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.screen == null ? 0.25F : minecraft.screen.width / (float) minecraft.width / 2.0F;
    }

    /**
     * Arc points move half a screen pixel inward and get a transparent twin half a pixel outward,
     * which fades the edge across one pixel. The two points at the ends of an arc stay put so the
     * corner still meets the straight sides exactly.
     */
    private static void arc(int quadrant, float centerX, float centerY, float radius, float feather) {
        for (int i = 0; i < ARC_POINTS; i++) {
            int point = quadrant * ARC_POINTS + i;
            float spread = i == 0 || i == ARC_SEGMENTS ? 0.0F : Math.min(feather, radius);

            EDGE_X[point] = centerX + COS[point] * (radius - spread);
            EDGE_Y[point] = centerY - SIN[point] * (radius - spread);
            FRINGE_X[point] = centerX + COS[point] * (radius + spread);
            FRINGE_Y[point] = centerY - SIN[point] * (radius + spread);
        }
    }

    private static void fan(BufferBuilder buffer, float centerX, float centerY, int first, int segments, int color) {
        int clear = color & 0xFFFFFF;

        for (int i = 0; i < segments; i++) {
            int from = (first + i) % OUTLINE_POINTS;
            int to = (first + i + 1) % OUTLINE_POINTS;

            vertex(buffer, centerX, centerY, color);
            vertex(buffer, EDGE_X[from], EDGE_Y[from], color);
            vertex(buffer, EDGE_X[to], EDGE_Y[to], color);

            if (EDGE_X[from] == FRINGE_X[from] && EDGE_Y[from] == FRINGE_Y[from]
                    && EDGE_X[to] == FRINGE_X[to] && EDGE_Y[to] == FRINGE_Y[to]) continue;

            vertex(buffer, EDGE_X[to], EDGE_Y[to], color);
            vertex(buffer, EDGE_X[from], EDGE_Y[from], color);
            vertex(buffer, FRINGE_X[from], FRINGE_Y[from], clear);

            vertex(buffer, EDGE_X[to], EDGE_Y[to], color);
            vertex(buffer, FRINGE_X[from], FRINGE_Y[from], clear);
            vertex(buffer, FRINGE_X[to], FRINGE_Y[to], clear);
        }
    }

    private static void vertex(BufferBuilder buffer, double x, double y, int color) {
        buffer.vertex(x, y, 0.0D)
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, color >>> 24)
                .nextVertex();
    }
}
