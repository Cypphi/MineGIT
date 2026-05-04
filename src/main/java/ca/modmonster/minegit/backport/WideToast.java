package ca.modmonster.minegit.backport;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;

public class WideToast implements Toast, ToastWidthAccessor {
    private final String message;
    private boolean changed;
    private long lastChanged;

    public WideToast(String message) {
        this.message = message;
    }

    @Override
    public Visibility draw(ToastManager toastComponent, long l) {
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }

        toastComponent.getGame().getTextureManager().bindTexture(Toast.TOASTS_TEX);
        GlStateManager.color3f(1.0F, 1.0F, 1.0F);
        this.renderBackgroundRow(toastComponent, getWidth());
        toastComponent.getGame().textRenderer.draw(message, 18.0F, 12.0F, -256);
        return l - this.lastChanged < 5000L ? Visibility.SHOW : Visibility.HIDE;
    }

    private void renderBackgroundRow(ToastManager toastComponent, int width) {
        int m = 20;
        int n = Math.min(60, width - m);
        toastComponent.blit(0, 0, 0, 64, m, 32);

        for (int o = m; o < width - n; o += 64) {
            toastComponent.blit(o, 0, 32, 64, Math.min(64, width - o - n), 32);
        }

        toastComponent.blit(width - n, 0, 160 - n, 64, n, 32);
    }

    @Override
    public int getWidth() {
        return Math.max(MinecraftClient.getInstance().textRenderer.getStringWidth(message) + 30, 160);
    }
}
