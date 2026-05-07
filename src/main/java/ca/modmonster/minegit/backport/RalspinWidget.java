package ca.modmonster.minegit.backport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.resource.Identifier;
import org.lwjgl.opengl.GL11;

public class RalspinWidget extends GuiElement {
    private static final Identifier SPRITE = new Identifier("minegit", "textures/gui/ralspin.png");
    private static final int FRAME_WIDTH = 21;
    private static final int FRAME_HEIGHT = 40;
    private static final int FRAME_COUNT = 12;
    private static final int FRAME_TIME = 2;
    private static final int SCALE = 2;
    public static final String TOOLTIP = "hiiiii!! ^-^";

    public final int x;
    public final int y;
    private boolean hovered;

    public RalspinWidget(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public void render(int i, int j) {
        this.hovered = i >= this.x && j >= this.y && i < this.x + FRAME_WIDTH * SCALE && j < this.y + FRAME_HEIGHT * SCALE;
        long time = System.currentTimeMillis() / 50;
        int frame = (int) ((time / FRAME_TIME) % FRAME_COUNT);

        int u = 0;
        int v = frame * FRAME_HEIGHT;

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(SCALE, SCALE, 1);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        Minecraft.getInstance().getTextureManager().bind(SPRITE);
        ScreenUtil.drawTexture(
                0, 0,
                u, v,
                FRAME_WIDTH, FRAME_HEIGHT,
                FRAME_WIDTH, FRAME_HEIGHT * FRAME_COUNT
        );
        GL11.glPopMatrix();
    }

    public boolean isHovered() {
        return hovered;
    }
}
