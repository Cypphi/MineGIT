package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.MultiLineLabel;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class GitConflictScreen extends Screen {
    public GitConflictScreen(@NotNull Runnable resolvedCallback, @Nullable Runnable cancelCallback, @NotNull Path worldFolder) {
        super(new TranslatableComponent("minegit.sync.conflict.title"));
        this.resolvedCallback = resolvedCallback;
        this.cancelCallback = cancelCallback;
        this.worldFolder = worldFolder;
    }

    private final @NotNull Runnable resolvedCallback;
    private final @Nullable Runnable cancelCallback;
    private final @NotNull Path worldFolder;

    private MultiLineLabel descriptionWidget;

    @Override
    protected void init() {
        // Get latest commit dates of remote and local
        String remoteCommitDate = GitManager.getLatestRemoteCommitDate(worldFolder);
        String localCommitDate = GitManager.getLatestLocalCommitDate(worldFolder);

        // Confirmation message
        descriptionWidget = MultiLineLabel.create(this.font, I18n.get("minegit.sync.conflict.description"), this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Remote button
        Button remoteButton = new Button(this.width / 2 - 120, 108 + descriptionHeight, 240, 20, I18n.get("minegit.sync.conflict.remote") + " - " + remoteCommitDate, button -> {
            GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableComponent("minegit.sync.status.git_pull"));
            minecraft.setScreen(progressScreen);
            new Thread(() -> {
                boolean ok = GitManager.forcePull(worldFolder, progressScreen) == SyncResult.SUCCESS;
                if (ok) {
                    minecraft.submit(resolvedCallback);
                } else {
                    minecraft.submit(() -> {
                        minecraft.getToasts().addToast(new WideToast(I18n.get("minegit.sync.conflict.failed")));
                        if (cancelCallback != null) {
                            cancelCallback.run();
                        } else {
                            minecraft.setScreen(null);
                        }
                    });
                }
            }).start();
        });
        addButton(remoteButton);

        // Local button
        Button localButton = new Button(this.width / 2 - 120, 130 + descriptionHeight, 240, 20, I18n.get("minegit.sync.conflict.local") + " - " + localCommitDate, button -> {
            GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableComponent("minegit.sync.status.git_push"));
            minecraft.setScreen(progressScreen);
            new Thread(() -> {
                boolean ok = GitManager.forcePush(worldFolder, progressScreen) == SyncResult.SUCCESS;
                if (ok) {
                    minecraft.submit(resolvedCallback);
                } else {
                    minecraft.submit(() -> {
                        minecraft.getToasts().addToast(new WideToast(I18n.get("minegit.sync.conflict.failed")));
                        if (cancelCallback != null) {
                            cancelCallback.run();
                        } else {
                            minecraft.setScreen(null);
                        }
                    });
                }
            }).start();
        });
        addButton(localButton);

        // Cancel button
        if (cancelCallback != null) {
            Button cancelButton = new Button(this.width / 2 - 75, 156 + descriptionHeight, 150, 20, I18n.get("minegit.sync.conflict.cancel"), button -> cancelCallback.run());
            addButton(cancelButton);
        }
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(i, j, f);
        drawCenteredString(this.font, this.title.getString(), this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(this.width / 2, 90);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return cancelCallback != null;
    }

    @Override
    public void onClose() {
        if (cancelCallback != null) cancelCallback.run();
    }
}