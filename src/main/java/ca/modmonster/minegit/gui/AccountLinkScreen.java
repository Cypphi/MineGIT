package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.backport.ScreenTooltipRenderer;
import ca.modmonster.minegit.backport.ScreenUtil;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.CryptoManager;
import ca.modmonster.minegit.data.NetworkManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class AccountLinkScreen extends Screen {
    private final Screen parent;
    private final Runnable closeCallback;
    private TextFieldWidget usernameEdit;
    private TextFieldWidget patEdit;
    private ButtonWidget testCredentialsButton;
    private boolean requestInProgress = false;
    private String testCredentialsStatus = null;
    private ButtonWidget backButton;
    private RalspinWidget ralspinWidget;
    private ButtonWidget clearButton;

    public AccountLinkScreen(Screen parent) {
        this(parent, null);
    }

    public AccountLinkScreen(Screen parent, Runnable closeCallback) {
        this.parent = parent;
        this.closeCallback = closeCallback;
    }

    @Override
    public void init() {
        // Username text field
        usernameEdit = new TextFieldWidget(this, textRenderer, this.width / 2 - 100, 107, 200, 20, "");
        usernameEdit.setMaxLength(39);

        // PAT text field
        patEdit = new TextFieldWidget(this, textRenderer, this.width / 2 - 100, 152, 200, 20, "");
        patEdit.setMaxLength(255);

        // Clear credentials button
        clearButton = new ButtonWidget(1, this.width / 2 - 100, 180, 60, 20, "Clear");
        buttons.add(clearButton);

        // Test credentials button
        testCredentialsButton = new ButtonWidget(2, this.width / 2 - 34, 180, 136, 20, "Test Credentials");
        buttons.add(testCredentialsButton);

        // Back button
        backButton = new ImageButton(3, 6, 6, ImageButton.ImageButtonTex.BACK);
        buttons.add(backButton);

        // Ralsei go spinny
        ralspinWidget = new RalspinWidget(width - 60, height - 80);

        // Load configuration and update default values
        Config config = ConfigManager.getCurrentConfig();
        usernameEdit.setText(config.username);
        String pat = config.getPat();
        if (pat != null) patEdit.setText(pat);

        usernameEdit.setFocused(true);

        updateTestButtonStatus(false);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 1) {
          usernameEdit.setText("");
          patEdit.setText("");
          updateTestButtonStatus(false);
        } if (button.id == 2) {
            testCredentials();
        } else if (button.id == 3) {
            close();
        }
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackgroundTexture(i);
        drawCenteredTextWithShadow(this.textRenderer, "MineGit Cloud Sync Setup", this.width / 2, 50, 16777215);
        drawCenteredTextWithShadow(this.textRenderer, "GitHub Username", this.width / 2, 90, -2130706433);
        drawCenteredTextWithShadow(this.textRenderer, "GitHub Access Token", this.width / 2, 135, -2130706433);
        ralspinWidget.render(minecraft, i, j);
        usernameEdit.render();
        patEdit.render();
        super.render(i, j, f);
        if (testCredentialsStatus != null) drawCenteredTextWithShadow(this.textRenderer, testCredentialsStatus, this.width / 2, 208, 16777215);
        if (ScreenUtil.isHovered(i, j, backButton.x, backButton.y, 20, 20)) ((ScreenTooltipRenderer) this).renderTooltip("Save and Exit",  i, j);
        if (ralspinWidget.isHovered()) ((ScreenTooltipRenderer) this).renderTooltip(RalspinWidget.TOOLTIP, i, j);
    }

    @Override
    protected void keyPressed(char i, int j) {
        if (this.usernameEdit.focused) {
            this.usernameEdit.keyPressed(i, j);
            updateTestButtonStatus(false);
        } else if (this.patEdit.focused) {
            this.patEdit.keyPressed(i, j);
            updateTestButtonStatus(false);
        }
        if (j == 1) close();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.usernameEdit.mouseClicked(mouseX, mouseY, mouseButton);
        this.patEdit.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void tick() {
        usernameEdit.tick();
        patEdit.tick();
    }

    private void testCredentials() {
        requestInProgress = true;
        updateTestButtonStatus(false);

        new Thread(() -> {
            int statusCode = NetworkManager.testCredentials(usernameEdit.getText(), patEdit.getText());
            requestInProgress = false;
            updateTestButtonStatus(false);

            switch (statusCode) {
                case 200:
                    testCredentialsStatus = "Success!";
                    updateTestButtonStatus(true);
                    break;
                case 401:
                    testCredentialsStatus = "Invalid access token.";
                    updateTestButtonStatus(true);
                    break;
                case 404:
                    testCredentialsStatus = "Invalid username.";
                    updateTestButtonStatus(true);
                    break;
                default:
                    testCredentialsStatus = String.format("Something went wrong when trying to test your credentials! (Error %d)", statusCode);
                    updateTestButtonStatus(true);
                    break;
            }
        }).start();
    }

    private void updateTestButtonStatus(boolean forceDisable) {
        testCredentialsButton.active = !forceDisable && !requestInProgress && !usernameEdit.getText().replace(" ", "").isEmpty() && !patEdit.getText().replace(" ", "").isEmpty();
        clearButton.active = !this.usernameEdit.getText().isEmpty() || !this.patEdit.getText().isEmpty();
    }

    public void close() {
        // Save credentials
        String username = usernameEdit.getText();
        String pat = CryptoManager.encrypt(patEdit.getText());
        ConfigManager.save(new Config(username, pat));
        minecraft.setScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }
}
