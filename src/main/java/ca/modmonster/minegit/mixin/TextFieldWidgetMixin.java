package ca.modmonster.minegit.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextFieldWidget.class)
public class TextFieldWidgetMixin {
    @Shadow
    @Final
    private int width;

    @Shadow
    private int maxLength;

    @Redirect(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;drawStringWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V"
    ))
    public void redirectDrawString(TextFieldWidget instance, TextRenderer textRenderer, String text, int x, int y, int color) {
        String t = "";
        if (textRenderer.getWidth(text) < width - 8) {
            t = text;
        } else {
            int i = 0;
            while (textRenderer.getWidth(t) < width - 8) {
                t += text.charAt(i);
                i++;
            }
        }

        textRenderer.drawWithShadow(t, x, y, color);
    }

    @ModifyConstant(method = "keyPressed", constant = @Constant(intValue = 32))
    private int changeLimit(int original) {
        return maxLength;
    }
}
