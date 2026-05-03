package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.gui.PruneWorldScreen;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiWorldEdit.class)
public class EditWorldScreenMixin extends GuiScreen {
    @Shadow
    @Final
    private String worldId;

    @Shadow
    @Final
    private GuiYesNoCallback lastScreen;

    @Inject(at = @At(value = "TAIL"), method = "initGui")
    private void initGui(CallbackInfo ci) {
        if (!GitManager.syncEnabled(mc, worldId)) return;

        // Add prune button
        addButton(new GuiButton(children.size(), this.width / 2 - 100, this.height / 4 + 120 + 5, 200, 20, I18n.format("minegit.prune.button")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                mc.displayGuiScreen(new PruneWorldScreen(EditWorldScreenMixin.this, worldId, lastScreen));
            }
        });

        // Reposition existing buttons
        for (IGuiEventListener child : this.children) {
            if (!(child instanceof GuiButton)) return;
            GuiButton button = (GuiButton) child;
            String message = button.displayString;

            if (message.equals(I18n.format("selectWorld.edit.backup"))) {
                button.setWidth(80);
            } else if (message.equals(I18n.format("selectWorld.edit.backupFolder"))) {
                button.setWidth(116);
                button.x = this.width / 2 - 16;
                button.y = this.height / 4 + 72 + 5;
            } else if (message.equals(I18n.format("selectWorld.edit.optimize"))) {
                button.y = this.height / 4 + 96 + 5;
            }
        }
    }
}
