package ca.modmonster.minegit.mixin;

import net.minecraft.class_81;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(class_81.class)
public interface AlphaWorldStorageAccessor {
    @Accessor("field_279")
    File getDir();
}
