package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.gui.PruneWorldScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiRenameWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenameWorld.class)
public class EditWorldScreenMixin extends GuiScreen {
    @Shadow
    @Final
    private String saveName;

    @Shadow
    private GuiScreen parentScreen;

    @Inject(at = @At(value = "TAIL"), method = "initGui")
    private void init(CallbackInfo ci) {
        if (!GitManager.syncEnabled(mc, saveName)) return;

        // Add prune button
        buttonList.add(new GuiButton(100, this.width / 2 - 100, this.height / 4 + 72 + 12, 200, 20, I18n.format("minegit.prune.button")));
    }

    @Inject(at = @At("TAIL"), method = "actionPerformed")
    protected void buttonClicked(GuiButton button, CallbackInfo ci) {
        if (button.id == 100) {
            mc.displayGuiScreen(new PruneWorldScreen(EditWorldScreenMixin.this, saveName, parentScreen));
        }
    }
}
