package ca.modmonster.minegit.mixin;

import net.minecraft.core.world.save.SaveHandlerBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(value = SaveHandlerBase.class, remap = false)
public interface AlphaWorldStorageAccessor {
    @Accessor("saveDirectory")
    File getDir();
}
