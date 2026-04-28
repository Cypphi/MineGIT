package ca.modmonster.minegit.gui;

import com.google.gson.JsonParser;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelSummary;

import java.net.http.HttpResponse;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.NetworkManager;

public class EnableWorldSyncScreen extends Screen {
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 20 + 4, 60);

    private final Screen parent;
    private final LevelSummary level;
    private final Runnable closeCallback;

    private Button confirmButton;
    private Button cancelButton;
    private Button openSetupButton;

    public EnableWorldSyncScreen(Screen parent, LevelSummary level, Runnable closeCallback) {
        super(Component.translatable("minegit.sync.enable.title"));
        this.parent = parent;
        this.level = level;
        this.closeCallback = closeCallback;
    }

    @Override
    protected void init() {
        // Column layout
        GridLayout columnLayout = this.layout.addToContents(new GridLayout().spacing(8));
        columnLayout.defaultCellSetting().alignHorizontallyCenter();

        // Menu title
        layout.addToHeader(new StringWidget(this.title, this.font));

        // Confirmation message
        columnLayout.addChild(new StringWidget(Component.translatable("minegit.sync.enable.confirm.line1", level.getLevelName()), this.font), 0, 0);
        columnLayout.addChild(new StringWidget(Component.translatable("minegit.sync.enable.confirm.line2"), this.font), 1, 0);

        // Confirm button
        GridLayout buttonRowLayout = columnLayout.addChild(new GridLayout().spacing(8), 2, 0);
        confirmButton = Button.builder(Component.translatable("minegit.sync.enable.confirm.ok"), button -> setupSync()).build();
        buttonRowLayout.addChild(confirmButton, 0, 0);

        // Cancel button
        cancelButton = Button.builder(Component.translatable("minegit.sync.enable.confirm.cancel"), button -> onClose()).build();
        buttonRowLayout.addChild(cancelButton, 0, 1);

        openSetupButton = Button.builder(Component.translatable("minegit.link.setup.open"), button -> minecraft.setScreen(new AccountLinkScreen(this.parent, closeCallback))).build();
        openSetupButton.visible = false;
        columnLayout.addChild(openSetupButton, 3, 0);

        // Add layout widgets
        this.layout.visitWidgets(this::addRenderableWidget);
        this.layout.arrangeElements();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderDirtBackground(guiGraphics);
        super.render(guiGraphics, i, j, f);
    }

    private void setupSync() {
        confirmButton.active = false;
        cancelButton.active = false;

        GitProgressScreen progressScreen = new GitProgressScreen(Component.translatable("minegit.sync.enable.working"));
        minecraft.setScreen(progressScreen);
        new Thread(() -> {
            // Create a repository on GitHub
            Config config = ConfigManager.getCurrentConfig();
            progressScreen.beginTask("Create GitHub repository", 0);
            HttpResponse<String> response = NetworkManager.createRepo(config.getPat(), level.getLevelId(), level.getLevelName());
            int statusCode = response == null? -1 : response.statusCode();
            if (statusCode != 201) {
                // OOPS! ERROR!!
                minecraft.submit(() -> {
                    SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("minegit.sync.enable.create_repo.error", statusCode), null);
                    openSetupButton.visible = true;
                    cancelButton.active = true;

                    if (response != null) MineGIT.LOGGER.error(response.body());
                    minecraft.setScreen(this);
                });
                return;
            }

            String repoUrl = JsonParser.parseString(response.body()).getAsJsonObject().get("clone_url").getAsString();
            MineGIT.LOGGER.info("Successfully setup GitHub repo with URL: {}", repoUrl);

            // Git init on world save folder
            progressScreen.beginTask("Create Git repo", 0);
            boolean ok = GitManager.init(minecraft, level.getLevelId(), repoUrl, progressScreen);
            if (!ok) {
                minecraft.submit(() -> {
                    SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("minegit.sync.enable.git_init.error"), null);
                    minecraft.setScreen(this);
                    cancelButton.active = true;
                });
                return;
            }

            SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("minegit.sync.enable.complete"), null);
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
