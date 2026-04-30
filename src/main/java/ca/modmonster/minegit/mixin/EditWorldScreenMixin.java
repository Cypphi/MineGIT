package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.gui.PruneWorldScreen;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
    private LevelStorageSource.LevelStorageAccess levelAccess;

    @Shadow
    @Final
    private BooleanConsumer callback;

    protected EditWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At(value = "TAIL"), method = "init")
    private void init(CallbackInfo ci) {
        if (!GitManager.syncEnabled(minecraft, levelAccess.getLevelId())) return;

        // Add prune button
        addButton(new Button(this.width / 2 - 100, this.height / 4 + 120 + 5, 200, 20, new TranslatableComponent("minegit.prune.button"), button ->
                minecraft.setScreen(new PruneWorldScreen(this, levelAccess, callback))));

        // Reposition existing buttons
        for (GuiEventListener child : this.children()) {
            if (!(child instanceof Button)) return;
            Button button = (Button) child;
            if (!(button.getMessage() instanceof TranslatableComponent)) return;
            TranslatableComponent translatable = (TranslatableComponent) button.getMessage();
            String key = translatable.getKey();

            switch (key) {
                case "selectWorld.edit.backup":
                    button.setWidth(80);
                    break;
                case "selectWorld.edit.backupFolder":
                    button.setWidth(116);
                    button.x = this.width / 2 - 16;
                    button.y = this.height / 4 + 48 + 5;
                    break;
                case "selectWorld.edit.optimize":
                    button.y = this.height / 4 + 72 + 5;
                    break;
                case "selectWorld.edit.export_worldgen_settings":
                    button.y = this.height / 4 + 96 + 5;
                    break;
            }
        }
    }
}
