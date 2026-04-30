package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.eclipse.jgit.lib.ProgressMonitor;

import java.io.IOException;

public class PruneWorldScreen extends Screen {
    private final Screen parent;
    private final LevelStorageSource.LevelStorageAccess levelAccess;
    private final BooleanConsumer callback;

    private MultiLineLabel descriptionWidget;

    public PruneWorldScreen(Screen parent, LevelStorageSource.LevelStorageAccess levelAccess, BooleanConsumer callback) {
        super(new TranslatableComponent("minegit.prune.title"));
        this.parent = parent;
        this.levelAccess = levelAccess;
        this.callback = callback;
    }

    @Override
    protected void init() {
        // Confirmation message
        descriptionWidget = MultiLineLabel.create(this.font, new TranslatableComponent("minegit.prune.description"), this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Confirm button
        Button confirmButton = new Button(this.width / 2 - 152, 98 + descriptionHeight, 150, 20, new TranslatableComponent("minegit.prune.confirm"), button -> pullThenPrune());
        addButton(confirmButton);

        // Cancel button
        Button cancelButton = new Button(this.width / 2 + 2, 98 + descriptionHeight, 150, 20, new TranslatableComponent("minegit.prune.cancel"), button -> onClose());
        addButton(cancelButton);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(poseStack, this.width / 2, 90);
    }

    private void doPrune(ProgressMonitor progress) {
        String worldId = levelAccess.getLevelId();
        boolean ok = GitManager.prune(minecraft, worldId, progress);
        if (minecraft == null) return;
        if (ok) {
            minecraft.getToasts().addToast(WideToast.get(font, new TranslatableComponent("minegit.prune.complete")));
        } else {
            minecraft.getToasts().addToast(WideToast.get(font, new TranslatableComponent("minegit.prune.failed")));
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

        GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableComponent("minegit.prune.in_progress"));
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
                            new TranslatableComponent("minegit.sync.pull_unreachable.title"),
                            new TranslatableComponent("minegit.sync.pull_unreachable.description"),
                            new TranslatableComponent("minegit.sync.pull_unreachable.continue"),
                            new TranslatableComponent("minegit.sync.pull_unreachable.cancel"),
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
