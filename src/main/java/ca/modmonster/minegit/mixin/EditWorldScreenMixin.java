package ca.modmonster.minegit.mixin;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldEdit;
import net.minecraft.client.resources.I18n;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.gui.PruneWorldScreen;

@Mixin(GuiWorldEdit.class)
public class EditWorldScreenMixin extends GuiScreen {
    @Shadow
    @Final
    private String worldId;

    @Shadow
    @Final
    private GuiScreen lastScreen;

    @Inject(at = @At(value = "TAIL"), method = "initGui")
    private void initGui(CallbackInfo ci) {
        if (!GitManager.syncEnabled(mc, worldId)) return;

        // Add prune button
        addButton(new GuiButton(100, this.width / 2 - 100, this.height / 4 + 72 + 12, 200, 20, I18n.format("minegit.prune.button")));
    }

    @Inject(at = @At("TAIL"), method = "actionPerformed")
    protected void actionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.id == 100) {
            mc.displayGuiScreen(new PruneWorldScreen(EditWorldScreenMixin.this, worldId, lastScreen));
        }
    }
}
