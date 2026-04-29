package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class GitConflictScreen extends Screen {
    public GitConflictScreen(@NotNull Runnable resolvedCallback, @Nullable Runnable cancelCallback, @NotNull Path worldFolder) {
        super(Component.translatable("minegit.sync.conflict.title"));
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
        descriptionWidget = MultiLineLabel.create(this.font, Component.translatable("minegit.sync.conflict.description"), this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Remote button
        Button remoteButton = new Button(this.width / 2 - 120, 108 + descriptionHeight, 240, 20, Component.translatable("minegit.sync.conflict.remote").append(" - " + remoteCommitDate), button -> {
            GitProgressScreen progressScreen = new GitProgressScreen(Component.translatable("minegit.sync.status.git_pull"));
            minecraft.setScreen(progressScreen);
            new Thread(() -> {
                boolean ok = GitManager.forcePull(worldFolder, progressScreen) == SyncResult.SUCCESS;
                if (ok) {
                    minecraft.submit(resolvedCallback);
                } else {
                    minecraft.submit(() -> {
                        SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("minegit.sync.conflict.failed"), null);
                        if (cancelCallback != null) {
                            cancelCallback.run();
                        } else {
                            minecraft.setScreen(null);
                        }
                    });
                }
            }).start();
        });
        addRenderableWidget(remoteButton);

        // Local button
        Button localButton = new Button(this.width / 2 - 120, 130 + descriptionHeight, 240, 20, Component.translatable("minegit.sync.conflict.local").append(" - " + localCommitDate), button -> {
            GitProgressScreen progressScreen = new GitProgressScreen(Component.translatable("minegit.sync.status.git_push"));
            minecraft.setScreen(progressScreen);
            new Thread(() -> {
                boolean ok = GitManager.forcePush(worldFolder, progressScreen) == SyncResult.SUCCESS;
                if (ok) {
                    minecraft.submit(resolvedCallback);
                } else {
                    minecraft.submit(() -> {
                        SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("minegit.sync.conflict.failed"), null);
                        if (cancelCallback != null) {
                            cancelCallback.run();
                        } else {
                            minecraft.setScreen(null);
                        }
                    });
                }
            }).start();
        });
        addRenderableWidget(localButton);

        // Cancel button
        if (cancelCallback != null) {
            Button cancelButton = new Button(this.width / 2 - 75, 156 + descriptionHeight, 150, 20, Component.translatable("minegit.sync.conflict.cancel"), button -> cancelCallback.run());
            addRenderableWidget(cancelButton);
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(poseStack, this.width / 2, 90);
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