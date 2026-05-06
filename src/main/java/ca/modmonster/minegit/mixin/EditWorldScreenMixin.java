package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.gui.PruneWorldScreen;
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
    private Screen parent;

    @Inject(at = @At(value = "TAIL"), method = "init")
    private void init(CallbackInfo ci) {
        if (!GitManager.syncEnabled(minecraft, worldName)) return;

        // Add prune button
        buttons.add(new ButtonWidget(100, this.width / 2 - 100, this.height / 4 + 72 + 12, 200, 20, I18n.translate("minegit.prune.button")));
    }

    @Inject(at = @At("TAIL"), method = "buttonClicked")
    protected void buttonClicked(ButtonWidget button, CallbackInfo ci) {
        if (button.id == 100) {
            minecraft.openScreen(new PruneWorldScreen(EditWorldScreenMixin.this, worldName, parent));
        }
    }
}
