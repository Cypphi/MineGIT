package ca.modmonster.minegit.mixin;

import net.minecraft.client.gui.GuiListWorldSelection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(GuiListWorldSelection.class)
public interface WorldSelectionListInvoker {
    @Invoker("func_212330_a")
    void invokeReloadWorldList(Supplier<String> supplier, boolean bl);
}