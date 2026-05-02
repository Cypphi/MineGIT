package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.MultiLineLabel;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.nio.file.Path;

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
    protected void initGui() {
        // Get latest commit dates of remote and local
        String remoteCommitDate = GitManager.getLatestRemoteCommitDate(worldFolder);
        String localCommitDate = GitManager.getLatestLocalCommitDate(worldFolder);

        // Confirmation message
        descriptionWidget = MultiLineLabel.create(fontRenderer, I18n.format("minegit.sync.conflict.description"), this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Remote button
        GuiButton remoteButton = new GuiButton(0, this.width / 2 - 120, 108 + descriptionHeight, 240, 20, I18n.format("minegit.sync.conflict.remote") + " - " + remoteCommitDate) {
            @Override
            public void onClick(double mouseX, double mouseY) {
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
            }
        };
        addButton(remoteButton);

        // Local button
        GuiButton localButton = new GuiButton(1, this.width / 2 - 120, 130 + descriptionHeight, 240, 20, I18n.format("minegit.sync.conflict.local") + " - " + localCommitDate) {
            @Override
            public void onClick(double mouseX, double mouseY) {
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
            }
        };
        addButton(localButton);

        // Cancel button
        if (cancelCallback != null) {
            GuiButton cancelButton = new GuiButton(3, this.width / 2 - 75, 156 + descriptionHeight, 150, 20, I18n.format("minegit.sync.conflict.cancel")) {
                @Override
                public void onClick(double mouseX, double mouseY) {
                    cancelCallback.run();
                }
            };
            addButton(cancelButton);
        }
    }

    @Override
    public void render(int i, int j, float f) {
        this.drawDefaultBackground();
        super.render(i, j, f);
        drawCenteredString(fontRenderer, I18n.format("minegit.sync.conflict.title"), this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(this.width / 2, 90);
    }

    @Override
    public boolean allowCloseWithEscape() {
        return cancelCallback != null;
    }

    @Override
    public void close() {
        if (cancelCallback != null) cancelCallback.run();
    }
}