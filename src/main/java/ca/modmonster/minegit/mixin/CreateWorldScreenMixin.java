package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.gui.AccountLinkScreen;
import ca.modmonster.minegit.gui.CloneScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
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
    private Button gitButton;

    @Unique
    @Nullable
    private String gitButtonTooltip;

    @Unique
    private boolean needsSetup = false;

    protected CreateWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init", remap = false)
    private void init(CallbackInfo info) {
        checkNeedsSetup();

        if (needsSetup) {
            // Add setup button
            gitButton = new Button(width / 2 - 178, height - 28, 20, 20, "☁", this::onGitButtonPress);
            gitButtonTooltip = I18n.get("minegit.link.setup");
        } else {
            // Add clone button
            gitButton = new Button(width / 2 - 178, height - 28, 20, 20, "↓", this::onGitButtonPress);
            gitButtonTooltip = I18n.get("minegit.clone.title");
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
    void onGitButtonPress(Button gitButton) {
        if (needsSetup) {
            this.minecraft.setScreen(new AccountLinkScreen(this, this::updateSetupButton));
        } else {
            this.minecraft.setScreen(
                    new CloneScreen(() -> minecraft.setScreen(this), () ->
                            minecraft.setScreen(new SelectWorldScreen(null))));
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
            gitButton.setMessage("☁");
            gitButtonTooltip = I18n.get("minegit.link.setup");
        } else {
            gitButton.setMessage("↓");
            gitButtonTooltip = I18n.get("minegit.clone.title");
        }
    }
}
