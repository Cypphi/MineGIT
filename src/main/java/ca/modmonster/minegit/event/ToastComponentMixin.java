package ca.modmonster.minegit.event;

//@Mixin(targets = "net.minecraft.client.gui.components.toasts.ToastComponent$ToastInstance")
//public class ToastComponentMixin {
//    @Shadow
//    @Final
//    private Toast toast;
//
//    @ModifyConstant(method = "render", constant = @Constant(floatValue = 160.0f))
//    private float replaceToastWidth(float original) {
//        if (toast instanceof ToastWidthAccessor) {
//            return (float) ((ToastWidthAccessor) toast).getWidth();
//        }
//        return original;
//    }
//}