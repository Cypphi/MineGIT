package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.MultiLineLabel;
import ca.modmonster.minegit.backport.toast.Toast;
import ca.modmonster.minegit.backport.toast.ToastManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.eclipse.jgit.lib.ProgressMonitor;

public class PruneWorldScreen extends GuiScreen {
    private final GuiScreen parent;
    private final String levelId;
    private final GuiScreen successParent;

    private MultiLineLabel descriptionWidget;

    public PruneWorldScreen(GuiScreen parent, String levelId, GuiScreen successParent) {
        this.parent = parent;
        this.levelId = levelId;
        this.successParent = successParent;
    }

    @Override
    public void initGui() {
        // Confirmation message
        descriptionWidget = MultiLineLabel.create(fontRendererObj, I18n.format("minegit.prune.description"), this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Confirm button
        GuiButton confirmButton = new GuiButton(0, this.width / 2 - 152, 98 + descriptionHeight, 150, 20, I18n.format("minegit.prune.confirm"));
        buttonList.add(confirmButton);

        // Cancel button
        GuiButton cancelButton = new GuiButton(1, this.width / 2 + 2, 98 + descriptionHeight, 150, 20, I18n.format("minegit.prune.cancel"));
        buttonList.add(cancelButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            pullThenPrune();
        } else if (button.id == 1) {
            close();
        }
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        this.drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("minegit.prune.title"), this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(this.width / 2, 90);
        super.drawScreen(i, j, f);
    }

    private void doPrune(ProgressMonitor progress) {
        boolean ok = GitManager.prune(mc, levelId, progress);
        if (mc == null) return;
        if (ok) {
            ToastManager.INSTANCE.add(new Toast(I18n.format("minegit.prune.complete")));
        } else {
            ToastManager.INSTANCE.add(new Toast(I18n.format("minegit.prune.failed")));
        }
        mc.addScheduledTask(() -> mc.displayGuiScreen(successParent));
    }

    private void pullThenPrune() {
        GitProgressScreen progressScreen = new GitProgressScreen(I18n.format("minegit.prune.in_progress"));
        mc.displayGuiScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.pull(GitManager.getPath(mc, levelId), progressScreen);
            GitManager.makeWritable(mc, levelId);
            switch (status) {
                case SUCCESS:
                    // Success; load world as normal
                    doPrune(progressScreen);
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    mc.addScheduledTask(() -> mc.displayGuiScreen(new GitConflictScreen(
                            () -> doPrune(progressScreen),
                            this::close,
                            GitManager.getPath(mc, levelId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    mc.addScheduledTask(() -> mc.displayGuiScreen(new TwoChoiceScreen(
                            I18n.format("minegit.sync.pull_unreachable.title"),
                            I18n.format("minegit.sync.pull_unreachable.description"),
                            I18n.format("minegit.sync.pull_unreachable.continue"),
                            I18n.format("minegit.sync.pull_unreachable.cancel"),
                            () -> doPrune(progressScreen),
                            this::close
                    )));
                    break;
            }
        }).start();
    }

    public void close() {
        mc.displayGuiScreen(parent);
    }

    @Override
    protected void keyTyped(char i, int j) {
        if (j == 1) close();
    }
}
