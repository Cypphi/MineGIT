package ca.modmonster.minegit.mixin;

import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(SystemToast.class)
public interface SystemToastAccessor {
    @Invoker("<init>")
    static SystemToast create(SystemToast.SystemToastIds systemToastIds, Component component, List<FormattedText> list, int i) {return null;}
}
