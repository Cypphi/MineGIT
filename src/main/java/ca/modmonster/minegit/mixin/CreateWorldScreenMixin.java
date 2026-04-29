package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.gui.AccountLinkScreen;
import ca.modmonster.minegit.gui.CloneScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
    private Component gitButtonTooltip;

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
            gitButton = new Button(width / 2 - 178, height - 28, 20, 20, new TextComponent("☁"), this::onGitButtonPress);
            gitButtonTooltip = new TranslatableComponent("minegit.link.setup");
        } else {
            // Add clone button
            gitButton = new Button(width / 2 - 178, height - 28, 20, 20, new TextComponent("↓"), this::onGitButtonPress);
            gitButtonTooltip = new TranslatableComponent("minegit.clone.title");
        }
        addRenderableWidget(gitButton);
    }

    @Inject(at = @At("TAIL"), method = "render", remap = false)
    private void render(PoseStack poseStack, final int mouseX, final int mouseY, final float a, CallbackInfo info) {
        if (gitButton != null && gitButton.isHoveredOrFocused()) {
            renderTooltip(poseStack, gitButtonTooltip, mouseX, mouseY);
        }
    }

    @Unique
    void onGitButtonPress(Button gitButton) {
        if (needsSetup) {
            this.minecraft.setScreen(new AccountLinkScreen(this, this::updateSetupButton));
        } else {
            this.minecraft.setScreen(
                    new CloneScreen(this, null, () ->
                            minecraft.setScreen(new SelectWorldScreen(null))));
        }
    }

    @Unique
    void checkNeedsSetup() {
        Config config = ConfigManager.getCurrentConfig();
        needsSetup = config.username.isBlank() || config.getPat().isBlank();
    }

    @Unique
    void updateSetupButton() {
        if (gitButton == null) return;
        checkNeedsSetup();
        if (needsSetup) {
            gitButton.setMessage(new TextComponent("☁"));
            gitButtonTooltip = new TranslatableComponent("minegit.link.setup");
        } else {
            gitButton.setMessage(new TextComponent("↓"));
            gitButtonTooltip = new TranslatableComponent("minegit.clone.title");
        }
    }
}
