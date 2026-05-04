package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.gui.AccountLinkScreen;
import ca.modmonster.minegit.gui.CloneScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_01559903;
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
    private C_01559903 gitButton;

    @Unique
    @Nullable
    private String gitButtonTooltip;

    @Unique
    private boolean needsSetup = false;

    protected CreateWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init", remap = false)
    private void init(CallbackInfo info) {
        checkNeedsSetup();

        if (needsSetup) {
            // Add setup button
            gitButton = new C_01559903(width / 2 - 178, height - 28, 20, 20, "☁", this::onGitButtonPress);
            gitButtonTooltip = I18n.translate("minegit.link.setup");
        } else {
            // Add clone button
            gitButton = new C_01559903(width / 2 - 178, height - 28, 20, 20, "↓", this::onGitButtonPress);
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
    void onGitButtonPress(C_01559903 gitButton) {
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
            gitButton.setMessage("☁");
            gitButtonTooltip = I18n.translate("minegit.link.setup");
        } else {
            gitButton.setMessage("↓");
            gitButtonTooltip = I18n.translate("minegit.clone.title");
        }
    }
}
