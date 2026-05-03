package ca.modmonster.minegit.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.CryptoManager;
import ca.modmonster.minegit.data.NetworkManager;

public class AccountLinkScreen extends GuiScreen {
    private final GuiScreen parent;
    private final Runnable closeCallback;
    private GuiTextField usernameEdit;
    private GuiTextField patEdit;
    private GuiButton testCredentialsButton;
    private boolean requestInProgress = false;
    private String testCredentialsStatus = null;
    private ImageButton backButton;
    private RalspinWidget ralspinWidget;

    public AccountLinkScreen(GuiScreen parent) {
        this(parent, null);
    }

    public AccountLinkScreen(GuiScreen parent, Runnable closeCallback) {
        this.parent = parent;
        this.closeCallback = closeCallback;
    }

    @Override
    public void initGui() {
        // Username text field
        usernameEdit = new GuiTextField(0, fontRenderer, this.width / 2 - 100, 107, 200, 20);
        usernameEdit.setMaxStringLength(39);

        // PAT text field
        patEdit = new GuiTextField(1, fontRenderer, this.width / 2 - 100, 152, 200, 20);
        patEdit.setMaxStringLength(255);

        // Test credentials button
        testCredentialsButton = new GuiButton(2, this.width / 2 - 100, 180, 200, 20, I18n.format("minegit.link.test"));
        addButton(testCredentialsButton);

        // Back button
        backButton = new ImageButton(3, 6, 6, ImageButton.ImageButtonTex.BACK);
        addButton(backButton);

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
    protected void actionPerformed(GuiButton button) {
        if (button.id == 2) {
            testCredentials();
        } else if (button.id == 3) {
            close();
        }
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        this.drawDefaultBackground();
        super.drawScreen(i, j, f);
        drawCenteredString(fontRenderer, I18n.format("minegit.link.title"), this.width / 2, 50, 16777215);
        drawCenteredString(fontRenderer, I18n.format("minegit.link.username"), this.width / 2, 90, -2130706433);
        drawCenteredString(fontRenderer, I18n.format("minegit.link.pat"), this.width / 2, 135, -2130706433);
        ralspinWidget.render(i, j);
        usernameEdit.drawTextBox();
        patEdit.drawTextBox();
        if (testCredentialsStatus != null) drawCenteredString(fontRenderer, testCredentialsStatus, this.width / 2, 208, 16777215);
        if (backButton.isMouseOver()) drawHoveringText(I18n.format("minegit.link.back"),  i, j);
        if (ralspinWidget.isMouseOver()) drawHoveringText(RalspinWidget.TOOLTIP, i, j);
    }

    @Override
    protected void keyTyped(char i, int j) {
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
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.usernameEdit.mouseClicked(mouseX, mouseY, mouseButton);
        this.patEdit.mouseClicked(mouseX, mouseY, mouseButton);
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

    public void close() {
        // Save credentials
        String username = usernameEdit.getText();
        String pat = CryptoManager.encrypt(patEdit.getText());
        ConfigManager.save(new Config(username, pat));
        mc.displayGuiScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }
}
