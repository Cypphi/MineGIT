package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.*;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.*;
import ca.modmonster.minegit.widget.WorldSyncButtonState;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ScreenSelectWorld;
import net.minecraft.core.world.save.SaveFile;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(value = ScreenSelectWorld.class, remap = false)
public abstract class SinglePlayerScreenMixin extends Screen implements SinglePlayerScreenExtension {
    @Shadow
    private int selectedWorld;
    @Shadow
    private List<SaveFile> saveList;

    @Shadow
    protected abstract String getSaveFileName(int i);

    @Shadow
    public abstract void selectWorld(int i);

    @Unique
    private String cloneButtonTooltip;

    @Unique @Nullable
    private ButtonElement cloneButton;

    @Unique @Nullable
    private ImageButton worldSyncButton;

    @Unique @Nullable
    private List<String> worldSyncButtonTooltip;

    @Unique @Nullable
    private ButtonElement pruneButton;

    @Unique
    private WorldSyncButtonState worldSyncButtonState = WorldSyncButtonState.SETUP;

    @Unique
    private boolean prevAltState = false;

    @Unique
    private boolean showGitBeforeJoin = true;

    @Inject(at = @At("TAIL"), method = "init")
	private void init(CallbackInfo info) {
        cloneButtonTooltip = "Clone World";

        // Add world sync button
        worldSyncButton = new ImageButton(100, width / 2 - 178, height - 52, ImageButton.ImageButtonTex.CLOUD);
        worldSyncButton.enabled = false;
        buttons.add(worldSyncButton);

        // Add clone button
        cloneButton = new ImageButton(101, width / 2 - 178, height - 28, ImageButton.ImageButtonTex.CLONE);
        buttons.add(cloneButton);

        updateWorldSyncButton();
	}

    @Inject(at = @At("TAIL"), method = "initButtons")
    public void initButtons(CallbackInfo ci) {
        ButtonElement cancelButton = this.buttons.get(this.buttons.size() - 1);
        cancelButton.width = 70;
        cancelButton.xPosition = this.width / 2 + 84;

        // Add prune button
        pruneButton = new ButtonElement(102, this.width / 2 + 4, this.height - 28, 70, 20, "Prune");
        pruneButton.enabled = false;
        buttons.add(pruneButton);
    }

    @Inject(at = @At("TAIL"), method = "buttonClicked")
    protected void buttonClicked(ButtonElement button, CallbackInfo ci) {
        if (button.id == 2) {
            // delete button; make .git folder writable
            GitManager.makeWritable(getSaveFileName(selectedWorld));
        } else if (button.id == 100) {
            if (worldSyncButtonState == WorldSyncButtonState.SETUP || ScreenUtil.isAltDown()) {
                mc.displayScreen(new AccountLinkScreen(SinglePlayerScreenMixin.this, () -> {
                    returnToScreen();
                    updateWorldSyncButton();
                }));
            } else if (worldSyncButtonState == WorldSyncButtonState.ENABLE) {
                if (selectedWorld != -1)
                    mc.displayScreen(new EnableWorldSyncScreen(SinglePlayerScreenMixin.this, saveList.get(selectedWorld), () -> {
                        returnToScreen();
                        updateWorldSyncButton();
                    }));
            }
        } else if (button.id == 101) {
            mc.displayScreen(new CloneScreen(() -> {
                returnToScreen();
                updateWorldSyncButton();
            }));
        } else if (button.id == 102) {
            mc.displayScreen(new PruneWorldScreen(this, getSaveFileName(selectedWorld), this));
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void render(int i, int j, float f, CallbackInfo ci) {
        if (cloneButton != null && ScreenUtil.isHovered(i, j, cloneButton.xPosition, cloneButton.yPosition, 20, 20))
            ((ScreenTooltipRenderer) this).renderTooltip(cloneButtonTooltip, i, j);
        if (worldSyncButtonTooltip != null && worldSyncButton != null && ScreenUtil.isHovered(i, j, worldSyncButton.xPosition, worldSyncButton.yPosition, 20, 20))
            ((ScreenTooltipRenderer) this).renderTooltip(worldSyncButtonTooltip, i, j);

        if (worldSyncButton != null && ScreenUtil.isAltDown() != prevAltState) {
            prevAltState = ScreenUtil.isAltDown();

            if (ScreenUtil.isAltDown()) {
                worldSyncButton.enabled = true;
                worldSyncButton.texture = ImageButton.ImageButtonTex.CLOUD;
                worldSyncButtonTooltip = Collections.singletonList("Open Cloud Sync Setup");
            } else {
                updateWorldSyncButton();
            }
        }
    }

    @Override
    public void worldSelected(int selectedWorld) {
        if (pruneButton == null || worldSyncButton == null) return;

        boolean corrupted = saveList.get(selectedWorld) == null || (saveList.get(selectedWorld)).isCorrupted();
        pruneButton.enabled = !corrupted;

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
            this.worldSyncButton.enabled = true;
        } else if (selectedWorld != -1 && GitManager.syncEnabled(getSaveFileName(selectedWorld))) {
            worldSyncButtonState = WorldSyncButtonState.WORLD_CONFIGURE;
            this.worldSyncButton.enabled = false;
        } else {
            worldSyncButtonState = WorldSyncButtonState.ENABLE;
            this.worldSyncButton.enabled = selectedWorld != -1;
        }

        worldSyncButton.texture = worldSyncButtonState.texture;
        worldSyncButtonTooltip = worldSyncButtonState.getTooltip();
        if (cloneButton != null) cloneButton.enabled = worldSyncButtonState != WorldSyncButtonState.SETUP;
    }

    @Inject(method = "selectWorld", at = @At("HEAD"), cancellable = true)
    private void beforeWorldJoin(int id, CallbackInfo ci) {
        if (!showGitBeforeJoin) return;
        String worldId = getSaveFileName(id);
        if (!GitManager.syncEnabled(worldId)) return;
        ci.cancel();
        GitProgressScreen progressScreen = new GitProgressScreen("Pulling from GitHub...");
        mc.displayScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.pull(GitManager.getPath(worldId), progressScreen);
            GitManager.makeWritable(worldId);
            switch (status) {
                case SUCCESS:
                    // Success; load world as normal
                    doLoadWorld();
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    MainThreadTasks.execute(() -> mc.displayScreen(new GitConflictScreen(
                            this::doLoadWorld,
                            this::returnToScreen,
                            GitManager.getPath(worldId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    MainThreadTasks.execute(() -> mc.displayScreen(new TwoChoiceScreen(
                            "Error syncing world",
                            "Your latest world changes could not be synced with the cloud. You can continue to load the world if you wish, but you might not have the latest version of your world.",
                            "Load without syncing",
                            "Cancel",
                            this::doLoadWorld, // continue
                            this::returnToScreen // cancel
                    )));
                    break;
            }
        }).start();
    }

    @Unique
    private void doLoadWorld() {
        MainThreadTasks.execute(() -> {
            showGitBeforeJoin = false;
            selectWorld(selectedWorld);
            showGitBeforeJoin = true;
        });
    }

    @Unique
    private void returnToScreen() {
        mc.displayScreen(this);
    }
}