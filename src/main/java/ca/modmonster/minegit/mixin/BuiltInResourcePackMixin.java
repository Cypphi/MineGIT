package ca.modmonster.minegit.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.resource.pack.BuiltInResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashSet;
import java.util.Set;

@Mixin(BuiltInResourcePack.class)
public class BuiltInResourcePackMixin {
    @ModifyReturnValue(method = "getNamespaces", at = @At("RETURN"))
    private Set<String> injectNamespace(Set<String> original) {
        Set<String> set = new HashSet<>(original);
        set.add("minegit");
        return set;
    }
}