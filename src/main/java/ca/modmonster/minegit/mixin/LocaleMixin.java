package ca.modmonster.minegit.mixin;

import net.minecraft.client.resource.language.Locale;
import net.minecraft.client.resource.manager.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(Locale.class)
public class LocaleMixin {
    @Shadow
    Map translations;

    @Inject(at = @At("TAIL"), method = "load(Lnet/minecraft/client/resource/manager/ResourceManager;Ljava/util/List;)V")
    public void load(ResourceManager resourceManager, List languageCodes, CallbackInfo ci) {
        this.translations.put("selectWorld.rename", "Edit");
        this.translations.put("selectWorld.renameTitle", "Edit World");
    }
}
