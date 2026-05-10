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
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.core.world.save.SaveFile;

public class EnableWorldSyncScreen extends Screen {
    private final Screen parent;
    private final SaveFile level;
    private final Runnable closeCallback;

    private ButtonElement confirmButton;
    private ButtonElement cancelButton;
    private boolean showOpenSetupButton = false;

    public EnableWorldSyncScreen(Screen parent, SaveFile level, Runnable closeCallback) {
        this.parent = parent;
        this.level = level;
        this.closeCallback = closeCallback;
    }

    @Override
    public void init() {
        // Confirm button
        confirmButton = new ButtonElement(0, width / 2 - 152, 124, 150, 20, "OK!");
        buttons.add(confirmButton);

        // Cancel button
        cancelButton = new ButtonElement(1, width / 2 + 2, 124, 150, 20, "Cancel");
        buttons.add(cancelButton);

        ButtonElement openSetupButton = new ButtonElement(2, width / 2 - 75, 152, 150, 20, "Open Cloud Sync Setup");
        openSetupButton.visible = showOpenSetupButton;
        buttons.add(openSetupButton);
    }

    @Override
    protected void buttonClicked(ButtonElement button) {
        if (button.id == 0) {
            setupSync();
        } else if (button.id == 1) {
            close();
        } else if (button.id == 2) {
            mc.displayScreen(new AccountLinkScreen(EnableWorldSyncScreen.this.parent, closeCallback));
        }
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderTexturedBackground();
        drawStringCentered(font, "Enable Cloud Sync", this.width / 2, 50, 16777215);
        drawStringCentered(font, String.format("Would you like to enable cloud sync for %s?", level.getDisplayName()), this.width / 2, 90, 16777215);
        drawStringCentered(font, "A new GitHub repository will be created for this world.", this.width / 2, 103, 16777215);
        super.render(i, j, f);
    }

    private void setupSync() {
        confirmButton.enabled = false;
        cancelButton.enabled = false;

        GitProgressScreen progressScreen = new GitProgressScreen("Enabling cloud sync...");
        mc.displayScreen(progressScreen);
        new Thread(() -> {
            // Create a repository on GitHub
            Config config = ConfigManager.getCurrentConfig();
            progressScreen.beginTask("Create GitHub repository", 0);
            NetworkManager.HttpResponse response = NetworkManager.createRepo(config.getPat(), level.getFileName(), level.getDisplayName());
            int statusCode = response == null? -1 : response.statusCode();
            if (statusCode != 201) {
                // OOPS! ERROR!!
                MainThreadTasks.execute(() -> {
                    ToastManager.INSTANCE.add(new Toast(String.format("Something went wrong while creating the GitHub repo! (Error %d)", statusCode)));
                    showOpenSetupButton = true;
                    cancelButton.enabled = true;

                    if (response != null) MineGIT.LOGGER.error(response.body());
                    mc.displayScreen(this);
                });
                return;
            }

            @SuppressWarnings("deprecation")
            String repoUrl = new JsonParser().parse(response.body()).getAsJsonObject().get("clone_url").getAsString();
            MineGIT.LOGGER.info("Successfully setup GitHub repo with URL: {}", repoUrl);

            // Git init on world save folder
            progressScreen.beginTask("Create Git repo", 0);
            boolean ok = GitManager.init(level.getFileName(), repoUrl, progressScreen);
            if (!ok) {
                MainThreadTasks.execute(() -> {
                    ToastManager.INSTANCE.add(new Toast("Something went wrong while setting up the Git repo!"));
                    mc.displayScreen(this);
                    cancelButton.enabled = true;
                });
                return;
            }

            ToastManager.INSTANCE.add(new Toast("Successfully enabled cloud sync!"));
            MainThreadTasks.execute(this::close);
        }).start();
    }

    public void close() {
        mc.displayScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }

    @Override
    public void keyPressed(char i, int j, int k, int l) {
        if (j == 1) close();
    }
}
