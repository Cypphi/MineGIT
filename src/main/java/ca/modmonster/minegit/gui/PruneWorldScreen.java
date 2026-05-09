package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.MainThreadTasks;
import ca.modmonster.minegit.backport.MultiLineLabel;
import ca.modmonster.minegit.backport.toast.Toast;
import ca.modmonster.minegit.backport.toast.ToastManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.eclipse.jgit.lib.ProgressMonitor;

public class PruneWorldScreen extends Screen {
    private final Screen parent;
    private final String levelId;
    private final Screen successParent;

    private MultiLineLabel descriptionWidget;

    public PruneWorldScreen(Screen parent, String levelId, Screen successParent) {
        this.parent = parent;
        this.levelId = levelId;
        this.successParent = successParent;
    }

    @Override
    public void init() {
        // Confirmation message
        descriptionWidget = MultiLineLabel.create(textRenderer, "This will delete all but the most recent version of your world both locally and from cloud storage. You will no longer be able to restore old snapshots of your world, but it will reduce the world's file size.", this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Confirm button
        ButtonWidget confirmButton = new ButtonWidget(0, this.width / 2 - 152, 98 + descriptionHeight, 150, 20, "Confirm");
        buttons.add(confirmButton);

        // Cancel button
        ButtonWidget cancelButton = new ButtonWidget(1, this.width / 2 + 2, 98 + descriptionHeight, 150, 20, "Cancel");
        buttons.add(cancelButton);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 0) {
            pullThenPrune();
        } else if (button.id == 1) {
            close();
        }
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackgroundTexture(i);
        drawCenteredTextWithShadow(textRenderer, "Prune World Commits", this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(this.width / 2, 90);
        super.render(i, j, f);
    }

    private void doPrune(ProgressMonitor progress) {
        boolean ok = GitManager.prune(levelId, progress);
        if (minecraft == null) return;
        if (ok) {
            ToastManager.INSTANCE.add(new Toast("World pruning complete!"));
        } else {
            ToastManager.INSTANCE.add(new Toast("Failed to prune world. Please see game console for details"));
        }
        MainThreadTasks.execute(() -> minecraft.setScreen(successParent));
    }

    private void pullThenPrune() {
        GitProgressScreen progressScreen = new GitProgressScreen("Pruning world...");
        minecraft.setScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.pull(GitManager.getPath(levelId), progressScreen);
            GitManager.makeWritable(levelId);
            switch (status) {
                case SUCCESS:
                    // Success; load world as normal
                    doPrune(progressScreen);
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    MainThreadTasks.execute(() -> minecraft.setScreen(new GitConflictScreen(
                            () -> doPrune(progressScreen),
                            this::close,
                            GitManager.getPath(levelId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    MainThreadTasks.execute(() -> minecraft.setScreen(new TwoChoiceScreen(
                            "Error syncing world",
                            "Your latest world changes could not be synced with the cloud. You can continue to load the world if you wish, but you might not have the latest version of your world.",
                            "Load without syncing",
                            "Cancel",
                            () -> doPrune(progressScreen),
                            this::close
                    )));
                    break;
            }
        }).start();
    }

    public void close() {
        minecraft.setScreen(parent);
    }

    @Override
    protected void keyPressed(char i, int j) {
        if (j == 1) close();
    }
}
