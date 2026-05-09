package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.backport.MainThreadTasks;
import ca.modmonster.minegit.backport.toast.Toast;
import ca.modmonster.minegit.backport.toast.ToastManager;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.NetworkManager;
import com.google.gson.JsonParser;
import net.minecraft.class_591;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class EnableWorldSyncScreen extends Screen {
    private final Screen parent;
    private final class_591 level;
    private final Runnable closeCallback;

    private ButtonWidget confirmButton;
    private ButtonWidget cancelButton;
    private boolean showOpenSetupButton = false;

    public EnableWorldSyncScreen(Screen parent, class_591 level, Runnable closeCallback) {
        this.parent = parent;
        this.level = level;
        this.closeCallback = closeCallback;
    }

    @Override
    public void init() {
        // Confirm button
        confirmButton = new ButtonWidget(0, width / 2 - 152, 124, 150, 20, "OK!");
        buttons.add(confirmButton);

        // Cancel button
        cancelButton = new ButtonWidget(1, width / 2 + 2, 124, 150, 20, "Cancel");
        buttons.add(cancelButton);

        ButtonWidget openSetupButton = new ButtonWidget(2, width / 2 - 75, 152, 150, 20, "Open Cloud Sync Setup");
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
            minecraft.setScreen(new AccountLinkScreen(EnableWorldSyncScreen.this.parent, closeCallback));
        }
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackgroundTexture(i);
        drawCenteredTextWithShadow(this.textRenderer, "Enable Cloud Sync", this.width / 2, 50, 16777215);
        drawCenteredTextWithShadow(this.textRenderer, String.format("Would you like to enable cloud sync for %s?", level.method_1958()), this.width / 2, 90, 16777215);
        drawCenteredTextWithShadow(this.textRenderer, "A new GitHub repository will be created for this world.", this.width / 2, 103, 16777215);
        super.render(i, j, f);
    }

    private void setupSync() {
        confirmButton.active = false;
        cancelButton.active = false;

        GitProgressScreen progressScreen = new GitProgressScreen("Enabling cloud sync...");
        minecraft.setScreen(progressScreen);
        new Thread(() -> {
            // Create a repository on GitHub
            Config config = ConfigManager.getCurrentConfig();
            progressScreen.beginTask("Create GitHub repository", 0);
            NetworkManager.HttpResponse response = NetworkManager.createRepo(config.getPat(), level.method_1956(), level.method_1958());
            int statusCode = response == null? -1 : response.statusCode();
            if (statusCode != 201) {
                // OOPS! ERROR!!
                MainThreadTasks.execute(() -> {
                    ToastManager.INSTANCE.add(new Toast(String.format("Something went wrong while creating the GitHub repo! (Error %d)", statusCode)));
                    showOpenSetupButton = true;
                    cancelButton.active = true;

                    if (response != null) MineGIT.LOGGER.error(response.body());
                    minecraft.setScreen(this);
                });
                return;
            }

            @SuppressWarnings("deprecation")
            String repoUrl = new JsonParser().parse(response.body()).getAsJsonObject().get("clone_url").getAsString();
            MineGIT.LOGGER.info("Successfully setup GitHub repo with URL: {}", repoUrl);

            // Git init on world save folder
            progressScreen.beginTask("Create Git repo", 0);
            boolean ok = GitManager.init(level.method_1956(), repoUrl, progressScreen);
            if (!ok) {
                MainThreadTasks.execute(() -> {
                    ToastManager.INSTANCE.add(new Toast("Something went wrong while setting up the Git repo!"));
                    minecraft.setScreen(this);
                    cancelButton.active = true;
                });
                return;
            }

            ToastManager.INSTANCE.add(new Toast("Successfully enabled cloud sync!"));
            MainThreadTasks.execute(this::close);
        }).start();
    }

    public void close() {
        minecraft.setScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }

    @Override
    protected void keyPressed(char i, int j) {
        if (j == 1) close();
    }
}
