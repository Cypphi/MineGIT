package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.gui.PruneWorldScreen;
import ca.modmonster.minegit.gui.RevertScreen;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditWorldScreen.class)
public class EditWorldScreenMixin extends Screen {
    @Shadow
    @Final
    private LinearLayout layout;

    @Shadow
    @Final
    private LevelStorageSource.LevelStorageAccess levelAccess;

    @Shadow
    @Final
    private BooleanConsumer callback;

    protected EditWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 10), method = "<init>", remap = false)
    private void init(CallbackInfo info) {
        if (!GitManager.syncEnabled(minecraft, levelAccess.getLevelId())) return;

        LinearLayout row = new LinearLayout(0, 0, LinearLayout.Orientation.HORIZONTAL);
        row.spacing(4);

        // Add buttons
        row.addChild(Button.builder(Component.translatable("minegit.prune.button"), button -> minecraft.setScreen(new PruneWorldScreen(this, levelAccess, callback))).width(98).build());
        row.addChild(Button.builder(Component.translatable("minegit.revert"), button -> minecraft.setScreen(new RevertScreen(this, levelAccess, callback))).width(98).build());

        // Add row
        layout.addChild(row);
    }
}
