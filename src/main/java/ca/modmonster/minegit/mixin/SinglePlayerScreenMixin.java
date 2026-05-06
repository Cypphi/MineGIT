package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.backport.ScreenUtil;
import ca.modmonster.minegit.backport.SinglePlayerScreenExtension;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.*;
import ca.modmonster.minegit.widget.WorldSyncButtonState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.storage.WorldSaveInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(SelectWorldScreen.class)
public abstract class SinglePlayerScreenMixin extends Screen implements SinglePlayerScreenExtension {
    @Shadow
    private int selectedWorldId;
    @Shadow
    private List<WorldSaveInfo> saves;

    @Shadow
    protected abstract String getSaveFileName(int index);

    @Shadow
    public abstract void selectWorld(int id);

    @Unique
    private String cloneButtonTooltip;

    @Unique @Nullable
    private ButtonWidget cloneButton;

    @Unique @Nullable
    private ImageButton worldSyncButton;

    @Unique @Nullable
    private List<String> worldSyncButtonTooltip;

    @Unique
    private WorldSyncButtonState worldSyncButtonState = WorldSyncButtonState.SETUP;

    @Unique
    private boolean prevAltState = false;

    @Unique
    private boolean showGitBeforeJoin = true;

    @Inject(at = @At("TAIL"), method = "init", remap = false)
	private void init(CallbackInfo info) {
        cloneButtonTooltip = I18n.translate("minegit.clone.title");

        // Add world sync button
        worldSyncButton = new ImageButton(100, width / 2 - 178, height - 52, ImageButton.ImageButtonTex.CLOUD);
        worldSyncButton.active = false;
        buttons.add(worldSyncButton);

        // Add clone button
        cloneButton = new ImageButton(101, width / 2 - 178, height - 28, ImageButton.ImageButtonTex.CLONE);
        buttons.add(cloneButton);

        updateWorldSyncButton();
	}

    @Inject(at = @At("TAIL"), method = "buttonClicked")
    protected void buttonClicked(ButtonWidget button, CallbackInfo ci) {
        if (button.id == 2) {
            // delete button; make .git folder writable
            GitManager.makeWritable(minecraft, getSaveFileName(selectedWorldId));
        } else if (button.id == 100) {
            if (worldSyncButtonState == WorldSyncButtonState.SETUP || ScreenUtil.isAltDown()) {
                minecraft.openScreen(new AccountLinkScreen(SinglePlayerScreenMixin.this, () -> {
                    returnToScreen();
                    updateWorldSyncButton();
                }));
            } else if (worldSyncButtonState == WorldSyncButtonState.ENABLE) {
                if (selectedWorldId != -1)
                    minecraft.openScreen(new EnableWorldSyncScreen(SinglePlayerScreenMixin.this, saves.get(selectedWorldId), () -> {
                        returnToScreen();
                        updateWorldSyncButton();
                    }));
            }
        } else if (button.id == 101) {
            minecraft.openScreen(new CloneScreen(() -> {
                returnToScreen();
                updateWorldSyncButton();
            }));
        }
    }

    @Inject(at = @At("TAIL"), method = "render", remap = false)
    public void render(int i, int j, float f, CallbackInfo ci) {
        if (cloneButton != null && cloneButton.isHovered()) renderTooltip(cloneButtonTooltip, i, j);
        if (worldSyncButtonTooltip != null && worldSyncButton != null &&  worldSyncButton.isHovered()) renderTooltip(worldSyncButtonTooltip, i, j);

        if (worldSyncButton != null && ScreenUtil.isAltDown() != prevAltState) {
            prevAltState = ScreenUtil.isAltDown();

            if (ScreenUtil.isAltDown()) {
                worldSyncButton.active = true;
                worldSyncButton.texture = ImageButton.ImageButtonTex.CLOUD;
                worldSyncButtonTooltip = Collections.singletonList(I18n.translate("minegit.link.setup.open"));
            } else {
                updateWorldSyncButton();
            }
        }
    }

    @Override
    public void worldSelected(int selectedWorld) {
        if (worldSyncButton == null) return;
        updateWorldSyncButton();
    }

    @Unique
    private void updateWorldSyncButton() {
        if (worldSyncButton == null) return;
        if (ScreenUtil.isAltDown()) return;
        Config config = ConfigManager.getCurrentConfig();
        if (config.username.replace(" ", "").isEmpty() || config.getPat().replace(" ", "").isEmpty()) {
            // Set the world sync button to configuration state
            worldSyncButtonState = WorldSyncButtonState.SETUP;
            this.worldSyncButton.active = true;
        } else if (selectedWorldId != -1 && GitManager.syncEnabled(minecraft, getSaveFileName(selectedWorldId))) {
            worldSyncButtonState = WorldSyncButtonState.WORLD_CONFIGURE;
            this.worldSyncButton.active = false;
        } else {
            worldSyncButtonState = WorldSyncButtonState.ENABLE;
            this.worldSyncButton.active = selectedWorldId != -1;
        }

        worldSyncButton.texture = worldSyncButtonState.texture;
        worldSyncButtonTooltip = worldSyncButtonState.getTooltip();
        if (cloneButton != null) cloneButton.active = worldSyncButtonState != WorldSyncButtonState.SETUP;
    }

    @Inject(method = "selectWorld", at = @At("HEAD"), cancellable = true)
    private void beforeWorldJoin(int id, CallbackInfo ci) {
        if (!showGitBeforeJoin) return;
        String worldId = getSaveFileName(selectedWorldId);
        if (!GitManager.syncEnabled(minecraft, worldId)) return;
        ci.cancel();
        GitProgressScreen progressScreen = new GitProgressScreen(I18n.translate("minegit.sync.status.git_pull"));
        minecraft.openScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.pull(GitManager.getPath(minecraft, worldId), progressScreen);
            GitManager.makeWritable(minecraft, worldId);
            switch (status) {
                case SUCCESS:
                    // Success; load world as normal
                    doLoadWorld();
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    minecraft.execute(() -> minecraft.openScreen(new GitConflictScreen(
                            this::doLoadWorld,
                            this::returnToScreen,
                            GitManager.getPath(minecraft, worldId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    minecraft.execute(() -> minecraft.openScreen(new TwoChoiceScreen(
                            I18n.translate("minegit.sync.pull_unreachable.title"),
                            I18n.translate("minegit.sync.pull_unreachable.description"),
                            I18n.translate("minegit.sync.pull_unreachable.continue"),
                            I18n.translate("minegit.sync.pull_unreachable.cancel"),
                            this::doLoadWorld, // continue
                            this::returnToScreen // cancel
                    )));
                    break;
            }
        }).start();
    }

    @Unique
    private void doLoadWorld() {
        minecraft.execute(() -> {
            showGitBeforeJoin = false;
            selectWorld(selectedWorldId);
            showGitBeforeJoin = true;
        });
    }

    @Unique
    private void returnToScreen() {
        minecraft.openScreen(this);
    }
}