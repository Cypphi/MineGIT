package ca.modmonster.minegit.backport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

public class ImageButton extends GuiButton {
    public ImageButtonTex texture;

    public ImageButton(int buttonId, int x, int y, ImageButtonTex texture) {
        super(buttonId, x, y, 20, 20, "");
        this.texture = texture;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        super.drawButton(minecraft, mouseX, mouseY);
        minecraft.getTextureManager().bindTexture(texture.get(enabled));
        drawModalRectWithCustomSizedTexture(
                xPosition, yPosition,
                0, 0,
                20, 20,
                20, 20
        );
    }

    public static class ImageButtonTex {
        public static final ImageButtonTex CLONE = new ImageButtonTex(new ResourceLocation("minegit", "textures/gui/clone-on.png"), new ResourceLocation("minegit", "textures/gui/clone-off.png"));
        public static final ImageButtonTex CLOUD = new ImageButtonTex(new ResourceLocation("minegit", "textures/gui/cloud-on.png"), new ResourceLocation("minegit", "textures/gui/cloud-off.png"));
        public static final ImageButtonTex CHECK = new ImageButtonTex(new ResourceLocation("minegit", "textures/gui/check-off.png"), new ResourceLocation("minegit", "textures/gui/check-off.png"));
        public static final ImageButtonTex BACK = new ImageButtonTex(new ResourceLocation("minegit", "textures/gui/back-on.png"), new ResourceLocation("minegit", "textures/gui/back-off.png"));

        private final ResourceLocation enabledTex;
        private final ResourceLocation disabledTex;

        ImageButtonTex(ResourceLocation enabledTex, ResourceLocation disabledTex) {
            this.enabledTex = enabledTex;
            this.disabledTex = disabledTex;
        }

        public ResourceLocation get(boolean enabled) {
            return enabled? enabledTex : disabledTex;
        }
    }
}
