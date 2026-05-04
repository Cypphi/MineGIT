package ca.modmonster.minegit.mixin;

import net.minecraft.client.gui.screen.world.WorldListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(WorldListWidget.class)
public interface WorldSelectionListInvoker {
    @Invoker("filter")
    void invokeReloadWorldList(Supplier<String> supplier, boolean bl);
}