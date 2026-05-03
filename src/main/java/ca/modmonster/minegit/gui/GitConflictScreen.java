package ca.modmonster.minegit.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.nio.file.Path;

import ca.modmonster.minegit.backport.MultiLineLabel;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;

public class GitConflictScreen extends GuiScreen {
    public GitConflictScreen(Runnable resolvedCallback, Runnable cancelCallback, Path worldFolder) {
        this.resolvedCallback = resolvedCallback;
        this.cancelCallback = cancelCallback;
        this.worldFolder = worldFolder;
    }

    private final Runnable resolvedCallback;
    private final Runnable cancelCallback;
    private final Path worldFolder;

    private MultiLineLabel descriptionWidget;

    @Override
    public void initGui() {
        // Get latest commit dates of remote and local
        String remoteCommitDate = GitManager.getLatestRemoteCommitDate(worldFolder);
        String localCommitDate = GitManager.getLatestLocalCommitDate(worldFolder);

        // Confirmation message
        descriptionWidget = MultiLineLabel.create(fontRenderer, I18n.format("minegit.sync.conflict.description"), this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Remote button
        GuiButton remoteButton = new GuiButton(0, this.width / 2 - 120, 108 + descriptionHeight, 240, 20, I18n.format("minegit.sync.conflict.remote") + " - " + remoteCommitDate);
        addButton(remoteButton);

        // Local button
        GuiButton localButton = new GuiButton(1, this.width / 2 - 120, 130 + descriptionHeight, 240, 20, I18n.format("minegit.sync.conflict.local") + " - " + localCommitDate);
        addButton(localButton);

        // Cancel button
        if (cancelCallback != null) {
            GuiButton cancelButton = new GuiButton(3, this.width / 2 - 75, 156 + descriptionHeight, 150, 20, I18n.format("minegit.sync.conflict.cancel"));
            addButton(cancelButton);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            GitProgressScreen progressScreen = new GitProgressScreen(I18n.format("minegit.sync.status.git_pull"));
            mc.displayGuiScreen(progressScreen);
            new Thread(() -> {
                boolean ok = GitManager.forcePull(worldFolder, progressScreen) == SyncResult.SUCCESS;
                if (ok) {
                    mc.addScheduledTask(resolvedCallback);
                } else {
                    mc.addScheduledTask(() -> {
                        mc.getToastGui().add(new WideToast(I18n.format("minegit.sync.conflict.failed")));
                        if (cancelCallback != null) {
                            cancelCallback.run();
                        } else {
                            mc.displayGuiScreen(null);
                        }
                    });
                }
            }).start();
        } else if (button.id == 1) {
            GitProgressScreen progressScreen = new GitProgressScreen(I18n.format("minegit.sync.status.git_push"));
            mc.displayGuiScreen(progressScreen);
            new Thread(() -> {
                boolean ok = GitManager.forcePush(worldFolder, progressScreen) == SyncResult.SUCCESS;
                if (ok) {
                    mc.addScheduledTask(resolvedCallback);
                } else {
                    mc.addScheduledTask(() -> {
                        mc.getToastGui().add(new WideToast(I18n.format("minegit.sync.conflict.failed")));
                        if (cancelCallback != null) {
                            cancelCallback.run();
                        } else {
                            mc.displayGuiScreen(null);
                        }
                    });
                }
            }).start();
        } else if (button.id == 3) {
            cancelCallback.run();
        }
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        this.drawDefaultBackground();
        super.drawScreen(i, j, f);
        drawCenteredString(fontRenderer, I18n.format("minegit.sync.conflict.title"), this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(this.width / 2, 90);
    }

    public void close() {
        if (cancelCallback != null) cancelCallback.run();
    }

    @Override
    protected void keyTyped(char i, int j) {
        if (j == 1 && cancelCallback != null) close();
    }
}