package ca.modmonster.minegit.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;

public class GitConflictScreen extends Screen {
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 20 + 4, 60);

    public GitConflictScreen(@NotNull Runnable resolvedCallback, @Nullable Runnable cancelCallback, @NotNull Path worldFolder) {
        super(Component.translatable("minegit.sync.conflict.title"));
        this.resolvedCallback = resolvedCallback;
        this.cancelCallback = cancelCallback;
        this.worldFolder = worldFolder;
    }

    private final @NotNull Runnable resolvedCallback;
    private final @Nullable Runnable cancelCallback;
    private final @NotNull Path worldFolder;

    private MultiLineTextWidget descriptionWidget;

    @Override
    protected void init() {
        // Get latest commit dates of remote and local
        String remoteCommitDate = GitManager.getLatestRemoteCommitDate(worldFolder);
        String localCommitDate = GitManager.getLatestLocalCommitDate(worldFolder);

        // Column layout
        GridLayout columnLayout = this.layout.addToContents(new GridLayout().spacing(2));
        columnLayout.defaultCellSetting().alignHorizontallyCenter();

        // Menu title
        layout.addToHeader(new StringWidget(this.title, this.font));

        // Confirmation message
        descriptionWidget = new MultiLineTextWidget(Component.translatable("minegit.sync.conflict.description"), this.font).setMaxWidth(this.width - 50);
        columnLayout.addChild(descriptionWidget, 0, 0);
        columnLayout.addChild(new SpacerElement(200, 14), 1, 0);

        // Remote button
        Button remoteButton = Button.builder(Component.translatable("minegit.sync.conflict.remote").append(" - " + remoteCommitDate), button -> {
            GitProgressScreen progressScreen = new GitProgressScreen(Component.translatable("minegit.sync.status.git_pull", ConfigManager.getCurrentConfig().gitService.getNaturalName()));
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
        columnLayout.addChild(remoteButton, 2, 0);

        // Local button
        Button localButton = Button.builder(Component.translatable("minegit.sync.conflict.local").append(" - " + localCommitDate), button -> {
            GitProgressScreen progressScreen = new GitProgressScreen(Component.translatable("minegit.sync.status.git_push", ConfigManager.getCurrentConfig().gitService.getNaturalName()));
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
        columnLayout.addChild(localButton, 3, 0);

        // Cancel button
        if (cancelCallback != null) {
            columnLayout.addChild(new SpacerElement(200, 6), 4, 0);
            Button cancelButton = Button.builder(Component.translatable("minegit.sync.conflict.cancel"), button -> cancelCallback.run()).build();
            columnLayout.addChild(cancelButton, 5, 0);
        }

        // Add layout widgets
        this.layout.visitWidgets(this::addRenderableWidget);
        this.layout.arrangeElements();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderDirtBackground(guiGraphics);
        super.render(guiGraphics, i, j, f);
    }

    @Override
    protected void repositionElements() {
        if (descriptionWidget != null) descriptionWidget.setMaxWidth(this.width - 50);
        layout.arrangeElements();
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