package ca.modmonster.minegit.backport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.render.texture.Texture;

public class ImageButton extends ButtonElement {
    public ImageButtonTex texture;

    public ImageButton(int buttonId, int x, int y, ImageButtonTex texture) {
        super(buttonId, x, y, 20, 20, "");
        this.texture = texture;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        super.drawButton(minecraft, mouseX, mouseY);
        texture.bind(minecraft, enabled);
        ScreenUtil.drawTexture(
                xPosition, yPosition,
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
        private Texture enabledGlid;
        private Texture disabledGlid;

        ImageButtonTex(String enabledTex, String disabledTex) {
            this.enabledTex = enabledTex;
            this.disabledTex = disabledTex;
        }

        public String get(boolean enabled) {
            return enabled? enabledTex : disabledTex;
        }

        public void bind(Minecraft minecraft, boolean enabled) {
            if (enabled) {
                if (enabledGlid == null) {
                    enabledGlid = minecraft.textureManager.loadTexture(enabledTex);
                }
                minecraft.textureManager.bindTexture(enabledGlid);
            } else {
                if (disabledGlid == null) {
                    disabledGlid = minecraft.textureManager.loadTexture(disabledTex);
                }
                minecraft.textureManager.bindTexture(disabledGlid);
            }
        }
    }
}
