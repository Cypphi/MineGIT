package ca.modmonster.minegit.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldSelectionList.class)
public interface WorldSelectionListInvoker {
    @Invoker("reloadWorldList")
    void invokeReloadWorldList();
}
