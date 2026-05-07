package ca.modmonster.minegit.backport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;

public class ImageButton extends ButtonWidget {
    public ImageButtonTex texture;

    public ImageButton(int buttonId, int x, int y, ImageButtonTex texture) {
        super(buttonId, x, y, 20, 20, "");
        this.texture = texture;
    }

    @Override
    public void render(Minecraft minecraft, int mouseX, int mouseY) {
        super.render(minecraft, mouseX, mouseY);
        minecraft.textureManager.bind(texture.get(active));
        ScreenUtil.drawTexture(
                x, y,
                0, 0,
                20, 20,
                20, 20
        );
    }

    public static class ImageButtonTex {
        public static final ImageButtonTex CLONE = new ImageButtonTex("/assets/minegit/textures/gui/clone-on.png", "/assets/minegit/textures/gui/clone-off.png");
        public static final ImageButtonTex CLOUD = new ImageButtonTex("/assets/minegit/textures/gui/cloud-on.png", "/assets/minegit/textures/gui/cloud-off.png");
        public static final ImageButtonTex CHECK = new ImageButtonTex("/assets/minegit/textures/gui/check-off.png", "/assets/minegit/textures/gui/check-off.png");
        public static final ImageButtonTex BACK = new ImageButtonTex("/assets/minegit/textures/gui/back-on.png", "/assets/minegit/textures/gui/back-off.png");

        private final String enabledTex;
        private final String disabledTex;

        ImageButtonTex(String enabledTex, String disabledTex) {
            this.enabledTex = enabledTex;
            this.disabledTex = disabledTex;
        }

        public String get(boolean enabled) {
            return enabled? enabledTex : disabledTex;
        }
    }
}
