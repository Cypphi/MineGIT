package ca.modmonster.minegit.mixin;

import net.minecraft.world.storage.AlphaWorldStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(AlphaWorldStorage.class)
public interface AlphaWorldStorageAccessor {
    @Accessor("dir")
    File getDir();
}
