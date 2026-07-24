package ca.modmonster.minegit.backport.toast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.render.Lighting;
import net.minecraft.client.render.renderer.GLRenderer;
import org.lwjgl.Sys;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class ToastManager extends Gui {
    public static final ToastManager INSTANCE = new ToastManager();

    private final ToastManager.ToastInstance<?>[] toasts = new ToastManager.ToastInstance[5];
    private final Deque<Toast> queue = new ArrayDeque<>();

    public void render(Minecraft minecraft, int width) {
        // Turn off lighting
        Lighting.disable();

        for (int i = 0; i < this.toasts.length; i++) {
            ToastManager.ToastInstance<?> toastInstance = this.toasts[i];
            if (toastInstance != null && toastInstance.render(minecraft, width, i, this)) {
                this.toasts[i] = null;
            }

            if (this.toasts[i] == null && !this.queue.isEmpty()) {
                this.toasts[i] = new ToastManager.ToastInstance<>(this.queue.removeFirst());
            }
        }
    }

    public void clear() {
        Arrays.fill(this.toasts, null);
        this.queue.clear();
    }

    public void add(Toast toast) {
        this.queue.add(toast);
    }

    class ToastInstance<T extends Toast> {
        private final T toast;
        private long time = -1L;
        private long visibleTime = -1L;
        private Toast.Visibility visibility = Toast.Visibility.SHOW;

        private ToastInstance(T toast) {
            this.toast = toast;
        }

        public T getToast() {
            return this.toast;
        }

        private float getVisibility(long time) {
            float f = clamp((float)(time - this.time) / 600.0F, 0.0F, 1.0F);
            f *= f;
            return this.visibility == Toast.Visibility.HIDE ? 1.0F - f : f;
        }

        private float clamp(float x, float min, float max) {
            if (x > max) return max;
            if (x < min) return min;
            return x;
        }

        public boolean render(Minecraft minecraft, int x, int y, Gui gui) {
            long l = Sys.getTime() * 1000L / Sys.getTimerResolution();
            if (this.time == -1L) {
                this.time = l;
            }

            if (this.visibility == Toast.Visibility.SHOW && l - this.time <= 600L) {
                this.visibleTime = l;
            }

            GLRenderer.pushFrame();
            Toast.Visibility visibility = this.toast.render(minecraft, ToastManager.this, l - this.visibleTime, (int) (x - toast.getWidth(minecraft) * this.getVisibility(l)), y * 32, gui);
            GLRenderer.popFrame();
            if (visibility != this.visibility) {
                this.time = l - (int)((1.0F - this.getVisibility(l)) * 600.0F);
                this.visibility = visibility;
            }

            return this.visibility == Toast.Visibility.HIDE && l - this.time > 600L;
        }
    }
}