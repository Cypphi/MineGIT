package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
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

    @Override
    protected void init() {
        // Get latest commit dates of remote and local
        String remoteCommitDate = GitManager.getLatestRemoteCommitDate(worldFolder);
        String localCommitDate = GitManager.getLatestLocalCommitDate(worldFolder);

        // Confirmation message
        MultiLineTextWidget descriptionWidget = MultiLineTextWidget.createCentered(this.width - 50, this.font, Component.translatable("minegit.sync.conflict.description"));
        descriptionWidget.setPosition((this.width - descriptionWidget.getWidth()) / 2, 90);
        addRenderableWidget(descriptionWidget);

        // Remote button
        Button remoteButton = Button.builder(Component.translatable("minegit.sync.conflict.remote").append(" - " + remoteCommitDate), button -> {
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
        }).width(240).build();
        remoteButton.setPosition(this.width / 2 - 120, 108 + descriptionWidget.getHeight());
        addRenderableWidget(remoteButton);

        // Local button
        Button localButton = Button.builder(Component.translatable("minegit.sync.conflict.local").append(" - " + localCommitDate), button -> {
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
        }).width(240).build();
        localButton.setPosition(this.width / 2 - 120, 130 + descriptionWidget.getHeight());
        addRenderableWidget(localButton);

        // Cancel button
        if (cancelCallback != null) {
            Button cancelButton = Button.builder(Component.translatable("minegit.sync.conflict.cancel"), button -> cancelCallback.run()).build();
            cancelButton.setPosition(this.width / 2 - 75, 156 + descriptionWidget.getHeight());
            addRenderableWidget(cancelButton);
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 16777215);
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