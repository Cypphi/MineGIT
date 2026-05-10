package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.backport.ScreenTooltipRenderer;
import ca.modmonster.minegit.backport.ScreenUtil;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.gui.AccountLinkScreen;
import ca.modmonster.minegit.gui.CloneScreen;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ScreenCreateWorld;
import net.minecraft.client.gui.ScreenSelectWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScreenCreateWorld.class, remap = false)
public abstract class CreateWorldScreenMixin extends Screen {
    @Unique
    @Nullable
    private ImageButton gitButton;

    @Unique
    @Nullable
    private String gitButtonTooltip;

    @Unique
    private boolean needsSetup = false;

    @Inject(at = @At("TAIL"), method = "init")
    private void init(CallbackInfo info) {
        checkNeedsSetup();

        if (needsSetup) {
            // Add setup button
            gitButton = new ImageButton(100, this.width / 2 - 124, this.height / 4 + 116, ImageButton.ImageButtonTex.CLOUD);
            gitButtonTooltip = "Setup Cloud Syncing";
        } else {
            // Add clone button
            gitButton = new ImageButton(100, this.width / 2 - 124, this.height / 4 + 116, ImageButton.ImageButtonTex.CLONE);
            gitButtonTooltip = "Clone World";
        }
        buttons.add(gitButton);
    }

    @Inject(at = @At("TAIL"), method = "buttonClicked")
    protected void buttonClicked(ButtonElement button, CallbackInfo ci) {
        if (button.id == 100) {
            onGitButtonPress();
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(final int mouseX, final int mouseY, final float a, CallbackInfo info) {
        if (gitButton != null && ScreenUtil.isHovered(mouseX, mouseY, gitButton.xPosition, gitButton.yPosition, 20, 20)) {
            ((ScreenTooltipRenderer) this).renderTooltip(gitButtonTooltip, mouseX, mouseY);
        }
    }

    @Unique
    void onGitButtonPress() {
        if (needsSetup) {
            this.mc.displayScreen(new AccountLinkScreen(this, this::updateSetupButton));
        } else {
            this.mc.displayScreen(
                    new CloneScreen(() -> mc.displayScreen(this), () ->
                            mc.displayScreen(new ScreenSelectWorld(null))));
        }
    }

    @Unique
    void checkNeedsSetup() {
        Config config = ConfigManager.getCurrentConfig();
        needsSetup = config.username.replace(" ", "").isEmpty() || config.getPat().replace(" ", "").isEmpty();
    }

    @Unique
    void updateSetupButton() {
        if (gitButton == null) return;
        checkNeedsSetup();
        if (needsSetup) {
            gitButton.texture = ImageButton.ImageButtonTex.CLOUD;
            gitButtonTooltip = "Setup Cloud Syncing";
        } else {
            gitButton.texture = ImageButton.ImageButtonTex.CLONE;
            gitButtonTooltip = "Clone World";
        }
    }
}
