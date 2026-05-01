package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.CryptoManager;
import ca.modmonster.minegit.data.NetworkManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

public class AccountLinkScreen extends Screen {
    private final Screen parent;
    private final Runnable closeCallback;
    private EditBox usernameEdit;
    private EditBox patEdit;
    private Button testCredentialsButton;
    private boolean requestInProgress = false;
    private String testCredentialsStatus = null;
    private Button backButton;
    private RalspinWidget ralspinWidget;

    public AccountLinkScreen(Screen parent) {
        this(parent, null);
    }

    public AccountLinkScreen(Screen parent, Runnable closeCallback) {
        super(new TranslatableComponent("minegit.link.title"));
        this.parent = parent;
        this.closeCallback = closeCallback;
    }

    @Override
    protected void init() {
        // Username text field
        usernameEdit = new EditBox(font, this.width / 2 - 100, 107, 200, 20, I18n.get("minegit.link.username"));
        usernameEdit.setMaxLength(39);
        usernameEdit.setResponder(string -> updateTestButtonStatus(false));
        this.children.add(usernameEdit);

        // PAT text field
        patEdit = new EditBox(font, this.width / 2 - 100, 152, 200, 20, I18n.get("minegit.link.pat"));
        patEdit.setMaxLength(255);
        patEdit.setResponder(string -> updateTestButtonStatus(false));
        this.children.add(patEdit);

        // Test credentials button
        testCredentialsButton = new Button(this.width / 2 - 100, 180, 200, 20, I18n.get("minegit.link.test"), button -> testCredentials());
        addButton(testCredentialsButton);

        // Back button
        backButton = new Button(6, 6, 20, 20, "←", button -> onClose());
        addButton(backButton);

        // Ralsei go spinny
        ralspinWidget = new RalspinWidget(width - 60, height - 80);
        this.children.add(ralspinWidget);

        updateTestButtonStatus(false);

        // Load configuration and update default values
        Config config = ConfigManager.getCurrentConfig();
        usernameEdit.setValue(config.username);
        String pat = config.getPat();
        if (pat != null) patEdit.setValue(pat);

        setInitialFocus(usernameEdit);
        usernameEdit.setFocus(true);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(i, j, f);
        drawCenteredString(this.font, this.title.getString(), this.width / 2, 50, 16777215);
        drawCenteredString(this.font, I18n.get("minegit.link.username"), this.width / 2, 90, -2130706433);
        drawCenteredString(this.font, I18n.get("minegit.link.pat"), this.width / 2, 135, -2130706433);
        ralspinWidget.render(i, j, f);
        usernameEdit.render(i, j, f);
        patEdit.render(i, j, f);
        if (testCredentialsStatus != null) drawCenteredString(this.font, testCredentialsStatus, this.width / 2, 208, 16777215);
        if (backButton.isHovered()) renderTooltip(I18n.get("minegit.link.back"),  i, j);
        if (ralspinWidget.isHovered()) renderTooltip(RalspinWidget.TOOLTIP, i, j);
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
            int statusCode = NetworkManager.testCredentials(usernameEdit.getValue(), patEdit.getValue());
            requestInProgress = false;
            updateTestButtonStatus(false);

            switch (statusCode) {
                case 200:
                    testCredentialsStatus = I18n.get("minegit.link.status.success");
                    updateTestButtonStatus(true);
                    break;
                case 401:
                    testCredentialsStatus = I18n.get("minegit.link.status.error.pat");
                    updateTestButtonStatus(true);
                    break;
                case 404:
                    testCredentialsStatus = I18n.get("minegit.link.status.error.username");
                    updateTestButtonStatus(true);
                    break;
                default:
                    testCredentialsStatus = I18n.get("minegit.link.status.error.generic", statusCode);
                    updateTestButtonStatus(true);
                    break;
            }
        }).start();
    }

    private void updateTestButtonStatus(boolean forceDisable) {
        testCredentialsButton.active = !forceDisable && !requestInProgress && !usernameEdit.getValue().replace(" ", "").isEmpty() && !patEdit.getValue().replace(" ", "").isEmpty();
    }

    @Override
    public void onClose() {
        // Save credentials
        String username = usernameEdit.getValue();
        String pat = CryptoManager.encrypt(patEdit.getValue());
        ConfigManager.save(new Config(username, pat));
        minecraft.setScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }
}
