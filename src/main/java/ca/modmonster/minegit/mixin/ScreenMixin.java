package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.toast.ToastManager;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public class ScreenMixin {
    @Shadow
    public int width;

    @Inject(at = @At("TAIL"), method = "drawScreen")
    public void render(CallbackInfo ci) {
        ToastManager.INSTANCE.render(width);
    }
}
