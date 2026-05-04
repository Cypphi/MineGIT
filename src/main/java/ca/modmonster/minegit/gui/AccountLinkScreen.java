package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.CryptoManager;
import ca.modmonster.minegit.data.NetworkManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;

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

    public AccountLinkScreen(Screen parent) {
        this(parent, null);
    }

    public AccountLinkScreen(Screen parent, Runnable closeCallback) {
        this.parent = parent;
        this.closeCallback = closeCallback;
    }

    @Override
    protected void init() {
        // Username text field
        usernameEdit = new TextFieldWidget(0, textRenderer, this.width / 2 - 100, 107, 200, 20);
        usernameEdit.setMaxLength(39);
        this.children.add(usernameEdit);

        // PAT text field
        patEdit = new TextFieldWidget(1, textRenderer, this.width / 2 - 100, 152, 200, 20);
        patEdit.setMaxLength(255);
        this.children.add(patEdit);

        // Test credentials button
        testCredentialsButton = new ButtonWidget(2, this.width / 2 - 100, 180, 200, 20, I18n.translate("minegit.link.test")) {
            @Override
            public void click(double mouseX, double mouseY) {
                testCredentials();
            }
        };
        addButton(testCredentialsButton);

        // Back button
        backButton = new ImageButton(3, 6, 6, ImageButton.ImageButtonTex.BACK) {
            @Override
            public void click(double mouseX, double mouseY) {
                close();
            }
        };
        addButton(backButton);

        // Ralsei go spinny
        ralspinWidget = new RalspinWidget(width - 60, height - 80);
        this.children.add(ralspinWidget);

        // Load configuration and update default values
        Config config = ConfigManager.getCurrentConfig();
        usernameEdit.setText(config.username);
        String pat = config.getPat();
        if (pat != null) patEdit.setText(pat);

        setFocused(usernameEdit);
        usernameEdit.setFocused(true);

        updateTestButtonStatus(false);
    }

    @Override
    public void render(int i, int j, float f) {
        this.drawBackgroundTexture(i);
        super.render(i, j, f);
        drawCenteredString(this.textRenderer, I18n.translate("minegit.link.title"), this.width / 2, 50, 16777215);
        drawCenteredString(this.textRenderer, I18n.translate("minegit.link.username"), this.width / 2, 90, -2130706433);
        drawCenteredString(this.textRenderer, I18n.translate("minegit.link.pat"), this.width / 2, 135, -2130706433);
        ralspinWidget.render(i, j, f);
        usernameEdit.render(i, j, f);
        patEdit.render(i, j, f);
        if (testCredentialsStatus != null) drawCenteredString(this.textRenderer, testCredentialsStatus, this.width / 2, 208, 16777215);
        if (backButton.isHovered()) renderTooltip(I18n.translate("minegit.link.back"),  i, j);
        if (ralspinWidget.isHovered()) renderTooltip(RalspinWidget.TOOLTIP, i, j);
    }

    @Override
    public boolean charTyped(char i, int j) {
        if (this.usernameEdit.charTyped(i, j) | this.patEdit.charTyped(i, j)) {
            updateTestButtonStatus(false);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (this.usernameEdit.keyPressed(i, j, k) | this.patEdit.keyPressed(i, j, k)) {
            updateTestButtonStatus(false);
            return true;
        } else if (i != 257 && i != 335) {
            return false;
        } else {
            close();
            return true;
        }
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
                    testCredentialsStatus = I18n.translate("minegit.link.status.success");
                    updateTestButtonStatus(true);
                    break;
                case 401:
                    testCredentialsStatus = I18n.translate("minegit.link.status.error.pat");
                    updateTestButtonStatus(true);
                    break;
                case 404:
                    testCredentialsStatus = I18n.translate("minegit.link.status.error.username");
                    updateTestButtonStatus(true);
                    break;
                default:
                    testCredentialsStatus = I18n.translate("minegit.link.status.error.generic", statusCode);
                    updateTestButtonStatus(true);
                    break;
            }
        }).start();
    }

    private void updateTestButtonStatus(boolean forceDisable) {
        testCredentialsButton.active = !forceDisable && !requestInProgress && !usernameEdit.getText().replace(" ", "").isEmpty() && !patEdit.getText().replace(" ", "").isEmpty();
    }

    @Override
    public void close() {
        // Save credentials
        String username = usernameEdit.getText();
        String pat = CryptoManager.encrypt(patEdit.getText());
        ConfigManager.save(new Config(username, pat));
        minecraft.openScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }
}
