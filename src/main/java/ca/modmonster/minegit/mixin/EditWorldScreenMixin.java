package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.gui.PruneWorldScreen;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.gui.screen.ConfirmationListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditWorldScreen.class)
public class EditWorldScreenMixin extends Screen {
    @Shadow
    @Final
    private String worldName;

    @Shadow
    @Final
    private ConfirmationListener parent;

    @Inject(at = @At(value = "TAIL"), method = "init")
    private void init(CallbackInfo ci) {
        if (!GitManager.syncEnabled(minecraft, worldName)) return;

        // Add prune button
        addButton(new ButtonWidget(100, this.width / 2 - 100, this.height / 4 + 120 + 5, 200, 20, I18n.translate("minegit.prune.button")) {
            @Override
            public void click(double mouseX, double mouseY) {
                minecraft.openScreen(new PruneWorldScreen(EditWorldScreenMixin.this, worldName, parent));
            }
        });

        // Reposition existing buttons
        for (GuiEventListener child : this.children) {
            if (!(child instanceof ButtonWidget)) return;
            ButtonWidget button = (ButtonWidget) child;
            String message = button.message;

            if (message.equals(I18n.translate("selectWorld.edit.backup"))) {
                button.setWidth(80);
            } else if (message.equals(I18n.translate("selectWorld.edit.backupFolder"))) {
                button.setWidth(116);
                button.x = this.width / 2 - 16;
                button.y = this.height / 4 + 72 + 5;
            } else if (message.equals(I18n.translate("selectWorld.edit.optimize"))) {
                button.y = this.height / 4 + 96 + 5;
            }
        }
    }
}
