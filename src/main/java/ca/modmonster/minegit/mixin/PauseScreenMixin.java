package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ScreenTooltipRenderer;
import ca.modmonster.minegit.backport.ScreenUtil;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import net.minecraft.class_52;
import net.minecraft.class_525;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_525.class)
public class PauseScreenMixin extends Screen {
    @Unique
    private String tooltip;

    @Unique
    private ButtonWidget disconnectButton;

    @Inject(at = @At("TAIL"), method = "init")
    public void createPauseMenu(CallbackInfo ci) {
        // Find the disconnect button
        for (Object o : this.buttons) {
            if (!(o instanceof ButtonWidget)) continue;
            ButtonWidget button = (ButtonWidget) o;
            if (button.text.equals("Save and quit to title")) disconnectButton = button;
        }

        tooltip = "Force quit without pushing world to GitHub";
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(int mouseX, int mouseY, float f, CallbackInfo ci) {
        if (disconnectButton == null) return;
        if (minecraft.isWorldRemote()) return;
        if (minecraft.world == null) return;
        class_52 worldStorage = ((WorldAccessor) minecraft.world).getStorage();
        if (!(worldStorage instanceof AlphaWorldStorageAccessor)) return;
        if (!GitManager.syncEnabled(((AlphaWorldStorageAccessor) worldStorage).getDir().toPath())) return;
        QuitState.altQuit = ScreenUtil.isAltDown();
        if (!ScreenUtil.isAltDown()) return;

        if (ScreenUtil.isHovered(mouseX, mouseY, disconnectButton.x, disconnectButton.y, 200, 20)) {
            // draw red border
            renderOutline(
                    disconnectButton.x,
                    disconnectButton.y,
                    200,
                    20,
                    -65536
            );

            // draw tooltip
            ((ScreenTooltipRenderer) this).renderTooltip(tooltip, mouseX, mouseY);
        }
    }

    @Unique
    private void renderOutline(final int x, final int y, final int width, final int height, final int color) {
        fill(x, y, x + width, y + 1, color);
        fill(x, y + height - 1, x + width, y + height, color);
        fill(x, y + 1, x + 1, y + height - 1, color);
        fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }
}
