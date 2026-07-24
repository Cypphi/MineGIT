package ca.modmonster.minegit.backport.toast;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.texture.Texture;

public class Toast {
    public static final String TOASTS_LOCATION = "/assets/minegit/textures/gui/toasts.png";
    private static Texture glid;
    private final String message;
    private boolean changed;
    private long lastChanged;

    public Toast(String message) {
        this.message = message;
    }

    private void renderBackgroundRow(ToastManager toastComponent, int width, int x, int y) {
        int m = 20;
        int n = Math.min(60, width - m);
        toastComponent.drawTexturedModalRect(x, y, 0, 64, m, 32);

        for (int o = m; o < width - n; o += 64) {
            toastComponent.drawTexturedModalRect(x + o, y, 32, 64, Math.min(64, width - o - n), 32);
        }

        toastComponent.drawTexturedModalRect(x + width - n, y, 160 - n, 64, n, 32);
    }

    public int getWidth(Minecraft minecraft) {
        return Math.max(minecraft.font.stringWidth(message) + 30, 160);
    }

    public Visibility render(Minecraft minecraft, ToastManager toastComponent, long l, int x, int y, Gui gui) {
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }

        bindTexture(minecraft);
        GLRenderer.setColor3f(1.0F, 1.0F, 1.0F);
        this.renderBackgroundRow(toastComponent, getWidth(minecraft), x, y);
        gui.drawStringNoShadow(minecraft.font, message, x + 18, y + 12, -256);
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
