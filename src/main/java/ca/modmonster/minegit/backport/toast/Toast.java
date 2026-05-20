package ca.modmonster.minegit.backport.toast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class Toast {
    ResourceLocation TOASTS_LOCATION = new ResourceLocation("minegit", "textures/gui/toasts.png");
    private final String message;
    private boolean changed;
    private long lastChanged;

    public Toast(String message) {
        this.message = message;
    }

    private void renderBackgroundRow(ToastManager toastComponent, int width) {
        int m = 20;
        int n = Math.min(60, width - m);
        toastComponent.drawTexturedModalRect(0, 0, 0, 64, m, 32);

        for (int o = m; o < width - n; o += 64) {
            toastComponent.drawTexturedModalRect(o, 0, 32, 64, Math.min(64, width - o - n), 32);
        }

        toastComponent.drawTexturedModalRect(width - n, 0, 160 - n, 64, n, 32);
    }

    public int getWidth() {
        return Math.max(Minecraft.getMinecraft().fontRendererObj.getStringWidth(message) + 30, 160);
    }

    public Visibility render(ToastManager toastComponent, long l) {
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }

        toastComponent.getMinecraft().getTextureManager().bindTexture(TOASTS_LOCATION);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        this.renderBackgroundRow(toastComponent, getWidth());
        toastComponent.getMinecraft().fontRendererObj.drawString(message, 18, 12, -256);
        return l - this.lastChanged < 5000L? Visibility.SHOW : Visibility.HIDE;
    }

    public enum Visibility {
        SHOW,
        HIDE
    }
}
