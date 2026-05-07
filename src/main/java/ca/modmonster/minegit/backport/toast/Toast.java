package ca.modmonster.minegit.backport.toast;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class Toast {
    String TOASTS_LOCATION = "/assets/minegit/textures/gui/toasts.png";
    private final String message;
    private boolean changed;
    private long lastChanged;

    public Toast(String message) {
        this.message = message;
    }

    private void renderBackgroundRow(ToastManager toastComponent, int width) {
        int m = 20;
        int n = Math.min(60, width - m);
        toastComponent.drawTexture(0, 0, 0, 64, m, 32);

        for (int o = m; o < width - n; o += 64) {
            toastComponent.drawTexture(o, 0, 32, 64, Math.min(64, width - o - n), 32);
        }

        toastComponent.drawTexture(width - n, 0, 160 - n, 64, n, 32);
    }

    public int getWidth() {
        return Math.max(Minecraft.getInstance().textRenderer.getWidth(message) + 30, 160);
    }

    public Visibility render(ToastManager toastComponent, long l) {
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }

        toastComponent.getMinecraft().textureManager.bind(TOASTS_LOCATION);
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        this.renderBackgroundRow(toastComponent, getWidth());
        toastComponent.getMinecraft().textRenderer.draw(message, 18, 12, -256);
        return l - this.lastChanged < 5000L? Visibility.SHOW : Visibility.HIDE;
    }

    @Environment(EnvType.CLIENT)
    public enum Visibility {
        SHOW,
        HIDE
    }
}
