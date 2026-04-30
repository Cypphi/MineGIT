package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.gui.AccountLinkScreen;
import ca.modmonster.minegit.gui.CloneScreen;
import ca.modmonster.minegit.gui.EnableWorldSyncScreen;
import ca.modmonster.minegit.widget.WorldSyncButtonState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(SelectWorldScreen.class)
public class SinglePlayerScreenMixin extends Screen {
    @Unique
    private static final Component CLONE_BUTTON_TOOLTIP = new TranslatableComponent("minegit.clone.title");

    @Shadow
    private @Nullable WorldSelectionList list;

    @Shadow
    protected EditBox searchBox;

    protected SinglePlayerScreenMixin(Component title) {
        super(title);
    }

    @Unique @Nullable
    private Button cloneButton;

    @Unique @Nullable
    private Button worldSyncButton;

    @Unique @Nullable
    private List<Component> worldSyncButtonTooltip;

    @Unique
    private WorldSyncButtonState worldSyncButtonState = WorldSyncButtonState.SETUP;

    @Unique @Nullable
    private LevelSummary hoveredLevel;

    @Unique
    private boolean altHeld;

    @Inject(at = @At("TAIL"), method = "init", remap = false)
	private void init(CallbackInfo info) {
        // Add world sync button
        worldSyncButton = new Button(width / 2 - 178, height - 52, 20, 20, new TextComponent("☁"), button -> {
            if (worldSyncButtonState == WorldSyncButtonState.SETUP || altHeld) {
                altHeld = false;
                this.minecraft.setScreen(new AccountLinkScreen(this, () -> {
                    if (this.list != null) returnToScreen();
                    updateWorldSyncButton();
                }));
            } else if (worldSyncButtonState == WorldSyncButtonState.ENABLE) {
                if (hoveredLevel != null) this.minecraft.setScreen(new EnableWorldSyncScreen(this, hoveredLevel, () -> {
                    if (this.list != null) returnToScreen();
                    updateWorldSyncButton();
                }));
            }
        });
        worldSyncButton.active = false;
        addButton(worldSyncButton);

        // Add clone button
        cloneButton = new Button(width / 2 - 178, height - 28, 20, 20, new TextComponent("↓"), button -> this.minecraft.setScreen(new CloneScreen(() -> {
            if (this.list != null) returnToScreen();
            updateWorldSyncButton();
        })));
        addButton(cloneButton);

        updateWorldSyncButton();
	}

    @Inject(at = @At("TAIL"), method = "render", remap = false)
    public void render(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {
        if (cloneButton != null && cloneButton.isHovered()) renderTooltip(poseStack, CLONE_BUTTON_TOOLTIP, i, j);
        if (worldSyncButtonTooltip != null && worldSyncButton != null &&  worldSyncButton.isHovered()) renderComponentTooltip(poseStack, worldSyncButtonTooltip, i, j);
    }

    @Inject(at = @At("TAIL"), method = "updateButtonStatus", remap = false)
    private void updateButtonStatus(boolean bl, CallbackInfo ci) {
        if (worldSyncButton == null) return;
        if (list == null) return;
        list.getSelectedOpt().ifPresent((level) -> hoveredLevel = ((WorldListEntryAccessor) (Object) level).getSummary());
        updateWorldSyncButton();
    }

    @Unique
    private void updateWorldSyncButton() {
        if (worldSyncButton == null) return;
        if (altHeld) return;
        Config config = ConfigManager.getCurrentConfig();
        if (config.username.replace(" ", "").isEmpty() || config.getPat().replace(" ", "").isEmpty()) {
            // Set the world sync button to configuration state
            worldSyncButtonState = WorldSyncButtonState.SETUP;
            this.worldSyncButton.active = true;
        } else if (hoveredLevel != null && GitManager.syncEnabled(minecraft, hoveredLevel.getLevelId())) {
            worldSyncButtonState = WorldSyncButtonState.WORLD_CONFIGURE;
            this.worldSyncButton.active = false;
        } else {
            worldSyncButtonState = WorldSyncButtonState.ENABLE;
            this.worldSyncButton.active = hoveredLevel != null;
        }

        worldSyncButton.setMessage(worldSyncButtonState.message);
        worldSyncButtonTooltip = worldSyncButtonState.tooltip;
        if (cloneButton != null) cloneButton.active = worldSyncButtonState != WorldSyncButtonState.SETUP;
    }

    @Inject(at = @At("HEAD"), method = "keyPressed")
    public void keyPressed(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if (i == 342) {
            altHeld = true;
            if (worldSyncButton != null) {
                worldSyncButton.active = true;
                worldSyncButton.setMessage(new TextComponent("☁"));
                worldSyncButtonTooltip = Collections.singletonList(new TranslatableComponent("minegit.link.setup.open"));
            }
        }
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        if (i == 342) {
            altHeld = false;
            updateWorldSyncButton();
        }
        return super.keyReleased(i, j, k);
    }

    @Unique
    private void returnToScreen() {
        WorldSelectionList list = ((SelectWorldScreenAccessor) this).getLevelList();
        ((WorldSelectionListInvoker) list).invokeReloadWorldList(() -> searchBox.getValue(), true);
        minecraft.setScreen(this);
    }
}