package ca.modmonster.minegit.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(WorldSelectionList.class)
public interface WorldSelectionListInvoker {
    @Invoker("refreshList")
    void invokeReloadWorldList(Supplier<String> supplier, boolean bl);
}
