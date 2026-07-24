package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.backport.ScreenTooltipRenderer;
import ca.modmonster.minegit.backport.ScreenUtil;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.CryptoManager;
import ca.modmonster.minegit.data.NetworkManager;
import io.github.prospector.modmenu.gui.TextFieldWidget;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;

public class AccountLinkScreen extends Screen {
    private final Screen parent;
    private final Runnable closeCallback;
    private TextFieldWidget usernameEdit;
    private TextFieldWidget patEdit;
    private ButtonElement testCredentialsButton;
    private boolean requestInProgress = false;
    private String testCredentialsStatus = null;
    private ButtonElement backButton;
    private RalspinWidget ralspinWidget;
    private ButtonElement clearButton;

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
        usernameEdit = new TextFieldWidget(null, this.width / 2 - 100, 107, 200, 20);
        usernameEdit.setMaxStringLength(39);

        // PAT text field
        patEdit = new TextFieldWidget(null, this.width / 2 - 100, 152, 200, 20);
        patEdit.setMaxStringLength(255);

        // Clear credentials button
        clearButton = new ButtonElement(1, this.width / 2 - 100, 180, 60, 20, "Clear");
        buttons.add(clearButton);

        // Test credentials button
        testCredentialsButton = new ButtonElement(2, this.width / 2 - 34, 180, 136, 20, "Test Credentials");
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
    protected void buttonClicked(ButtonElement button) {
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
        this.renderTexturedBackground();
        drawStringCenteredShadow(this.fontRenderer, "MineGit Cloud Sync Setup", this.width / 2, 50, 16777215);
        drawStringCenteredShadow(this.fontRenderer, "GitHub Username", this.width / 2, 90, -2130706433);
        drawStringCenteredShadow(this.fontRenderer, "GitHub Access Token", this.width / 2, 135, -2130706433);
        ralspinWidget.render(mc, i, j);
        usernameEdit.drawTextBox();
        patEdit.drawTextBox();
        super.render(i, j, f);
        if (testCredentialsStatus != null) drawStringCenteredShadow(this.fontRenderer, testCredentialsStatus, this.width / 2, 208, 16777215);
        if (ScreenUtil.isHovered(i, j, backButton.xPosition, backButton.yPosition, 20, 20)) ((ScreenTooltipRenderer) this).renderTooltip("Save and Exit",  i, j);
        if (ralspinWidget.isHovered()) ((ScreenTooltipRenderer) this).renderTooltip(RalspinWidget.TOOLTIP, i, j);
    }

    @Override
    public void keyPressed(char i, int j, int k, int l) {
        if (this.usernameEdit.isFocused()) {
            this.usernameEdit.textboxKeyTyped(i, j);
            updateTestButtonStatus(false);
        } else if (this.patEdit.isFocused()) {
            this.patEdit.textboxKeyTyped(i, j);
            updateTestButtonStatus(false);
        }
        if (j == 1) close();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.usernameEdit.mouseClicked(mouseX, mouseY, mouseButton);
        this.patEdit.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void tick() {
        usernameEdit.updateCursorCounter();
        patEdit.updateCursorCounter();
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
        testCredentialsButton.enabled = !forceDisable && !requestInProgress && !usernameEdit.getText().replace(" ", "").isEmpty() && !patEdit.getText().replace(" ", "").isEmpty();
        clearButton.enabled = !this.usernameEdit.getText().isEmpty() || !this.patEdit.getText().isEmpty();
    }

    public void close() {
        // Save credentials
        String username = usernameEdit.getText();
        String pat = CryptoManager.encrypt(patEdit.getText());
        ConfigManager.save(new Config(username, pat));
        mc.displayScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }
}
