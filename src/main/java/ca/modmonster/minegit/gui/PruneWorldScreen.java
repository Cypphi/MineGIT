package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.MultiLineLabel;
import ca.modmonster.minegit.backport.toast.Toast;
import ca.modmonster.minegit.backport.toast.ToastManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
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
        descriptionWidget = MultiLineLabel.create(textRenderer, I18n.translate("minegit.prune.description"), this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Confirm button
        ButtonWidget confirmButton = new ButtonWidget(0, this.width / 2 - 152, 98 + descriptionHeight, 150, 20, I18n.translate("minegit.prune.confirm"));
        buttons.add(confirmButton);

        // Cancel button
        ButtonWidget cancelButton = new ButtonWidget(1, this.width / 2 + 2, 98 + descriptionHeight, 150, 20, I18n.translate("minegit.prune.cancel"));
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
        this.drawBackgroundTexture(i);
        drawCenteredString(textRenderer, I18n.translate("minegit.prune.title"), this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(this.width / 2, 90);
        super.render(i, j, f);
    }

    private void doPrune(ProgressMonitor progress) {
        boolean ok = GitManager.prune(minecraft, levelId, progress);
        if (minecraft == null) return;
        if (ok) {
            ToastManager.INSTANCE.add(new Toast(I18n.translate("minegit.prune.complete")));
        } else {
            ToastManager.INSTANCE.add(new Toast(I18n.translate("minegit.prune.failed")));
        }
        minecraft.executeTask(() -> minecraft.openScreen(successParent));
    }

    private void pullThenPrune() {
        GitProgressScreen progressScreen = new GitProgressScreen(I18n.translate("minegit.prune.in_progress"));
        minecraft.openScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.pull(GitManager.getPath(minecraft, levelId), progressScreen);
            GitManager.makeWritable(minecraft, levelId);
            switch (status) {
                case SUCCESS:
                    // Success; load world as normal
                    doPrune(progressScreen);
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    minecraft.executeTask(() -> minecraft.openScreen(new GitConflictScreen(
                            () -> doPrune(progressScreen),
                            this::close,
                            GitManager.getPath(minecraft, levelId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    minecraft.executeTask(() -> minecraft.openScreen(new TwoChoiceScreen(
                            I18n.translate("minegit.sync.pull_unreachable.title"),
                            I18n.translate("minegit.sync.pull_unreachable.description"),
                            I18n.translate("minegit.sync.pull_unreachable.continue"),
                            I18n.translate("minegit.sync.pull_unreachable.cancel"),
                            () -> doPrune(progressScreen),
                            this::close
                    )));
                    break;
            }
        }).start();
    }

    public void close() {
        minecraft.openScreen(parent);
    }

    @Override
    protected void keyPressed(char i, int j) {
        if (j == 1) close();
    }
}
