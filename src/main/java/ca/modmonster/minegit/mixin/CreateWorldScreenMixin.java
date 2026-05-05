package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.gui.AccountLinkScreen;
import ca.modmonster.minegit.gui.CloneScreen;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiCreateWorld.class)
public abstract class CreateWorldScreenMixin extends GuiScreen {
    @Unique
    private ImageButton mineGIT$gitButton;

    @Unique
    private String mineGIT$gitButtonTooltip;

    @Unique
    private boolean mineGIT$needsSetup = false;

    @Inject(at = @At("TAIL"), method = "initGui")
    private void initGui(CallbackInfo info) {
        mineGIT$checkNeedsSetup();

        if (mineGIT$needsSetup) {
            // Add setup button
            mineGIT$gitButton = new ImageButton(100, width / 2 - 178, height - 28, ImageButton.ImageButtonTex.CLOUD);
            mineGIT$gitButtonTooltip = I18n.format("minegit.link.setup");
        } else {
            // Add clone button
            mineGIT$gitButton = new ImageButton(100, width / 2 - 178, height - 28, ImageButton.ImageButtonTex.CLONE);
            mineGIT$gitButtonTooltip = I18n.format("minegit.clone.title");
        }
        addButton(mineGIT$gitButton);
    }

    @Inject(at = @At("TAIL"), method = "actionPerformed")
    protected void actionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.id == 100) {
            mineGIT$onGitButtonPress();
        }
    }

    @Inject(at = @At("TAIL"), method = "drawScreen")
    private void drawScreen(final int mouseX, final int mouseY, final float a, CallbackInfo info) {
        if (mineGIT$gitButton != null && mineGIT$gitButton.isMouseOver()) {
            drawHoveringText(mineGIT$gitButtonTooltip, mouseX, mouseY);
        }
    }

    @Unique
    void mineGIT$onGitButtonPress() {
        if (mineGIT$needsSetup) {
            this.mc.displayGuiScreen(new AccountLinkScreen(this, this::mineGIT$updateSetupButton));
        } else {
            this.mc.displayGuiScreen(
                    new CloneScreen(() -> mc.displayGuiScreen(this), () ->
                            mc.displayGuiScreen(new GuiWorldSelection(new GuiMainMenu()))));
        }
    }

    @Unique
    void mineGIT$checkNeedsSetup() {
        Config config = ConfigManager.getCurrentConfig();
        mineGIT$needsSetup = config.username.replace(" ", "").isEmpty() || config.getPat().replace(" ", "").isEmpty();
    }

    @Unique
    void mineGIT$updateSetupButton() {
        if (mineGIT$gitButton == null) return;
        mineGIT$checkNeedsSetup();
        if (mineGIT$needsSetup) {
            mineGIT$gitButton.texture = ImageButton.ImageButtonTex.CLOUD;
            mineGIT$gitButtonTooltip = I18n.format("minegit.link.setup");
        } else {
            mineGIT$gitButton.texture = ImageButton.ImageButtonTex.CLONE;
            mineGIT$gitButtonTooltip = I18n.format("minegit.clone.title");
        }
    }
}
