package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.gui.PruneWorldScreen;
import net.minecraft.class_341;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_341.class)
public class EditWorldScreenMixin extends Screen {
    @Shadow
    @Final
    private String field_1273;

    @Shadow
    private Screen field_1271;

    @Inject(at = @At(value = "TAIL"), method = "init")
    private void init(CallbackInfo ci) {
        if (!GitManager.syncEnabled(field_1273)) return;

        // Add prune button
        buttons.add(new ButtonWidget(100, this.width / 2 - 100, this.height / 4 + 72 + 12, 200, 20, "Prune World Commits"));
    }

    @Inject(at = @At("TAIL"), method = "buttonClicked")
    protected void buttonClicked(ButtonWidget button, CallbackInfo ci) {
        if (button.id == 100) {
            minecraft.setScreen(new PruneWorldScreen(EditWorldScreenMixin.this, field_1273, field_1271));
        }
    }
}
