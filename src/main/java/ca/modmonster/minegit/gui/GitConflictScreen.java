package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.MultiLineLabel;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class GitConflictScreen extends Screen {
    public GitConflictScreen(@NotNull Runnable resolvedCallback, @Nullable Runnable cancelCallback, @NotNull Path worldFolder) {
        super(new TranslatableText("minegit.sync.conflict.title"));
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
        descriptionWidget = MultiLineLabel.create(this.font, I18n.translate("minegit.sync.conflict.description"), this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Remote button
        ButtonWidget remoteButton = new ButtonWidget(this.width / 2 - 120, 108 + descriptionHeight, 240, 20, I18n.translate("minegit.sync.conflict.remote") + " - " + remoteCommitDate, button -> {
            GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableText("minegit.sync.status.git_pull"));
            minecraft.openScreen(progressScreen);
            new Thread(() -> {
                boolean ok = GitManager.forcePull(worldFolder, progressScreen) == SyncResult.SUCCESS;
                if (ok) {
                    minecraft.execute(resolvedCallback);
                } else {
                    minecraft.execute(() -> {
                        minecraft.getToastManager().add(new WideToast(I18n.translate("minegit.sync.conflict.failed")));
                        if (cancelCallback != null) {
                            cancelCallback.run();
                        } else {
                            minecraft.openScreen(null);
                        }
                    });
                }
            }).start();
        });
        addButton(remoteButton);

        // Local button
        ButtonWidget localButton = new ButtonWidget(this.width / 2 - 120, 130 + descriptionHeight, 240, 20, I18n.translate("minegit.sync.conflict.local") + " - " + localCommitDate, button -> {
            GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableText("minegit.sync.status.git_push"));
            minecraft.openScreen(progressScreen);
            new Thread(() -> {
                boolean ok = GitManager.forcePush(worldFolder, progressScreen) == SyncResult.SUCCESS;
                if (ok) {
                    minecraft.execute(resolvedCallback);
                } else {
                    minecraft.execute(() -> {
                        minecraft.getToastManager().add(new WideToast(I18n.translate("minegit.sync.conflict.failed")));
                        if (cancelCallback != null) {
                            cancelCallback.run();
                        } else {
                            minecraft.openScreen(null);
                        }
                    });
                }
            }).start();
        });
        addButton(localButton);

        // Cancel button
        if (cancelCallback != null) {
            ButtonWidget cancelButton = new ButtonWidget(this.width / 2 - 75, 156 + descriptionHeight, 150, 20, I18n.translate("minegit.sync.conflict.cancel"), button -> cancelCallback.run());
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