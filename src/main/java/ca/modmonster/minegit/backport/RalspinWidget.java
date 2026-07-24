package ca.modmonster.minegit.backport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.texture.Texture;

public class RalspinWidget extends Gui {
    private static final String SPRITE = "/assets/minegit/textures/gui/ralspin.png";
    private static final int FRAME_WIDTH = 21;
    private static final int FRAME_HEIGHT = 40;
    private static final int FRAME_COUNT = 12;
    private static final int FRAME_TIME = 2;
    private static final int SCALE = 2;
    public static final String TOOLTIP = "hiiiii!! ^-^";
    private static Texture glid;

    public final int x;
    public final int y;
    private boolean hovered;

    public RalspinWidget(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public void render(Minecraft minecraft, int i, int j) {
        this.hovered = i >= this.x && j >= this.y && i < this.x + FRAME_WIDTH * SCALE && j < this.y + FRAME_HEIGHT * SCALE;
        long time = System.currentTimeMillis() / 50;
        int frame = (int) ((time / FRAME_TIME) % FRAME_COUNT);

        GLRenderer.pushFrame();
        GLRenderer.setShader(Shaders.INTERFACE);
        GLRenderer.enableState(State.BLEND);
        GLRenderer.setColor4f(1f, 1f, 1f, 1f);
        bindTexture(minecraft);
        ScreenUtil.drawTexture(
                x, y,
                0, frame * FRAME_HEIGHT * SCALE,
                FRAME_WIDTH * SCALE, FRAME_HEIGHT * SCALE,
                FRAME_WIDTH * SCALE, FRAME_HEIGHT * FRAME_COUNT * SCALE
        );
        GLRenderer.popFrame();
    }

    public void bindTexture(Minecraft minecraft) {
        if (glid == null) {
            glid = minecraft.textureManager.loadTexture(SPRITE);
        }
        minecraft.textureManager.bindTexture(glid);
    }

    public boolean isHovered() {
        return hovered;
    }
}
