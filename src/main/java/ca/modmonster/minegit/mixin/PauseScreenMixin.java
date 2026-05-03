package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameMenu.class)
public class PauseScreenMixin extends GuiScreen {
    @Unique
    private String mineGIT$tooltip;

    @Unique
    private GuiButton mineGIT$disconnectButton;

    @Inject(at = @At("TAIL"), method = "initGui", remap = false)
    protected void initGui(CallbackInfo ci) {
        // Find the disconnect button
        for (IGuiEventListener child : this.children) {
            if (!(child instanceof GuiButton)) return;
            GuiButton button = (GuiButton) child;
            if (button.displayString.equals(I18n.format("menu.returnToMenu"))) mineGIT$disconnectButton = button;
        }

        mineGIT$tooltip = I18n.format("minegit.exit_without_push");
    }

    @Inject(at = @At("TAIL"), method = "render", remap = false)
    private void render(int mouseX, int mouseY, float f, CallbackInfo ci) {
        if (mineGIT$disconnectButton == null) return;
        if (!mc.isSingleplayer()) return;
        IntegratedServer server = mc.getIntegratedServer();
        if (server == null) return;
        if (!GitManager.syncEnabled(mc, server.getFolderName())) return;
        if (!QuitState.altQuit) return;

        if (mineGIT$disconnectButton.isMouseOver()) {
            // draw red border
            renderOutline(
                    mineGIT$disconnectButton.x,
                    mineGIT$disconnectButton.y,
                    mineGIT$disconnectButton.getWidth(),
                    20,
                    -65536
            );

            // draw tooltip
            drawHoveringText(mineGIT$tooltip, mouseX, mouseY);
        }
    }

    @Unique
    private void renderOutline(final int x, final int y, final int width, final int height, final int color) {
        Gui.drawRect(x, y, x + width, y + 1, color);
        Gui.drawRect(x, y + height - 1, x + width, y + height, color);
        Gui.drawRect(x, y + 1, x + 1, y + height - 1, color);
        Gui.drawRect(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 342) QuitState.altQuit = true;
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        if (i == 342) QuitState.altQuit = false;
        return super.keyReleased(i, j, k);
    }
}
