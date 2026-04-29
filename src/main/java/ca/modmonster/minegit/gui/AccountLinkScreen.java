package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.CryptoManager;
import ca.modmonster.minegit.data.NetworkManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class AccountLinkScreen extends Screen {
    private static final Component USERNAME_EDIT_LABEL = new TranslatableComponent("minegit.link.username");
    private static final Component PAT_EDIT_LABEL = new TranslatableComponent("minegit.link.pat");
    private static final Component BACK_BUTTON_TOOLTIP = new TranslatableComponent("minegit.link.back");

    private final Screen parent;
    private final Runnable closeCallback;
    private EditBox usernameEdit;
    private EditBox patEdit;
    private Button testCredentialsButton;
    private boolean requestInProgress = false;
    private Component testCredentialsStatus = null;
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
        usernameEdit = new EditBox(font, this.width / 2 - 100, 107, 200, 20, USERNAME_EDIT_LABEL);
        usernameEdit.setMaxLength(39);
        usernameEdit.setResponder(string -> updateTestButtonStatus(false));
        addRenderableWidget(usernameEdit);

        // PAT text field
        patEdit = new EditBox(font, this.width / 2 - 100, 152, 200, 20, PAT_EDIT_LABEL);
        patEdit.setMaxLength(255);
        patEdit.setResponder(string -> updateTestButtonStatus(false));
        addRenderableWidget(patEdit);

        // Test credentials button
        testCredentialsButton = new Button(this.width / 2 - 100, 180, 200, 20, new TranslatableComponent("minegit.link.test"), button -> testCredentials());
        addRenderableWidget(testCredentialsButton);

        // Back button
        backButton = new Button(6, 6, 20, 20, new TextComponent("←"), button -> onClose());
        addRenderableWidget(backButton);

        // Ralsei go spinny
        ralspinWidget = new RalspinWidget(width - 60, height - 80);
        addRenderableWidget(ralspinWidget);

        updateTestButtonStatus(false);

        // Load configuration and update default values
        Config config = ConfigManager.getCurrentConfig();
        usernameEdit.setValue(config.username);
        String pat = config.getPat();
        if (pat != null) patEdit.setValue(pat);

        setInitialFocus(usernameEdit);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 16777215);
        drawCenteredString(poseStack, this.font, USERNAME_EDIT_LABEL, this.width / 2, 90, -2130706433);
        drawCenteredString(poseStack, this.font, PAT_EDIT_LABEL, this.width / 2, 135, -2130706433);
        if (testCredentialsStatus != null) drawCenteredString(poseStack, this.font, testCredentialsStatus, this.width / 2, 208, 16777215);
        if (backButton.isHoveredOrFocused()) renderTooltip(poseStack, BACK_BUTTON_TOOLTIP,  i, j);
        if (ralspinWidget.isHoveredOrFocused()) renderTooltip(poseStack, RalspinWidget.TOOLTIP, i, j);
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
                    testCredentialsStatus = new TranslatableComponent("minegit.link.status.success");
                    updateTestButtonStatus(true);
                    break;
                case 401:
                    testCredentialsStatus = new TranslatableComponent("minegit.link.status.error.pat");
                    updateTestButtonStatus(true);
                    break;
                case 404:
                    testCredentialsStatus = new TranslatableComponent("minegit.link.status.error.username");
                    updateTestButtonStatus(true);
                    break;
                default:
                    testCredentialsStatus = new TranslatableComponent("minegit.link.status.error.generic", statusCode);
                    updateTestButtonStatus(true);
                    break;
            }
        }).start();
    }

    private void updateTestButtonStatus(boolean forceDisable) {
        testCredentialsButton.active = !forceDisable && !requestInProgress && !usernameEdit.getValue().isBlank() && !patEdit.getValue().isBlank();
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
