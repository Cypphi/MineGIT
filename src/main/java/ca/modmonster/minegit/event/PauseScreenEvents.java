package ca.modmonster.minegit.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class PauseScreenEvents {
    private static String tooltip;
    private static GuiButton disconnectButton;

    @SubscribeEvent
    public static void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof GuiIngameMenu)) return;
        // Find the disconnect button
        for (IGuiEventListener child : event.getGui().getChildren()) {
            if (!(child instanceof GuiButton)) return;
            GuiButton button = (GuiButton) child;
            if (button.displayString.equals(I18n.format("menu.returnToMenu"))) disconnectButton = button;
        }

        tooltip = I18n.format("minegit.exit_without_push");
    }

    @SubscribeEvent
    public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof GuiIngameMenu)) return;
        GuiIngameMenu gui = (GuiIngameMenu) event.getGui();
        Minecraft minecraft = gui.mc;
        if (disconnectButton == null) return;
        if (!minecraft.isSingleplayer()) return;
        IntegratedServer server = minecraft.getIntegratedServer();
        if (server == null) return;
        if (!GitManager.syncEnabled(minecraft, server.getFolderName())) return;
        if (!QuitState.altQuit) return;

        if (disconnectButton.isMouseOver()) {
            // draw red border
            renderOutline(
                    disconnectButton.x,
                    disconnectButton.y,
                    disconnectButton.getWidth(),
                    20,
                    -65536
            );

            // draw tooltip
            gui.drawHoveringText(tooltip, event.getMouseX(), event.getMouseY());
        }
    }

    private static void renderOutline(final int x, final int y, final int width, final int height, final int color) {
        Gui.drawRect(x, y, x + width, y + 1, color);
        Gui.drawRect(x, y + height - 1, x + width, y + height, color);
        Gui.drawRect(x, y + 1, x + 1, y + height - 1, color);
        Gui.drawRect(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    @SubscribeEvent
    public static void keyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        if (!(event.getGui() instanceof GuiIngameMenu)) return;
        if (event.getKeyCode() == 342) QuitState.altQuit = true;
    }

    @SubscribeEvent
    public static void keyReleased(GuiScreenEvent.KeyboardKeyReleasedEvent.Pre event) {
        if (!(event.getGui() instanceof GuiIngameMenu)) return;
        if (event.getKeyCode() == 342) QuitState.altQuit = false;
    }
}
