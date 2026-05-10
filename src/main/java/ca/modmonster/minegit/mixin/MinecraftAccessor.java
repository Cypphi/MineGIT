package ca.modmonster.minegit.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Minecraft.class, remap = false)
public interface MinecraftAccessor {
    @Accessor("INSTANCE")
    static Minecraft getInstance() {
        return null;
    }
}
