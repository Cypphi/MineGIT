package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.gui.AccountLinkScreen;
import ca.modmonster.minegit.gui.CloneScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.resource.language.I18n;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    @Unique
    @Nullable
    private ImageButton gitButton;

    @Unique
    @Nullable
    private String gitButtonTooltip;

    @Unique
    private boolean needsSetup = false;

    @Inject(at = @At("TAIL"), method = "init", remap = false)
    private void init(CallbackInfo info) {
        checkNeedsSetup();

        if (needsSetup) {
            // Add setup button
            gitButton = new ImageButton(100, width / 2 - 178, height - 28, ImageButton.ImageButtonTex.CLOUD) {
                @Override
                public void click(double mouseX, double mouseY) {
                    onGitButtonPress();
                }
            };
            gitButtonTooltip = I18n.translate("minegit.link.setup");
        } else {
            // Add clone button
            gitButton = new ImageButton(100, width / 2 - 178, height - 28, ImageButton.ImageButtonTex.CLONE) {
                @Override
                public void click(double mouseX, double mouseY) {
                    onGitButtonPress();
                }
            };
            gitButtonTooltip = I18n.translate("minegit.clone.title");
        }
        addButton(gitButton);
    }

    @Inject(at = @At("TAIL"), method = "render", remap = false)
    private void render(final int mouseX, final int mouseY, final float a, CallbackInfo info) {
        if (gitButton != null && gitButton.isHovered()) {
            renderTooltip(gitButtonTooltip, mouseX, mouseY);
        }
    }

    @Unique
    void onGitButtonPress() {
        if (needsSetup) {
            this.minecraft.openScreen(new AccountLinkScreen(this, this::updateSetupButton));
        } else {
            this.minecraft.openScreen(
                    new CloneScreen(() -> minecraft.openScreen(this), () ->
                            minecraft.openScreen(new SelectWorldScreen(null))));
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
            gitButtonTooltip = I18n.translate("minegit.link.setup");
        } else {
            gitButton.texture = ImageButton.ImageButtonTex.CLONE;
            gitButtonTooltip = I18n.translate("minegit.clone.title");
        }
    }
}
