package ca.modmonster.minegit.backport.toast;

import com.google.common.collect.Queues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Deque;

public class ToastManager extends GuiElement {
    public static final ToastManager INSTANCE = new ToastManager();

    private final ToastManager.ToastInstance<?>[] toasts = new ToastManager.ToastInstance[5];
    private final Deque<Toast> queue = Queues.newArrayDeque();

    public void render(Minecraft minecraft, int width) {
        if (!minecraft.options.hideGui) {
            Lighting.turnOff();

            for (int i = 0; i < this.toasts.length; i++) {
                ToastManager.ToastInstance<?> toastInstance = this.toasts[i];
                if (toastInstance != null && toastInstance.render(minecraft, width, i)) {
                    this.toasts[i] = null;
                }

                if (this.toasts[i] == null && !this.queue.isEmpty()) {
                    this.toasts[i] = new ToastManager.ToastInstance<>(this.queue.removeFirst());
                }
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
            float f = MathHelper.clamp((float)(time - this.time) / 600.0F, 0.0F, 1.0F);
            f *= f;
            return this.visibility == Toast.Visibility.HIDE ? 1.0F - f : f;
        }

        public boolean render(Minecraft minecraft, int x, int y) {
            long l = Sys.getTime() * 1000L / Sys.getTimerResolution();
            if (this.time == -1L) {
                this.time = l;
            }

            if (this.visibility == Toast.Visibility.SHOW && l - this.time <= 600L) {
                this.visibleTime = l;
            }

            GL11.glPushMatrix();
            GL11.glTranslatef(x - toast.getWidth(minecraft) * this.getVisibility(l), y * 32, 500 + y);
            Toast.Visibility visibility = this.toast.render(minecraft, ToastManager.this, l - this.visibleTime);
            GL11.glPopMatrix();
            if (visibility != this.visibility) {
                this.time = l - (int)((1.0F - this.getVisibility(l)) * 600.0F);
                this.visibility = visibility;
            }

            return this.visibility == Toast.Visibility.HIDE && l - this.time > 600L;
        }
    }
}