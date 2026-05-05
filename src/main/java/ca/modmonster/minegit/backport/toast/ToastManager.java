package ca.modmonster.minegit.backport.toast;

import com.google.common.collect.Queues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Deque;

public class ToastManager extends GuiElement {
    public static final ToastManager INSTANCE = new ToastManager(Minecraft.getInstance());

    private final Minecraft minecraft;
    private final ToastManager.ToastInstance<?>[] toasts = new ToastManager.ToastInstance[5];
    private final Deque<Toast> queue = Queues.newArrayDeque();

    public ToastManager(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(int width) {
        if (!this.minecraft.options.hideGui) {
            Lighting.turnOff();

            for (int i = 0; i < this.toasts.length; i++) {
                ToastManager.ToastInstance<?> toastInstance = this.toasts[i];
                if (toastInstance != null && toastInstance.render(width, i)) {
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

    public Minecraft getMinecraft() {
        return this.minecraft;
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

        public boolean render(int x, int y) {
            long l = Minecraft.getTime();
            if (this.time == -1L) {
                this.time = l;
                this.visibility.playSound(ToastManager.this.minecraft.getSoundManager());
            }

            if (this.visibility == Toast.Visibility.SHOW && l - this.time <= 600L) {
                this.visibleTime = l;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translatef(x - toast.getWidth() * this.getVisibility(l), y * 32, 500 + y);
            Toast.Visibility visibility = this.toast.render(ToastManager.this, l - this.visibleTime);
            GlStateManager.popMatrix();
            if (visibility != this.visibility) {
                this.time = l - (int)((1.0F - this.getVisibility(l)) * 600.0F);
                this.visibility = visibility;
                this.visibility.playSound(ToastManager.this.minecraft.getSoundManager());
            }

            return this.visibility == Toast.Visibility.HIDE && l - this.time > 600L;
        }
    }
}