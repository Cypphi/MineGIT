package ca.modmonster.minegit.event;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiWorldEdit;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.gui.PruneWorldScreen;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class EditWorldScreenEvents {
    @SubscribeEvent
    public static void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof GuiWorldEdit)) return;
        GuiWorldEdit gui = (GuiWorldEdit) event.getGui();
        String levelId = ObfuscationReflectionHelper.getPrivateValue(GuiWorldEdit.class, gui, "worldId");
        GuiYesNoCallback callback = ObfuscationReflectionHelper.getPrivateValue(GuiWorldEdit.class, gui, "lastScreen");
        if (!GitManager.syncEnabled(gui.mc, levelId)) return;

        // Add prune button
        event.addButton(new GuiButton(gui.getChildren().size(), gui.width / 2 - 100, gui.height / 4 + 120 + 5, 200, 20, I18n.format("minegit.prune.button")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                gui.mc.displayGuiScreen(new PruneWorldScreen(gui, levelId, callback));
            }
        });

        // Reposition existing buttons
        for (IGuiEventListener child : gui.getChildren()) {
            if (!(child instanceof GuiButton)) return;
            GuiButton button = (GuiButton) child;
            String message = button.displayString;

            if (message.equals(I18n.format("selectWorld.edit.backup"))) {
                button.setWidth(80);
            } else if (message.equals(I18n.format("selectWorld.edit.backupFolder"))) {
                button.setWidth(116);
                button.x = gui.width / 2 - 16;
                button.y = gui.height / 4 + 72 + 5;
            } else if (message.equals(I18n.format("selectWorld.edit.optimize"))) {
                button.y = gui.height / 4 + 96 + 5;
            }
        }
    }
}
