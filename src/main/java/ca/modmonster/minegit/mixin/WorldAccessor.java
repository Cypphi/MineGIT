package ca.modmonster.minegit.mixin;

import net.minecraft.world.World;
import net.minecraft.world.storage.WorldStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(World.class)
public interface WorldAccessor {
    @Accessor("storage")
    WorldStorage getStorage();
}
