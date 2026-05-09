package ca.modmonster.minegit.mixin;

import net.minecraft.class_52;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(World.class)
public interface WorldAccessor {
    @Accessor("field_219")
    class_52 getStorage();
}
