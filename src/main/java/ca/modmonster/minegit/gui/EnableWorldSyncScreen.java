package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.NetworkManager;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import net.minecraft.unmapped.C_01559903;
import net.minecraft.world.storage.WorldSaveInfo;

public class EnableWorldSyncScreen extends Screen {
    private final Screen parent;
    private final WorldSaveInfo level;
    private final Runnable closeCallback;

    private C_01559903 confirmButton;
    private C_01559903 cancelButton;
    private boolean showOpenSetupButton = false;

    public EnableWorldSyncScreen(Screen parent, WorldSaveInfo level, Runnable closeCallback) {
        super(new TranslatableText("minegit.sync.enable.title"));
        this.parent = parent;
        this.level = level;
        this.closeCallback = closeCallback;
    }

    @Override
    protected void init() {
        // Confirm button
        confirmButton = new C_01559903(width / 2 - 152, 124, 150, 20, I18n.translate("minegit.sync.enable.confirm.ok"), button -> setupSync());
        addButton(confirmButton);

        // Cancel button
        cancelButton = new C_01559903(width / 2 + 2, 124, 150, 20, I18n.translate("minegit.sync.enable.confirm.cancel"), button -> close());
        addButton(cancelButton);

        C_01559903 openSetupButton = new C_01559903(width / 2 - 75, 152, 150, 20, I18n.translate("minegit.link.setup.open"), button -> minecraft.openScreen(new AccountLinkScreen(this.parent, closeCallback)));
        openSetupButton.visible = showOpenSetupButton;
        addButton(openSetupButton);
    }

    @Override
    public void render(int i, int j, float f) {
        this.drawBackgroundTexture(i);
        super.render(i, j, f);
        drawCenteredString(this.textRenderer, this.f_89436361.getString(), this.width / 2, 50, 16777215);
        drawCenteredString(this.textRenderer, I18n.translate("minegit.sync.enable.confirm.line1", level.getName()), this.width / 2, 90, 16777215);
        drawCenteredString(this.textRenderer, I18n.translate("minegit.sync.enable.confirm.line2"), this.width / 2, 103, 16777215);
    }

    private void setupSync() {
        confirmButton.active = false;
        cancelButton.active = false;

        GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableText("minegit.sync.enable.working"));
        minecraft.openScreen(progressScreen);
        new Thread(() -> {
            // Create a repository on GitHub
            Config config = ConfigManager.getCurrentConfig();
            progressScreen.beginTask("Create GitHub repository", 0);
            NetworkManager.HttpResponse response = NetworkManager.createRepo(config.getPat(), level.getName(), level.getName());
            int statusCode = response == null? -1 : response.statusCode();
            if (statusCode != 201) {
                // OOPS! ERROR!!
                minecraft.execute(() -> {
                    minecraft.getToasts().add(new WideToast(I18n.translate("minegit.sync.enable.create_repo.error", statusCode)));
                    showOpenSetupButton = true;
                    cancelButton.active = true;

                    if (response != null) MineGIT.LOGGER.error(response.body());
                    minecraft.openScreen(this);
                });
                return;
            }

            String repoUrl = new JsonParser().parse(response.body()).getAsJsonObject().get("clone_url").getAsString();
            MineGIT.LOGGER.info("Successfully setup GitHub repo with URL: {}", repoUrl);

            // Git init on world save folder
            progressScreen.beginTask("Create Git repo", 0);
            boolean ok = GitManager.init(minecraft, level.getName(), repoUrl, progressScreen);
            if (!ok) {
                minecraft.execute(() -> {
                    minecraft.getToasts().add(new WideToast(I18n.translate("minegit.sync.enable.git_init.error")));
                    minecraft.openScreen(this);
                    cancelButton.active = true;
                });
                return;
            }

            minecraft.getToasts().add(new WideToast(I18n.translate("minegit.sync.enable.complete")));
            minecraft.execute(this::close);
        }).start();
    }

    @Override
    public void close() {
        minecraft.openScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }
}
