package ca.modmonster.minegit.mixin;

import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Properties;

@Mixin(Language.class)
public class LocaleMixin {
    @Shadow
    private Properties translations;

    @Inject(at = @At("TAIL"), method = "loadLanguage")
    public void load(String code, boolean forceUpdate, CallbackInfo ci) {
        if (!code.equals("en_US")) return;
        this.translations.put("selectWorld.rename", "Edit");
        this.translations.put("selectWorld.renameTitle", "Edit World");
    }
}
