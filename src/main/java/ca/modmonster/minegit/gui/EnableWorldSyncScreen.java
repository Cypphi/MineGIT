package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitService;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.NetworkManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelSummary;

import java.net.http.HttpResponse;

public class EnableWorldSyncScreen extends Screen {
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 20 + 4, 60);

    private final Screen parent;
    private final LevelSummary level;
    private final Runnable closeCallback;

    private Button confirmButton;
    private Button cancelButton;
    private Button openSetupButton;
    private EditBox repoUrlEdit;
    private StringWidget repoUrlLabel;

    public EnableWorldSyncScreen(Screen parent, LevelSummary level, Runnable closeCallback) {
        super(Component.translatable("minegit.sync.enable.title"));
        this.parent = parent;
        this.level = level;
        this.closeCallback = closeCallback;
    }

    @Override
    protected void init() {
        // Column layout
        LinearLayout columnLayout = this.layout.addToContents(LinearLayout.vertical().spacing(8));
        columnLayout.defaultCellSetting().alignHorizontallyCenter();

        // Menu title
        layout.addTitleHeader(this.title, this.font);

        Config config = ConfigManager.getCurrentConfig();
        boolean needsRepoUrl = config.gitService.requiresCustomUrl();

        if (needsRepoUrl) {
            // Show repository URL input for custom services
            StringWidget message = columnLayout.addChild(new StringWidget(
                    Component.translatable("minegit.sync.enable.custom_service.message", config.gitService.getDisplayName()),
                    this.font));
            message.setAlpha(0.7f);

            repoUrlLabel = columnLayout.addChild(new StringWidget(
                    Component.translatable("minegit.sync.enable.repo_url"), this.font));
            repoUrlLabel.setAlpha(0.5f);

            repoUrlEdit = new EditBox(font, 0, 0, 200, 20, Component.translatable("minegit.sync.enable.repo_url"));
            repoUrlEdit.setMaxLength(255);
            columnLayout.addChild(repoUrlEdit);
        } else {
            // Standard GitHub/GitLab flow - show confirmation
            columnLayout.addChild(new StringWidget(Component.translatable("minegit.sync.enable.confirm.line1", level.getLevelName()), this.font));
            columnLayout.addChild(new StringWidget(Component.translatable("minegit.sync.enable.confirm.line2"), this.font));
        }

        // Confirm button
        LinearLayout buttonRowLayout = columnLayout.addChild(LinearLayout.horizontal().spacing(8));
        confirmButton = Button.builder(Component.translatable("minegit.sync.enable.confirm.ok"), button -> setupSync()).build();
        buttonRowLayout.addChild(confirmButton);

        // Cancel button
        cancelButton = Button.builder(Component.translatable("minegit.sync.enable.confirm.cancel"), button -> onClose()).build();
        buttonRowLayout.addChild(cancelButton);

        openSetupButton = Button.builder(Component.translatable("minegit.link.setup.open"), button -> minecraft.setScreen(new AccountLinkScreen(this.parent, closeCallback))).build();
        openSetupButton.visible = false;
        columnLayout.addChild(openSetupButton);

        // Add layout widgets
        this.layout.visitWidgets(this::addRenderableWidget);
        this.layout.arrangeElements();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void setupSync() {
        confirmButton.active = false;
        cancelButton.active = false;

        GitProgressScreen progressScreen = new GitProgressScreen(Component.translatable("minegit.sync.enable.working"));
        minecraft.setScreen(progressScreen);

        Config config = ConfigManager.getCurrentConfig();
        boolean needsRepoUrl = config.gitService.requiresCustomUrl();

        new Thread(() -> {
            String repoUrl = null;

            if (needsRepoUrl) {
                // For custom services, use the provided repo URL directly
                repoUrl = repoUrlEdit.getValue();
                if (!repoUrl.startsWith("http://") && !repoUrl.startsWith("https://")) {
                    repoUrl = "https://" + repoUrl;
                }
                if (!repoUrl.endsWith(".git")) {
                    repoUrl = repoUrl + ".git";
                }
            } else {
                // Create a repository on GitHub/GitLab
                progressScreen.beginTask("Create repository", 0);
                HttpResponse<String> response = NetworkManager.createRepo(config, level.getLevelId(), level.getLevelName());
                int statusCode = response == null ? -1 : response.statusCode();

                // Check for expected success codes (201 for GitHub/Gitea, 201 for GitLab)
                boolean createSuccess = (statusCode == 201) || (config.gitService == GitService.GITLAB && statusCode == 201);

                if (!createSuccess) {
                    minecraft.submit(() -> {
                        minecraft.getToastManager().addToast(new SystemToast(new SystemToast.SystemToastId(),
                                Component.translatable("minegit.sync.enable.create_repo.error", statusCode), null));
                        openSetupButton.visible = true;
                        cancelButton.active = true;
                        if (response != null) MineGIT.LOGGER.error(response.body());
                        minecraft.setScreen(this);
                    });
                    return;
                }

                repoUrl = NetworkManager.parseCloneUrl(response, config.gitService);
                if (repoUrl == null) {
                    // Fallback for services that don't return clone_url in expected format
                    repoUrl = config.buildCloneUrl("minegit_" + level.getLevelId());
                }
                MineGIT.LOGGER.info("Successfully created repo with URL: {}", repoUrl);
            }

            // Git init on world save folder
            progressScreen.beginTask("Create Git repo", 0);
            boolean ok = GitManager.init(minecraft, level.getLevelId(), repoUrl, progressScreen);
            if (!ok) {
                minecraft.submit(() -> {
                    minecraft.getToastManager().addToast(new SystemToast(new SystemToast.SystemToastId(),
                            Component.translatable("minegit.sync.enable.git_init.error"), null));
                    minecraft.setScreen(this);
                    cancelButton.active = true;
                });
                return;
            }

            minecraft.getToastManager().addToast(new SystemToast(new SystemToast.SystemToastId(),
                    Component.translatable("minegit.sync.enable.complete"), null));
            minecraft.submit(this::onClose);
        }).start();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
    }
}