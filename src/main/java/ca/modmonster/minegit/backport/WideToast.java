package ca.modmonster.minegit.backport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;

public class WideToast implements IToast, ToastWidthAccessor {
    private final String message;
    private boolean changed;
    private long lastChanged;

    public WideToast(String message) {
        this.message = message;
    }

    @Override
    public Visibility draw(GuiToast toastComponent, long l) {
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }

        toastComponent.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        this.renderBackgroundRow(getWidth());
        toastComponent.getMinecraft().fontRenderer.drawString(message, 18, 12, -256);
        return l - this.lastChanged < 5000L ? Visibility.SHOW : Visibility.HIDE;
    }

    private void renderBackgroundRow(int width) {
        int m = 20;
        int n = Math.min(60, width - m);
        GuiToast.drawModalRectWithCustomSizedTexture(0, 0, 0, 64, m, 32, 256, 256);

        for (int o = m; o < width - n; o += 64) {
            GuiToast.drawModalRectWithCustomSizedTexture(o, 0, 32, 64, Math.min(64, width - o - n), 32, 256, 256);
        }

        GuiToast.drawModalRectWithCustomSizedTexture(width - n, 0, 160 - n, 64, n, 32, 256, 256);
    }

    @Override
    public int getWidth() {
        return Math.max(Minecraft.getMinecraft().fontRenderer.getStringWidth(message) + 30, 160);
    }
}
