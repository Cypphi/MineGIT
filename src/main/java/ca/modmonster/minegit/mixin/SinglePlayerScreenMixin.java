package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.*;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.*;
import ca.modmonster.minegit.widget.WorldSyncButtonState;
import net.minecraft.class_591;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
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
    private int field_2435;
    @Shadow
    private List field_2436;

    @Shadow
    protected abstract String method_1887(int index);

    @Shadow
    public abstract void method_1891(int id);

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

    @Inject(at = @At("TAIL"), method = "init")
	private void init(CallbackInfo info) {
        cloneButtonTooltip = "Clone World";

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
            GitManager.makeWritable(method_1887(field_2435));
        } else if (button.id == 100) {
            if (worldSyncButtonState == WorldSyncButtonState.SETUP || ScreenUtil.isAltDown()) {
                minecraft.setScreen(new AccountLinkScreen(SinglePlayerScreenMixin.this, () -> {
                    returnToScreen();
                    updateWorldSyncButton();
                }));
            } else if (worldSyncButtonState == WorldSyncButtonState.ENABLE) {
                if (field_2435 != -1)
                    minecraft.setScreen(new EnableWorldSyncScreen(SinglePlayerScreenMixin.this, (class_591) field_2436.get(field_2435), () -> {
                        returnToScreen();
                        updateWorldSyncButton();
                    }));
            }
        } else if (button.id == 101) {
            minecraft.setScreen(new CloneScreen(() -> {
                returnToScreen();
                updateWorldSyncButton();
            }));
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void render(int i, int j, float f, CallbackInfo ci) {
        if (cloneButton != null && ScreenUtil.isHovered(i, j, cloneButton.x, cloneButton.y, 20, 20))
            ((ScreenTooltipRenderer) this).renderTooltip(cloneButtonTooltip, i, j);
        if (worldSyncButtonTooltip != null && worldSyncButton != null && ScreenUtil.isHovered(i, j, worldSyncButton.x, worldSyncButton.y, 20, 20))
            ((ScreenTooltipRenderer) this).renderTooltip(worldSyncButtonTooltip, i, j);

        if (worldSyncButton != null && ScreenUtil.isAltDown() != prevAltState) {
            prevAltState = ScreenUtil.isAltDown();

            if (ScreenUtil.isAltDown()) {
                worldSyncButton.active = true;
                worldSyncButton.texture = ImageButton.ImageButtonTex.CLOUD;
                worldSyncButtonTooltip = Collections.singletonList("Open Cloud Sync Setup");
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
        } else if (field_2435 != -1 && GitManager.syncEnabled(method_1887(field_2435))) {
            worldSyncButtonState = WorldSyncButtonState.WORLD_CONFIGURE;
            this.worldSyncButton.active = false;
        } else {
            worldSyncButtonState = WorldSyncButtonState.ENABLE;
            this.worldSyncButton.active = field_2435 != -1;
        }

        worldSyncButton.texture = worldSyncButtonState.texture;
        worldSyncButtonTooltip = worldSyncButtonState.getTooltip();
        if (cloneButton != null) cloneButton.active = worldSyncButtonState != WorldSyncButtonState.SETUP;
    }

    @Inject(method = "method_1891", at = @At("HEAD"), cancellable = true)
    private void beforeWorldJoin(int id, CallbackInfo ci) {
        if (!showGitBeforeJoin) return;
        String worldId = method_1887(id);
        if (!GitManager.syncEnabled(worldId)) return;
        ci.cancel();
        GitProgressScreen progressScreen = new GitProgressScreen("Pulling from GitHub...");
        minecraft.setScreen(progressScreen);
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
                    MainThreadTasks.execute(() -> minecraft.setScreen(new GitConflictScreen(
                            this::doLoadWorld,
                            this::returnToScreen,
                            GitManager.getPath(worldId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    MainThreadTasks.execute(() -> minecraft.setScreen(new TwoChoiceScreen(
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
            method_1891(field_2435);
            showGitBeforeJoin = true;
        });
    }

    @Unique
    private void returnToScreen() {
        minecraft.setScreen(this);
    }
}