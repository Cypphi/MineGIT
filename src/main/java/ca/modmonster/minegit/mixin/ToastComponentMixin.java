package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ToastWidthAccessor;
import net.minecraft.client.toast.Toast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.client.toast.ToastManager$Entry")
public class ToastComponentMixin {
    @Shadow
    @Final
    private Toast instance;

    @ModifyConstant(method = "draw", constant = @Constant(floatValue = 160.0f))
    private float replaceToastWidth(float original) {
        if (instance instanceof ToastWidthAccessor) {
            return (float) ((ToastWidthAccessor) instance).getWidth();
        }
        return original;
    }
}