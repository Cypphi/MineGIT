package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.CryptoManager;
import ca.modmonster.minegit.data.NetworkManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class AccountLinkScreen extends GuiScreen {
    private final GuiScreen parent;
    private final Runnable closeCallback;
    private GuiTextField usernameEdit;
    private GuiTextField patEdit;
    private GuiButton testCredentialsButton;
    private boolean requestInProgress = false;
    private String testCredentialsStatus = null;
    private GuiButton backButton;
    private RalspinWidget ralspinWidget;

    public AccountLinkScreen(GuiScreen parent) {
        this(parent, null);
    }

    public AccountLinkScreen(GuiScreen parent, Runnable closeCallback) {
        this.parent = parent;
        this.closeCallback = closeCallback;
    }

    @Override
    protected void initGui() {
        // Username text field
        usernameEdit = new GuiTextField(0, fontRenderer, this.width / 2 - 100, 107, 200, 20);
        usernameEdit.setMaxStringLength(39);
        this.children.add(usernameEdit);

        // PAT text field
        patEdit = new GuiTextField(1, fontRenderer, this.width / 2 - 100, 152, 200, 20);
        patEdit.setMaxStringLength(255);
        this.children.add(patEdit);

        // Test credentials button
        testCredentialsButton = new GuiButton(2, this.width / 2 - 100, 180, 200, 20, I18n.format("minegit.link.test")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                testCredentials();
            }
        };
        addButton(testCredentialsButton);

        // Back button
        backButton = new GuiButton(3, 6, 6, 20, 20, "←") {
            @Override
            public void onClick(double mouseX, double mouseY) {
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
        this.drawDefaultBackground();
        super.render(i, j, f);
        drawCenteredString(fontRenderer, I18n.format("minegit.link.title"), this.width / 2, 50, 16777215);
        drawCenteredString(fontRenderer, I18n.format("minegit.link.username"), this.width / 2, 90, -2130706433);
        drawCenteredString(fontRenderer, I18n.format("minegit.link.pat"), this.width / 2, 135, -2130706433);
        ralspinWidget.render(i, j);
        usernameEdit.drawTextField(i, j, f);
        patEdit.drawTextField(i, j, f);
        if (testCredentialsStatus != null) drawCenteredString(fontRenderer, testCredentialsStatus, this.width / 2, 208, 16777215);
        if (backButton.isMouseOver()) drawHoveringText(I18n.format("minegit.link.back"),  i, j);
        if (ralspinWidget.isMouseOver()) drawHoveringText(RalspinWidget.TOOLTIP, i, j);
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
        } else if (i != 257 && i != 335) { // TODO: wtf is thissssssss
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
                    testCredentialsStatus = I18n.format("minegit.link.status.success");
                    updateTestButtonStatus(true);
                    break;
                case 401:
                    testCredentialsStatus = I18n.format("minegit.link.status.error.pat");
                    updateTestButtonStatus(true);
                    break;
                case 404:
                    testCredentialsStatus = I18n.format("minegit.link.status.error.username");
                    updateTestButtonStatus(true);
                    break;
                default:
                    testCredentialsStatus = I18n.format("minegit.link.status.error.generic", statusCode);
                    updateTestButtonStatus(true);
                    break;
            }
        }).start();
    }

    private void updateTestButtonStatus(boolean forceDisable) {
        testCredentialsButton.enabled = !forceDisable && !requestInProgress && !usernameEdit.getText().replace(" ", "").isEmpty() && !patEdit.getText().replace(" ", "").isEmpty();
    }

    @Override
    public void close() {
        // Save credentials
        String username = usernameEdit.getText();
        String pat = CryptoManager.encrypt(patEdit.getText());
        ConfigManager.save(new Config(username, pat));
        mc.displayGuiScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }
}
