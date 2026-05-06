package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.SinglePlayerScreenExtension;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screen.world.SelectWorldScreen__WorldListWidget")
public abstract class WorldListWidgetMixin {
    @Unique
    private SelectWorldScreen selectWorldScreen = null;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void init(SelectWorldScreen selectWorldScreen, CallbackInfo ci) {
        this.selectWorldScreen = selectWorldScreen;
    }

    @Inject(method = "entryClicked", at = @At("TAIL"))
    private void entryClicked(int index, boolean doubleClick, int mouseX, int mouseY, CallbackInfo ci) {
        ((SinglePlayerScreenExtension) selectWorldScreen).worldSelected(index);
    }
}
