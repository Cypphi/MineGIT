package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.backport.SinglePlayerScreenExtension;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.*;
import ca.modmonster.minegit.widget.WorldSyncButtonState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.storage.SaveFormatComparator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(GuiSelectWorld.class)
public abstract class SinglePlayerScreenMixin extends GuiScreen implements SinglePlayerScreenExtension {
    @Shadow
    private int selectedIndex;
    @Shadow
    private List<SaveFormatComparator> field_146639_s;

    @Shadow
    protected abstract String func_146621_a(int index);

    @Shadow
    public abstract void func_146615_e(int id);

    @Unique
    private String cloneButtonTooltip;

    @Unique @Nullable
    private GuiButton cloneButton;

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

    @Inject(at = @At("TAIL"), method = "initGui")
	private void init(CallbackInfo info) {
        cloneButtonTooltip = I18n.format("minegit.clone.title");

        // Add world sync button
        worldSyncButton = new ImageButton(100, width / 2 - 178, height - 52, ImageButton.ImageButtonTex.CLOUD);
        worldSyncButton.enabled = false;
        buttonList.add(worldSyncButton);

        // Add clone button
        cloneButton = new ImageButton(101, width / 2 - 178, height - 28, ImageButton.ImageButtonTex.CLONE);
        buttonList.add(cloneButton);

        updateWorldSyncButton();
	}

    @Inject(at = @At("TAIL"), method = "actionPerformed")
    protected void buttonClicked(GuiButton button, CallbackInfo ci) {
        if (button.id == 2) {
            // delete button; make .git folder writable
            GitManager.makeWritable(mc, func_146621_a(selectedIndex));
        } else if (button.id == 100) {
            if (worldSyncButtonState == WorldSyncButtonState.SETUP || isAltKeyDown()) {
                mc.displayGuiScreen(new AccountLinkScreen(SinglePlayerScreenMixin.this, () -> {
                    returnToScreen();
                    updateWorldSyncButton();
                }));
            } else if (worldSyncButtonState == WorldSyncButtonState.ENABLE) {
                if (selectedIndex != -1)
                    mc.displayGuiScreen(new EnableWorldSyncScreen(SinglePlayerScreenMixin.this, field_146639_s.get(selectedIndex), () -> {
                        returnToScreen();
                        updateWorldSyncButton();
                    }));
            }
        } else if (button.id == 101) {
            mc.displayGuiScreen(new CloneScreen(() -> {
                returnToScreen();
                updateWorldSyncButton();
            }));
        }
    }

    @Inject(at = @At("TAIL"), method = "drawScreen")
    public void render(int i, int j, float f, CallbackInfo ci) {
        if (cloneButton != null && cloneButton.isMouseOver()) drawHoveringText(Collections.singletonList(cloneButtonTooltip), i, j);
        if (worldSyncButtonTooltip != null && worldSyncButton != null &&  worldSyncButton.isMouseOver()) drawHoveringText(worldSyncButtonTooltip, i, j);

        if (worldSyncButton != null && isAltKeyDown() != prevAltState) {
            prevAltState = isAltKeyDown();

            if (isAltKeyDown()) {
                worldSyncButton.enabled = true;
                worldSyncButton.texture = ImageButton.ImageButtonTex.CLOUD;
                worldSyncButtonTooltip = Collections.singletonList(I18n.format("minegit.link.setup.open"));
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
        if (isAltKeyDown()) return;
        Config config = ConfigManager.getCurrentConfig();
        if (config.username.replace(" ", "").isEmpty() || config.getPat().replace(" ", "").isEmpty()) {
            // Set the world sync button to configuration state
            worldSyncButtonState = WorldSyncButtonState.SETUP;
            this.worldSyncButton.enabled = true;
        } else if (selectedIndex != -1 && GitManager.syncEnabled(mc, func_146621_a(selectedIndex))) {
            worldSyncButtonState = WorldSyncButtonState.WORLD_CONFIGURE;
            this.worldSyncButton.enabled = false;
        } else {
            worldSyncButtonState = WorldSyncButtonState.ENABLE;
            this.worldSyncButton.enabled = selectedIndex != -1;
        }

        worldSyncButton.texture = worldSyncButtonState.texture;
        worldSyncButtonTooltip = worldSyncButtonState.getTooltip();
        if (cloneButton != null) cloneButton.enabled = worldSyncButtonState != WorldSyncButtonState.SETUP;
    }

    @Inject(method = "func_146615_e", at = @At("HEAD"), cancellable = true)
    private void beforeWorldJoin(int id, CallbackInfo ci) {
        if (!showGitBeforeJoin) return;
        String worldId = func_146621_a(selectedIndex);
        if (!GitManager.syncEnabled(mc, worldId)) return;
        ci.cancel();
        GitProgressScreen progressScreen = new GitProgressScreen(I18n.format("minegit.sync.status.git_pull"));
        mc.displayGuiScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.pull(GitManager.getPath(mc, worldId), progressScreen);
            GitManager.makeWritable(mc, worldId);
            switch (status) {
                case SUCCESS:
                    // Success; load world as normal
                    doLoadWorld();
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    mc.addScheduledTask(() -> mc.displayGuiScreen(new GitConflictScreen(
                            this::doLoadWorld,
                            this::returnToScreen,
                            GitManager.getPath(mc, worldId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    mc.addScheduledTask(() -> mc.displayGuiScreen(new TwoChoiceScreen(
                            I18n.format("minegit.sync.pull_unreachable.title"),
                            I18n.format("minegit.sync.pull_unreachable.description"),
                            I18n.format("minegit.sync.pull_unreachable.continue"),
                            I18n.format("minegit.sync.pull_unreachable.cancel"),
                            this::doLoadWorld, // continue
                            this::returnToScreen // cancel
                    )));
                    break;
            }
        }).start();
    }

    @Unique
    private void doLoadWorld() {
        mc.addScheduledTask(() -> {
            showGitBeforeJoin = false;
            func_146615_e(selectedIndex);
            showGitBeforeJoin = true;
        });
    }

    @Unique
    private void returnToScreen() {
        mc.displayGuiScreen(this);
    }
}