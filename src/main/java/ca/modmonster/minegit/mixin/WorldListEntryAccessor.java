package ca.modmonster.minegit.mixin;

import net.minecraft.client.gui.screen.world.WorldSelectionList;
import net.minecraft.world.storage.WorldSaveInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldSelectionList.C_13896933.class)
public interface WorldListEntryAccessor {
    @Accessor("f_65569109")
    WorldSaveInfo getSummary();
}
