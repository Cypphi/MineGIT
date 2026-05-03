package ca.modmonster.minegit.gui;

import com.google.gson.JsonParser;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.storage.WorldSummary;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.NetworkManager;

public class EnableWorldSyncScreen extends GuiScreen {
    private final GuiScreen parent;
    private final WorldSummary level;
    private final Runnable closeCallback;

    private GuiButton confirmButton;
    private GuiButton cancelButton;
    private boolean showOpenSetupButton = false;

    public EnableWorldSyncScreen(GuiScreen parent, WorldSummary level, Runnable closeCallback) {
        this.parent = parent;
        this.level = level;
        this.closeCallback = closeCallback;
    }

    @Override
    public void initGui() {
        // Confirm button
        confirmButton = new GuiButton(0, width / 2 - 152, 124, 150, 20, I18n.format("minegit.sync.enable.confirm.ok"));
        addButton(confirmButton);

        // Cancel button
        cancelButton = new GuiButton(1, width / 2 + 2, 124, 150, 20, I18n.format("minegit.sync.enable.confirm.cancel"));
        addButton(cancelButton);

        GuiButton openSetupButton = new GuiButton(2, width / 2 - 75, 152, 150, 20, I18n.format("minegit.link.setup.open"));
        openSetupButton.visible = showOpenSetupButton;
        addButton(openSetupButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            setupSync();
        } else if (button.id == 1) {
            close();
        } else if (button.id == 2) {
            mc.displayGuiScreen(new AccountLinkScreen(EnableWorldSyncScreen.this.parent, closeCallback));
        }
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        this.drawDefaultBackground();
        super.drawScreen(i, j, f);
        drawCenteredString(fontRenderer, I18n.format("minegit.sync.enable.title"), this.width / 2, 50, 16777215);
        drawCenteredString(fontRenderer, I18n.format("minegit.sync.enable.confirm.line1", level.getDisplayName()), this.width / 2, 90, 16777215);
        drawCenteredString(fontRenderer, I18n.format("minegit.sync.enable.confirm.line2"), this.width / 2, 103, 16777215);
    }

    private void setupSync() {
        confirmButton.enabled = false;
        cancelButton.enabled = false;

        GitProgressScreen progressScreen = new GitProgressScreen(I18n.format("minegit.sync.enable.working"));
        mc.displayGuiScreen(progressScreen);
        new Thread(() -> {
            // Create a repository on GitHub
            Config config = ConfigManager.getCurrentConfig();
            progressScreen.beginTask("Create GitHub repository", 0);
            NetworkManager.HttpResponse response = NetworkManager.createRepo(config.getPat(), level.getFileName(), level.getDisplayName());
            int statusCode = response == null? -1 : response.statusCode();
            if (statusCode != 201) {
                // OOPS! ERROR!!
                mc.addScheduledTask(() -> {
                    mc.getToastGui().add(new WideToast(I18n.format("minegit.sync.enable.create_repo.error", statusCode)));
                    showOpenSetupButton = true;
                    cancelButton.enabled = true;

                    if (response != null) MineGIT.LOGGER.error(response.body());
                    mc.displayGuiScreen(this);
                });
                return;
            }

            String repoUrl = new JsonParser().parse(response.body()).getAsJsonObject().get("clone_url").getAsString();
            MineGIT.LOGGER.info("Successfully setup GitHub repo with URL: {}", repoUrl);

            // Git init on world save folder
            progressScreen.beginTask("Create Git repo", 0);
            boolean ok = GitManager.init(mc, level.getFileName(), repoUrl, progressScreen);
            if (!ok) {
                mc.addScheduledTask(() -> {
                    mc.getToastGui().add(new WideToast(I18n.format("minegit.sync.enable.git_init.error")));
                    mc.displayGuiScreen(this);
                    cancelButton.enabled = true;
                });
                return;
            }

            mc.getToastGui().add(new WideToast(I18n.format("minegit.sync.enable.complete")));
            mc.addScheduledTask(this::close);
        }).start();
    }

    public void close() {
        mc.displayGuiScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }

    @Override
    protected void keyTyped(char i, int j) {
        if (j == 1) close();
    }
}
