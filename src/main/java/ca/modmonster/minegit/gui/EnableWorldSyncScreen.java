package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.backport.toast.Toast;
import ca.modmonster.minegit.backport.toast.ToastManager;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.NetworkManager;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.locale.I18n;
import net.minecraft.world.storage.WorldSaveInfo;

public class EnableWorldSyncScreen extends Screen {
    private final Screen parent;
    private final WorldSaveInfo level;
    private final Runnable closeCallback;

    private ButtonWidget confirmButton;
    private ButtonWidget cancelButton;
    private boolean showOpenSetupButton = false;

    public EnableWorldSyncScreen(Screen parent, WorldSaveInfo level, Runnable closeCallback) {
        this.parent = parent;
        this.level = level;
        this.closeCallback = closeCallback;
    }

    @Override
    public void init() {
        // Confirm button
        confirmButton = new ButtonWidget(0, width / 2 - 152, 124, 150, 20, I18n.translate("minegit.sync.enable.confirm.ok"));
        buttons.add(confirmButton);

        // Cancel button
        cancelButton = new ButtonWidget(1, width / 2 + 2, 124, 150, 20, I18n.translate("minegit.sync.enable.confirm.cancel"));
        buttons.add(cancelButton);

        ButtonWidget openSetupButton = new ButtonWidget(2, width / 2 - 75, 152, 150, 20, I18n.translate("minegit.link.setup.open"));
        openSetupButton.visible = showOpenSetupButton;
        buttons.add(openSetupButton);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 0) {
            setupSync();
        } else if (button.id == 1) {
            close();
        } else if (button.id == 2) {
            minecraft.openScreen(new AccountLinkScreen(EnableWorldSyncScreen.this.parent, closeCallback));
        }
    }

    @Override
    public void render(int i, int j, float f) {
        this.drawBackgroundTexture(i);
        drawCenteredString(this.textRenderer, I18n.translate("minegit.sync.enable.title"), this.width / 2, 50, 16777215);
        drawCenteredString(this.textRenderer, I18n.translate("minegit.sync.enable.confirm.line1", level.getName()), this.width / 2, 90, 16777215);
        drawCenteredString(this.textRenderer, I18n.translate("minegit.sync.enable.confirm.line2"), this.width / 2, 103, 16777215);
        super.render(i, j, f);
    }

    private void setupSync() {
        confirmButton.active = false;
        cancelButton.active = false;

        GitProgressScreen progressScreen = new GitProgressScreen(I18n.translate("minegit.sync.enable.working"));
        minecraft.openScreen(progressScreen);
        new Thread(() -> {
            // Create a repository on GitHub
            Config config = ConfigManager.getCurrentConfig();
            progressScreen.beginTask("Create GitHub repository", 0);
            NetworkManager.HttpResponse response = NetworkManager.createRepo(config.getPat(), level.getSaveName(), level.getName());
            int statusCode = response == null? -1 : response.statusCode();
            if (statusCode != 201) {
                // OOPS! ERROR!!
                minecraft.execute(() -> {
                    ToastManager.INSTANCE.add(new Toast(I18n.translate("minegit.sync.enable.create_repo.error", statusCode)));
                    showOpenSetupButton = true;
                    cancelButton.active = true;

                    if (response != null) MineGIT.LOGGER.error(response.body());
                    minecraft.openScreen(this);
                });
                return;
            }

            @SuppressWarnings("deprecation")
            String repoUrl = new JsonParser().parse(response.body()).getAsJsonObject().get("clone_url").getAsString();
            MineGIT.LOGGER.info("Successfully setup GitHub repo with URL: {}", repoUrl);

            // Git init on world save folder
            progressScreen.beginTask("Create Git repo", 0);
            boolean ok = GitManager.init(minecraft, level.getSaveName(), repoUrl, progressScreen);
            if (!ok) {
                minecraft.execute(() -> {
                    ToastManager.INSTANCE.add(new Toast(I18n.translate("minegit.sync.enable.git_init.error")));
                    minecraft.openScreen(this);
                    cancelButton.active = true;
                });
                return;
            }

            ToastManager.INSTANCE.add(new Toast(I18n.translate("minegit.sync.enable.complete")));
            minecraft.execute(this::close);
        }).start();
    }

    public void close() {
        minecraft.openScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }

    @Override
    protected void keyPressed(char i, int j) {
        if (j == 1) close();
    }
}
