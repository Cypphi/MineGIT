package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.gui.AccountLinkScreen;
import ca.modmonster.minegit.gui.CloneScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(GuiCreateWorld.class)
public abstract class CreateWorldScreenMixin extends GuiScreen {
    @Unique
    @Nullable
    private ImageButton gitButton;

    @Unique
    @Nullable
    private String gitButtonTooltip;

    @Unique
    private boolean needsSetup = false;

    @Inject(at = @At("TAIL"), method = "initGui")
    private void init(CallbackInfo info) {
        checkNeedsSetup();

        if (needsSetup) {
            // Add setup button
            gitButton = new ImageButton(100, width / 2 - 178, height - 28, ImageButton.ImageButtonTex.CLOUD);
            gitButtonTooltip = I18n.format("minegit.link.setup");
        } else {
            // Add clone button
            gitButton = new ImageButton(100, width / 2 - 178, height - 28, ImageButton.ImageButtonTex.CLONE);
            gitButtonTooltip = I18n.format("minegit.clone.title");
        }
        buttonList.add(gitButton);
    }

    @Inject(at = @At("TAIL"), method = "actionPerformed")
    protected void buttonClicked(GuiButton button, CallbackInfo ci) {
        if (button.id == 100) {
            onGitButtonPress();
        }
    }

    @Inject(at = @At("TAIL"), method = "drawScreen")
    private void render(final int mouseX, final int mouseY, final float a, CallbackInfo info) {
        if (gitButton != null && gitButton.isMouseOver()) {
            drawHoveringText(Collections.singletonList(gitButtonTooltip), mouseX, mouseY);
        }
    }

    @Unique
    void onGitButtonPress() {
        if (needsSetup) {
            this.mc.displayGuiScreen(new AccountLinkScreen(this, this::updateSetupButton));
        } else {
            this.mc.displayGuiScreen(
                    new CloneScreen(() -> mc.displayGuiScreen(this), () ->
                            mc.displayGuiScreen(new GuiSelectWorld(null))));
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
            gitButtonTooltip = I18n.format("minegit.link.setup");
        } else {
            gitButton.texture = ImageButton.ImageButtonTex.CLONE;
            gitButtonTooltip = I18n.format("minegit.clone.title");
        }
    }
}
