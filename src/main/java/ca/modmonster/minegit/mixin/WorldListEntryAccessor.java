package ca.modmonster.minegit.mixin;

import net.minecraft.client.gui.GuiListWorldSelectionEntry;
import net.minecraft.world.storage.WorldSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiListWorldSelectionEntry.class)
public interface WorldListEntryAccessor {
    @Accessor("worldSummary")
    WorldSummary getSummary();
}
