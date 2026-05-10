package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.SinglePlayerScreenExtension;
import net.minecraft.client.gui.ScreenSelectWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.ScreenSelectWorld$WorldSlot", remap = false)
public abstract class WorldListWidgetMixin {
    @Unique
    private ScreenSelectWorld selectWorldScreen = null;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void init(ScreenSelectWorld this$0, CallbackInfo ci) {
        this.selectWorldScreen = this$0;
    }

    @Inject(method = "selectItem", at = @At("TAIL"))
    private void entryClicked(int itemIndex, boolean doubleClicked, CallbackInfo ci) {
        ((SinglePlayerScreenExtension) selectWorldScreen).worldSelected(itemIndex);
    }
}
