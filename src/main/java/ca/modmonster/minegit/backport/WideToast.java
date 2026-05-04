package ca.modmonster.minegit.backport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toast.Toast;
import net.minecraft.client.gui.toast.ToastGui;
import net.minecraft.client.render.platform.GlStateManager;

public class WideToast implements Toast, ToastWidthAccessor {
    private final String message;
    private boolean changed;
    private long lastChanged;

    public WideToast(String message) {
        this.message = message;
    }

    @Override
    public Visibility render(ToastGui toastComponent, long l) {
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }

        toastComponent.getMinecraft().getTextureManager().bind(Toast.TOASTS_LOCATION);
        GlStateManager.color3f(1.0F, 1.0F, 1.0F);
        this.renderBackgroundRow(toastComponent, getWidth());
        toastComponent.getMinecraft().textRenderer.draw(message, 18.0F, 12.0F, -256);
        return l - this.lastChanged < 5000L ? Visibility.SHOW : Visibility.HIDE;
    }

    private void renderBackgroundRow(ToastGui toastComponent, int width) {
        int m = 20;
        int n = Math.min(60, width - m);
        toastComponent.drawTexture(0, 0, 0, 64, m, 32);

        for (int o = m; o < width - n; o += 64) {
            toastComponent.drawTexture(o, 0, 32, 64, Math.min(64, width - o - n), 32);
        }

        toastComponent.drawTexture(width - n, 0, 160 - n, 64, n, 32);
    }

    @Override
    public int getWidth() {
        return Math.max(Minecraft.getInstance().textRenderer.getWidth(message) + 30, 160);
    }
}
