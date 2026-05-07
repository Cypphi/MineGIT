package ca.modmonster.minegit.backport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.Identifier;

public class ImageButton extends ButtonWidget {
    public ImageButtonTex texture;

    public ImageButton(int buttonId, int x, int y, ImageButtonTex texture) {
        super(buttonId, x, y, 20, 20, "");
        this.texture = texture;
    }

    @Override
    public void render(Minecraft minecraft, int mouseX, int mouseY) {
        super.render(minecraft, mouseX, mouseY);
        minecraft.getTextureManager().bind(texture.get(active));
        ScreenUtil.drawTexture(
                x, y,
                0, 0,
                20, 20,
                20, 20
        );
    }

    public static class ImageButtonTex {
        public static final ImageButtonTex CLONE = new ImageButtonTex(new Identifier("minegit", "textures/gui/clone-on.png"), new Identifier("minegit", "textures/gui/clone-off.png"));
        public static final ImageButtonTex CLOUD = new ImageButtonTex(new Identifier("minegit", "textures/gui/cloud-on.png"), new Identifier("minegit", "textures/gui/cloud-off.png"));
        public static final ImageButtonTex CHECK = new ImageButtonTex(new Identifier("minegit", "textures/gui/check-off.png"), new Identifier("minegit", "textures/gui/check-off.png"));
        public static final ImageButtonTex BACK = new ImageButtonTex(new Identifier("minegit", "textures/gui/back-on.png"), new Identifier("minegit", "textures/gui/back-off.png"));

        private final Identifier enabledTex;
        private final Identifier disabledTex;

        ImageButtonTex(Identifier enabledTex, Identifier disabledTex) {
            this.enabledTex = enabledTex;
            this.disabledTex = disabledTex;
        }

        public Identifier get(boolean enabled) {
            return enabled? enabledTex : disabledTex;
        }
    }
}
