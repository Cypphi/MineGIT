package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.NetworkManager;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.storage.LevelSummary;

public class EnableWorldSyncScreen extends Screen {
    private final Screen parent;
    private final LevelSummary level;
    private final Runnable closeCallback;

    private Button confirmButton;
    private Button cancelButton;
    private boolean showOpenSetupButton = false;

    public EnableWorldSyncScreen(Screen parent, LevelSummary level, Runnable closeCallback) {
        super(new TranslatableComponent("minegit.sync.enable.title"));
        this.parent = parent;
        this.level = level;
        this.closeCallback = closeCallback;
    }

    @Override
    protected void init() {
        // Confirm button
        confirmButton = new Button(width / 2 - 152, 124, 150, 20, I18n.get("minegit.sync.enable.confirm.ok"), button -> setupSync());
        addButton(confirmButton);

        // Cancel button
        cancelButton = new Button(width / 2 + 2, 124, 150, 20, I18n.get("minegit.sync.enable.confirm.cancel"), button -> onClose());
        addButton(cancelButton);

        Button openSetupButton = new Button(width / 2 - 75, 152, 150, 20, I18n.get("minegit.link.setup.open"), button -> minecraft.setScreen(new AccountLinkScreen(this.parent, closeCallback)));
        openSetupButton.visible = showOpenSetupButton;
        addButton(openSetupButton);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(i, j, f);
        drawCenteredString(this.font, this.title.getString(), this.width / 2, 50, 16777215);
        drawCenteredString(this.font, I18n.get("minegit.sync.enable.confirm.line1", level.getLevelName()), this.width / 2, 90, 16777215);
        drawCenteredString(this.font, I18n.get("minegit.sync.enable.confirm.line2"), this.width / 2, 103, 16777215);
    }

    private void setupSync() {
        confirmButton.active = false;
        cancelButton.active = false;

        GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableComponent("minegit.sync.enable.working"));
        minecraft.setScreen(progressScreen);
        new Thread(() -> {
            // Create a repository on GitHub
            Config config = ConfigManager.getCurrentConfig();
            progressScreen.beginTask("Create GitHub repository", 0);
            NetworkManager.HttpResponse response = NetworkManager.createRepo(config.getPat(), level.getLevelId(), level.getLevelName());
            int statusCode = response == null? -1 : response.statusCode();
            if (statusCode != 201) {
                // OOPS! ERROR!!
                minecraft.submit(() -> {
                    minecraft.getToasts().addToast(new WideToast(I18n.get("minegit.sync.enable.create_repo.error", statusCode)));
                    showOpenSetupButton = true;
                    cancelButton.active = true;

                    if (response != null) MineGIT.LOGGER.error(response.body());
                    minecraft.setScreen(this);
                });
                return;
            }

            String repoUrl = new JsonParser().parse(response.body()).getAsJsonObject().get("clone_url").getAsString();
            MineGIT.LOGGER.info("Successfully setup GitHub repo with URL: {}", repoUrl);

            // Git init on world save folder
            progressScreen.beginTask("Create Git repo", 0);
            boolean ok = GitManager.init(minecraft, level.getLevelId(), repoUrl, progressScreen);
            if (!ok) {
                minecraft.submit(() -> {
                    minecraft.getToasts().addToast(new WideToast(I18n.get("minegit.sync.enable.git_init.error")));
                    minecraft.setScreen(this);
                    cancelButton.active = true;
                });
                return;
            }

            minecraft.getToasts().addToast(new WideToast(I18n.get("minegit.sync.enable.complete")));
            minecraft.submit(this::onClose);
        }).start();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }
}
