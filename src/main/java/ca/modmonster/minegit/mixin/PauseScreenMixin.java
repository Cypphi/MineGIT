package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ScreenTooltipRenderer;
import ca.modmonster.minegit.backport.ScreenUtil;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ScreenPause;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.save.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScreenPause.class, remap = false)
public class PauseScreenMixin extends Screen {
    @Unique
    private String tooltip;

    @Unique
    private ButtonElement disconnectButton;

    @Inject(at = @At("TAIL"), method = "init")
    public void createPauseMenu(CallbackInfo ci) {
        String disconnectButtonMessage = I18n.getInstance().translateKey("gui.ingame_menu.button.save_and_quit");

        // Find the disconnect button
        for (ButtonElement button : this.buttons) {
            if (button.displayString.equals(disconnectButtonMessage)) disconnectButton = button;
        }

        tooltip = "Force quit without pushing world to GitHub";
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(int mouseX, int mouseY, float f, CallbackInfo ci) {
        if (disconnectButton == null) return;
        if (mc.isMultiplayerWorld()) return;
        if (mc.currentWorld == null) return;
        LevelStorage worldStorage = ((WorldAccessor) mc.currentWorld).getStorage();
        if (!(worldStorage instanceof AlphaWorldStorageAccessor)) return;
        if (!GitManager.syncEnabled(((AlphaWorldStorageAccessor) worldStorage).getDir().toPath())) return;
        QuitState.altQuit = ScreenUtil.isAltDown();
        if (!ScreenUtil.isAltDown()) return;

        if (ScreenUtil.isHovered(mouseX, mouseY, disconnectButton.xPosition, disconnectButton.yPosition, 200, 20)) {
            // draw red border
            renderOutline(
                    disconnectButton.xPosition,
                    disconnectButton.yPosition,
                    disconnectButton.width,
                    disconnectButton.height,
                    -65536
            );

            // draw tooltip
            ((ScreenTooltipRenderer) this).renderTooltip(tooltip, mouseX, mouseY);
        }
    }

    @Unique
    private void renderOutline(final int x, final int y, final int width, final int height, final int color) {
        drawRect(x, y, x + width, y + 1, color);
        drawRect(x, y + height - 1, x + width, y + height, color);
        drawRect(x, y + 1, x + 1, y + height - 1, color);
        drawRect(x + width - 1, y + 1, x + width, y + height - 1, color);
    }
}
