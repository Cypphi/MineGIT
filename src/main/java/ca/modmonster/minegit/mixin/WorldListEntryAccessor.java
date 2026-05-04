package ca.modmonster.minegit.mixin;

import net.minecraft.client.gui.screen.world.WorldSelectionEntry;
import net.minecraft.world.storage.WorldSaveInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldSelectionEntry.class)
public interface WorldListEntryAccessor {
    @Accessor("info")
    WorldSaveInfo getSummary();
}
