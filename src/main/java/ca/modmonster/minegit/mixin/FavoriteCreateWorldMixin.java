package ca.modmonster.minegit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

// Cherished worlds mod compat; move the star 20px to the left on world creation screen
@Mixin(targets = "com.illusivesoulworks.cherishedworlds.client.favorites.FavoriteCreateWorld")
public class FavoriteCreateWorldMixin {
    @ModifyConstant(method = "getHorizontalOffset", constant = @Constant(intValue = 170))
    private int changeOffset(int original) {
        return 194;
    }
}