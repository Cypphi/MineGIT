package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.MainThreadTasks;
import ca.modmonster.minegit.backport.MultiLineLabel;
import ca.modmonster.minegit.backport.toast.Toast;
import ca.modmonster.minegit.backport.toast.ToastManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class GitConflictScreen extends Screen {
    public GitConflictScreen(@NotNull Runnable resolvedCallback, @Nullable Runnable cancelCallback, @NotNull Path worldFolder) {
        this.resolvedCallback = resolvedCallback;
        this.cancelCallback = cancelCallback;
        this.worldFolder = worldFolder;
    }

    private final @NotNull Runnable resolvedCallback;
    private final @Nullable Runnable cancelCallback;
    private final @NotNull Path worldFolder;

    private MultiLineLabel descriptionWidget;

    @Override
    public void init() {
        // Get latest commit dates of remote and local
        String remoteCommitDate = GitManager.getLatestRemoteCommitDate(worldFolder);
        String localCommitDate = GitManager.getLatestLocalCommitDate(worldFolder);

        // Confirmation message
        descriptionWidget = MultiLineLabel.create(textRenderer, "The version of the world saved on your device conflicts with the one saved in the cloud. Whichever world data you choose to keep will be synced to this device and the cloud; the other will be overwritten.", this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Remote button
        ButtonWidget remoteButton = new ButtonWidget(0, this.width / 2 - 120, 108 + descriptionHeight, 240, 20, "Cloud save - " + remoteCommitDate);
        buttons.add(remoteButton);

        // Local button
        ButtonWidget localButton = new ButtonWidget(1, this.width / 2 - 120, 130 + descriptionHeight, 240, 20, "Local save - " + localCommitDate);
        buttons.add(localButton);

        // Cancel button
        if (cancelCallback != null) {
            ButtonWidget cancelButton = new ButtonWidget(2, this.width / 2 - 75, 156 + descriptionHeight, 150, 20, "Cancel loading world");
            buttons.add(cancelButton);
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 0) {
            GitProgressScreen progressScreen = new GitProgressScreen("Pulling from GitHub...");
            minecraft.setScreen(progressScreen);
            new Thread(() -> {
                boolean ok = GitManager.forcePull(worldFolder, progressScreen) == SyncResult.SUCCESS;
                if (ok) {
                    MainThreadTasks.execute(resolvedCallback);
                } else {
                    MainThreadTasks.execute(() -> {
                        ToastManager.INSTANCE.add(new Toast("Resolving sync conflict failed. You may need to delete the world and re-clone."));
                        if (cancelCallback != null) {
                            cancelCallback.run();
                        } else {
                            minecraft.setScreen(null);
                        }
                    });
                }
            }).start();
        } else if (button.id == 1) {
            GitProgressScreen progressScreen = new GitProgressScreen("Pushing to GitHub...");
            minecraft.setScreen(progressScreen);
            new Thread(() -> {
                boolean ok = GitManager.forcePush(worldFolder, progressScreen) == SyncResult.SUCCESS;
                if (ok) {
                    MainThreadTasks.execute(resolvedCallback);
                } else {
                    MainThreadTasks.execute(() -> {
                        ToastManager.INSTANCE.add(new Toast("Resolving sync conflict failed. You may need to delete the world and re-clone."));
                        if (cancelCallback != null) {
                            cancelCallback.run();
                        } else {
                            minecraft.setScreen(null);
                        }
                    });
                }
            }).start();
        } else if (button.id == 3) {
            cancelCallback.run();
        }
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackgroundTexture(i);
        drawCenteredTextWithShadow(textRenderer, "Error syncing world", this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(this.width / 2, 90);
        super.render(i, j, f);
    }

    public void close() {
        if (cancelCallback != null) cancelCallback.run();
    }

    @Override
    protected void keyPressed(char i, int j) {
        if (j == 1 && cancelCallback != null) close();
    }
}