package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(GuiIngameMenu.class)
public class PauseScreenMixin extends GuiScreen {
    @Unique
    private String tooltip;

    @Unique
    private GuiButton disconnectButton;

    @Inject(at = @At("TAIL"), method = "initGui")
    protected void createPauseMenu(CallbackInfo ci) {
        // Find the disconnect button
        for (GuiButton button : this.buttonList) {
            if (button.displayString.equals(I18n.format("menu.returnToMenu"))) disconnectButton = button;
        }

        tooltip = I18n.format("minegit.exit_without_push");
    }

    @Inject(at = @At("TAIL"), method = "drawScreen")
    private void render(int mouseX, int mouseY, float f, CallbackInfo ci) {
        if (disconnectButton == null) return;
        if (!mc.isSingleplayer()) return;
        IntegratedServer server = mc.getIntegratedServer();
        if (server == null) return;
        if (!GitManager.syncEnabled(mc, server.getFolderName())) return;
        QuitState.altQuit = isAltKeyDown();
        if (!isAltKeyDown()) return;

        if (disconnectButton.isMouseOver()) {
            // draw red border
            renderOutline(
                    disconnectButton.xPosition,
                    disconnectButton.yPosition,
                    disconnectButton.getButtonWidth(),
                    20,
                    -65536
            );

            // draw tooltip
            drawHoveringText(Collections.singletonList(tooltip), mouseX, mouseY);
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
