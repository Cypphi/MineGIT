package ca.modmonster.minegit.mixin;

import net.minecraft.core.world.World;
import net.minecraft.core.world.save.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = World.class, remap = false)
public interface WorldAccessor {
    @Accessor("levelStorage")
    LevelStorage getStorage();
}
