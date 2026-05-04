package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ToastWidthAccessor;
import net.minecraft.client.gui.toast.Toast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.client.gui.toast.ToastGui$ToastInstance")
public class ToastComponentMixin {
    @Shadow
    @Final
    private Toast toast;

    @ModifyConstant(method = "render", constant = @Constant(floatValue = 160.0f))
    private float replaceToastWidth(float original) {
        if (toast instanceof ToastWidthAccessor) {
            return (float) ((ToastWidthAccessor) toast).getWidth();
        }
        return original;
    }
}