package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ScreenTooltipRenderer;
import ca.modmonster.minegit.backport.toast.ToastManager;
import net.minecraft.class_583;
import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public class ScreenMixin extends DrawableHelper implements ScreenTooltipRenderer {
    @Shadow
    public int width;

    @Shadow
    protected TextRenderer textRenderer;

    @Shadow
    public int height;

    @Shadow
    protected Minecraft minecraft;

    @Inject(at = @At("TAIL"), method = "render")
    public void render(CallbackInfo ci) {
        ToastManager.INSTANCE.render(minecraft, width);
    }

    @Unique
    public void renderTooltip(List<String> tooltip, int x, int y) {
        if (!tooltip.isEmpty()) {
            GL11.glDisable(32826);
            class_583.method_1927();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int i = 0;

            for (String string7 : tooltip) {
                int l = this.textRenderer.getWidth(string7);
                if (l > i) {
                    i = l;
                }
            }

            int j = x + 12;
            int k = y - 12;
            int n = 8;
            if (tooltip.size() > 1) {
                n += 2 + (tooltip.size() - 1) * 10;
            }

            if (j + i > this.width) {
                j -= 28 + i;
            }

            if (k + n + 6 > this.height) {
                k = this.height - n - 6;
            }

            this.zOffset = 300.0F;
            int o = -267386864;
            this.fillGradient(j - 3, k - 4, j + i + 3, k - 3, o, o);
            this.fillGradient(j - 3, k + n + 3, j + i + 3, k + n + 4, o, o);
            this.fillGradient(j - 3, k - 3, j + i + 3, k + n + 3, o, o);
            this.fillGradient(j - 4, k - 3, j - 3, k + n + 3, o, o);
            this.fillGradient(j + i + 3, k - 3, j + i + 4, k + n + 3, o, o);
            int p = 1347420415;
            int q = (p & 16711422) >> 1 | p & 0xFF000000;
            this.fillGradient(j - 3, k - 3 + 1, j - 3 + 1, k + n + 3 - 1, p, q);
            this.fillGradient(j + i + 2, k - 3 + 1, j + i + 3, k + n + 3 - 1, p, q);
            this.fillGradient(j - 3, k - 3, j + i + 3, k - 3 + 1, p, p);
            this.fillGradient(j - 3, k + n + 2, j + i + 3, k + n + 3, q, q);

            for (int r = 0; r < tooltip.size(); r++) {
                String string16 = tooltip.get(r);
                this.textRenderer.drawWithShadow(string16, j, k, -1);
                if (r == 0) {
                    k += 2;
                }

                k += 10;
            }

            this.zOffset = 0.0F;
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            class_583.method_1930();
            GL11.glEnable(32826);
        }
    }
}
