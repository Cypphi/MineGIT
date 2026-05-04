package ca.modmonster.minegit.backport;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.resource.Identifier;

public class RalspinWidget extends ButtonWidget {
    private static final Identifier SPRITE = new Identifier("minegit", "textures/gui/ralspin.png");
    private static final int FRAME_WIDTH = 21;
    private static final int FRAME_HEIGHT = 40;
    private static final int FRAME_COUNT = 12;
    private static final int FRAME_TIME = 2;
    private static final int SCALE = 2;
    public static final String TOOLTIP = "hiiiii!! ^-^";

    public RalspinWidget(final int x, final int y) {
        super(x, y, FRAME_WIDTH * SCALE, FRAME_HEIGHT * SCALE, "");
    }

    @Override
    public void render(int i, int j, float f) {
        super.render(i, j, f);
        long time = System.currentTimeMillis() / 50;
        int frame = (int) ((time / FRAME_TIME) % FRAME_COUNT);

        int u = 0;
        int v = frame * FRAME_HEIGHT;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(SCALE, SCALE, 1);
        GlStateManager.color(1F, 1F, 1F, 1F);
        Minecraft.getInstance().getTextureManager().bind(SPRITE);
        drawTexture(
                0, 0,
                u, v,
                FRAME_WIDTH, FRAME_HEIGHT,
                FRAME_WIDTH, FRAME_HEIGHT * FRAME_COUNT
        );
        GlStateManager.popMatrix();
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float tickDelta) {

    }
}
