package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.eclipse.jgit.lib.ProgressMonitor;

import java.io.IOException;

public class PruneWorldScreen extends Screen {
    private final Screen parent;
    private final LevelStorageSource.LevelStorageAccess levelAccess;
    private final BooleanConsumer callback;

    public PruneWorldScreen(Screen parent, LevelStorageSource.LevelStorageAccess levelAccess, BooleanConsumer callback) {
        super(Component.translatable("minegit.prune.title"));
        this.parent = parent;
        this.levelAccess = levelAccess;
        this.callback = callback;
    }

    @Override
    protected void init() {
        // Confirmation message
        MultiLineTextWidget descriptionWidget = MultiLineTextWidget.createCentered(this.width - 50, this.font, Component.translatable("minegit.prune.description"));
        descriptionWidget.setPosition((this.width - descriptionWidget.getWidth()) / 2, 90);
        addRenderableWidget(descriptionWidget);

        // Confirm button
        Button confirmButton = Button.builder(Component.translatable("minegit.prune.confirm"), button -> pullThenPrune()).build();
        confirmButton.setPosition(this.width / 2 - 152, 98 + descriptionWidget.getHeight());
        addRenderableWidget(confirmButton);

        // Cancel button
        Button cancelButton = Button.builder(Component.translatable("minegit.prune.cancel"), button -> onClose()).build();
        cancelButton.setPosition(this.width / 2 + 2, 98 + descriptionWidget.getHeight());
        addRenderableWidget(cancelButton);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 16777215);
    }

    private void doPrune(ProgressMonitor progress) {
        String worldId = levelAccess.getLevelId();
        boolean ok = GitManager.prune(minecraft, worldId, progress);
        if (minecraft == null) return;
        if (ok) {
            SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("minegit.prune.complete"), null);
        } else {
            SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("minegit.prune.failed"), null);
        }
        minecraft.submit(() -> this.callback.accept(true));
    }

    private void pullThenPrune() {
        try {
            levelAccess.close();
        } catch (IOException e) {
            MineGIT.LOGGER.warn("Failed to unlock access to level {}", levelAccess.getLevelId(), e);
        }
        String worldId = levelAccess.getLevelId();

        GitProgressScreen progressScreen = new GitProgressScreen(Component.translatable("minegit.prune.in_progress"));
        minecraft.setScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.pull(GitManager.getPath(minecraft, worldId), progressScreen);
            GitManager.makeWritable(minecraft, worldId);
            switch (status) {
                case SUCCESS:
                    // Success; load world as normal
                    doPrune(progressScreen);
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    minecraft.submit(() -> minecraft.setScreen(new GitConflictScreen(
                            () -> doPrune(progressScreen),
                            this::onClose,
                            GitManager.getPath(minecraft, worldId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    minecraft.submit(() -> minecraft.setScreen(new TwoChoiceScreen(
                            Component.translatable("minegit.sync.pull_unreachable.title"),
                            Component.translatable("minegit.sync.pull_unreachable.description"),
                            Component.translatable("minegit.sync.pull_unreachable.continue"),
                            Component.translatable("minegit.sync.pull_unreachable.cancel"),
                            () -> doPrune(progressScreen),
                            this::onClose
                    )));
                    break;
            }
        }).start();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
