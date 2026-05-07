package ca.modmonster.minegit.backport;

import net.minecraft.client.render.vertex.Tesselator;
import org.lwjgl.input.Keyboard;

public class ScreenUtil {
    public static boolean isAltDown() {
        return Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184);
    }

    public static void drawTexture(int x, int y, float u, float v, int width, int height, float scaleU, float scaleV) {
        float f = 1.0F / scaleU;
        float g = 1.0F / scaleV;
        Tesselator tesselator = Tesselator.INSTANCE;
        tesselator.begin();
        tesselator.vertex(x, y + height, 0.0, u * f, (v + height) * g);
        tesselator.vertex(x + width, y + height, 0.0, (u + width) * f, (v + height) * g);
        tesselator.vertex(x + width, y, 0.0, (u + width) * f, v * g);
        tesselator.vertex(x, y, 0.0, u * f, v * g);
        tesselator.end();
    }

    public static boolean isHovered(int i, int j, int x, int y, int buttonWidth, int buttonHeight) {
        return i >= x && j >= y && i < x + buttonWidth && j < y + buttonHeight;
    }
}
