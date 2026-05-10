package ca.modmonster.minegit.backport.toast;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.texture.Texture;
import org.lwjgl.opengl.GL11;

public class Toast {
    public static final String TOASTS_LOCATION = "/assets/minegit/textures/gui/toasts.png";
    private static Texture glid;
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

    public int getWidth(Minecraft minecraft) {
        return Math.max(minecraft.font.getStringWidth(message) + 30, 160);
    }

    public Visibility render(Minecraft minecraft, ToastManager toastComponent, long l) {
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }

        bindTexture(minecraft);
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        this.renderBackgroundRow(toastComponent, getWidth(minecraft));
        minecraft.font.drawString(message, 18, 12, -256);
        return l - this.lastChanged < 5000L? Visibility.SHOW : Visibility.HIDE;
    }

    public void bindTexture(Minecraft minecraft) {
        if (glid == null) {
            glid = minecraft.textureManager.loadTexture(TOASTS_LOCATION);
        }
        minecraft.textureManager.bindTexture(glid);
    }

    @Environment(EnvType.CLIENT)
    public enum Visibility {
        SHOW,
        HIDE
    }
}
