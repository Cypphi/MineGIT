package ca.modmonster.minegit.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.gui.AccountLinkScreen;
import ca.modmonster.minegit.gui.CloneScreen;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class CreateWorldScreenEvents {
    private static GuiButton gitButton;
    private static String gitButtonTooltip;
    private static boolean needsSetup = false;

    @SubscribeEvent
    public static void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof GuiCreateWorld)) return;
        GuiCreateWorld gui = (GuiCreateWorld) event.getGui();
        checkNeedsSetup();

        if (needsSetup) {
            // Add setup button
            gitButton = new GuiButton(gui.getChildren().size(), gui.width / 2 - 178, gui.height - 28, 20, 20, "☁") {
                @Override
                public void onClick(double mouseX, double mouseY) {
                    onGitButtonPress(gui.mc, gui);
                }
            };
            gitButtonTooltip = I18n.format("minegit.link.setup");
        } else {
            // Add clone button
            gitButton = new GuiButton(gui.getChildren().size(), gui.width / 2 - 178, gui.height - 28, 20, 20, "↓") {
                @Override
                public void onClick(double mouseX, double mouseY) {
                    onGitButtonPress(gui.mc, gui);
                }
            };
            gitButtonTooltip = I18n.format("minegit.clone.title");
        }
        event.addButton(gitButton);
    }

    @SubscribeEvent
    public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof GuiCreateWorld)) return;
        if (gitButton != null && gitButton.isMouseOver()) {
            event.getGui().drawHoveringText(gitButtonTooltip, event.getMouseX(), event.getMouseY());
        }
    }

    private static void onGitButtonPress(Minecraft minecraft, GuiScreen createWorldScreen) {
        if (needsSetup) {
            minecraft.displayGuiScreen(new AccountLinkScreen(createWorldScreen, CreateWorldScreenEvents::updateSetupButton));
        } else {
            minecraft.displayGuiScreen(
                    new CloneScreen(() -> minecraft.displayGuiScreen(createWorldScreen), () ->
                            minecraft.displayGuiScreen(new GuiWorldSelection(new GuiMainMenu()))));
        }
    }

    private static void checkNeedsSetup() {
        Config config = ConfigManager.getCurrentConfig();
        needsSetup = config.username.replace(" ", "").isEmpty() || config.getPat().replace(" ", "").isEmpty();
    }

    private static void updateSetupButton() {
        if (gitButton == null) return;
        checkNeedsSetup();
        if (needsSetup) {
            gitButton.displayString = "☁";
            gitButtonTooltip = I18n.format("minegit.link.setup");
        } else {
            gitButton.displayString = "↓";
            gitButtonTooltip = I18n.format("minegit.clone.title");
        }
    }
}
