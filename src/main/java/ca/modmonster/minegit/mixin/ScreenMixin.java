package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ScreenTooltipRenderer;
import ca.modmonster.minegit.backport.toast.ToastManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import org.lwjgl.opengl.GL41;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = Screen.class, remap = false)
public class ScreenMixin extends Gui implements ScreenTooltipRenderer {
    @Shadow
    public int width;

    @Shadow
    public FontRenderer fontRenderer;

    @Shadow
    public int height;

    @Final
    @Shadow
    public Minecraft mc;

    @Inject(at = @At("TAIL"), method = "render")
    public void render(int mx, int my, float partialTick, CallbackInfo ci) {
        ToastManager.INSTANCE.render(mc, width);
    }

    // Thanks to the ModMenu BTA port for everrrrrything here down
    // https://github.com/Turnip-Labs/ModMenu

    @Unique
    public void renderTooltip(List<String> tooltip, int x, int y) {
        if (!tooltip.isEmpty()) {
            FontRenderer font = this.fontRenderer;

            GLRenderer.pushFrame();
            GLRenderer.disableState(State.DEPTH_TEST);
            int k = 0;

            for (String string : tooltip) {
                int l = fontRenderer.stringWidth(string);
                if (l > k) {
                    k = l;
                }
            }

            int m = x + 12;
            int n = y - 12;
            int p = 8;
            if (tooltip.size() > 1) {
                p += 2 + (tooltip.size() - 1) * 10;
            }

            if (m + k > this.width) {
                m -= 28 + k;
            }

            if (n + p + 6 > this.height) {
                n = this.height - p - 6;
            }

            int transparentGrey = -1073741824;
            int margin = 3;
            this.fillGradient(m - margin, n - margin, m + k + margin,
                    n + p + margin, transparentGrey, transparentGrey);


            GLRenderer.modelM4f().translate(0, 0, 300);

            for(int t = 0; t < tooltip.size(); ++t) {
                String string2 = tooltip.get(t);
                if (string2 != null) {
                    this.drawStringNoShadow(fontRenderer, string2, m, n, 0xffffff);
                }

                if (t == 0) {
                    n += 2;
                }

                n += 10;
            }

            GLRenderer.popFrame();
        }
    }

    @Unique
    protected void fillGradient(int i, int j, int k, int l, int m, int n) {
        float f = (float)(m >> 24 & 255) / 255.0F;
        float g = (float)(m >> 16 & 255) / 255.0F;
        float h = (float)(m >> 8 & 255) / 255.0F;
        float o = (float)(m & 255) / 255.0F;
        float p = (float)(n >> 24 & 255) / 255.0F;
        float q = (float)(n >> 16 & 255) / 255.0F;
        float r = (float)(n >> 8 & 255) / 255.0F;
        float s = (float)(n & 255) / 255.0F;
        GLRenderer.pushFrame();
        GLRenderer.setShader(Shaders.COLOR);
        GLRenderer.enableState(State.BLEND);
        GL41.glBlendFuncSeparate(GL41.GL_SRC_ALPHA, GL41.GL_ONE_MINUS_SRC_ALPHA, GL41.GL_ONE, GL41.GL_ZERO);
        GLRenderer.setAlphaTest(0);

        TessellatorGeneral t = GLRenderer.getTessellator();
        t.startDrawingQuads();
        t.setColor4f(g, h, o, f);
        t.addVertex(k, j, 300);
        t.addVertex(i, j, 300);
        t.setColor4f(q, r, s, p);
        t.addVertex(i, l, 300);
        t.addVertex(k, l, 300);
        t.draw();

        GLRenderer.popFrame();
    }
}
