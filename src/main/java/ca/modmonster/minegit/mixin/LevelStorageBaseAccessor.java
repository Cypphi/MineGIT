package ca.modmonster.minegit.mixin;

import net.minecraft.core.world.save.LevelStorageBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(value = LevelStorageBase.class, remap = false)
public interface LevelStorageBaseAccessor {
    @Accessor("saveDirectory")
    File getDir();
}
