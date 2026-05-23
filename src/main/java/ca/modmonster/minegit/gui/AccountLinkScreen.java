package ca.modmonster.minegit.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.data.*;

public class AccountLinkScreen extends Screen {
    private static final Component USERNAME_EDIT_LABEL = Component.translatable("minegit.link.username");
    private static final Component USERNAME_EDIT_LABEL_ORG = Component.translatable("minegit.link.username.org");
    private static final Component PAT_EDIT_LABEL = Component.translatable("minegit.link.pat");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 20 + 4, 36);

    private final Screen parent;
    private final Runnable closeCallback;
    private EditBox usernameEdit;
    private EditBox patEdit;
    private Button testCredentialsButton;
    private RalspinWidget ralspinWidget;
    private boolean requestInProgress = false;
    private StringWidget testCredentialsStatus;
    private StringWidget selectedServiceLabel;
    private StringWidget usernameEditLabel;

    private GitService selectedService = GitService.GITHUB;

    public AccountLinkScreen(Screen parent) {
        this(parent, null);
    }

    public AccountLinkScreen(Screen parent, Runnable closeCallback) {
        super(Component.translatable("minegit.link.title"));
        this.parent = parent;
        this.closeCallback = closeCallback;
    }

    @Override
    protected void init() {
        // Column layout
        GridLayout columnLayout = this.layout.addToContents(new GridLayout().spacing(8));
        columnLayout.defaultCellSetting().alignHorizontallyCenter();

        // Menu title
        layout.addToHeader(new StringWidget(this.title, this.font));

        GridLayout serviceRow = columnLayout.addChild(new GridLayout().spacing(4), 0, 0);

        selectedServiceLabel = new StringWidget(126, 22, Component.empty(), font);
        selectedServiceLabel.alignLeft();
        serviceRow.addChild(selectedServiceLabel, 0, 0);

        SelectServiceScreen selectServiceScreen = new SelectServiceScreen(this, () -> {
            minecraft.setScreen(this);
            updateService();
            updateTestButtonStatus(false);
        });
        Button changeServiceButton = Button.builder(Component.translatable("minegit.link.select_service"), (button) ->
                minecraft.setScreen(selectServiceScreen)).size(70, 20).build();
        serviceRow.addChild(changeServiceButton, 0, 1);

        // Username text field
        usernameEditLabel = columnLayout.addChild(new StringWidget(USERNAME_EDIT_LABEL, font), 1, 0);
        usernameEditLabel.setAlpha(0.5f);
        usernameEdit = new EditBox(font, 0, 0, 200, 20, USERNAME_EDIT_LABEL);
        usernameEdit.setMaxLength(39);
        usernameEdit.setResponder(string -> updateTestButtonStatus(false));
        columnLayout.addChild(usernameEdit, 2, 0);

        // PAT text field
        StringWidget patEditLabel = columnLayout.addChild(new StringWidget(PAT_EDIT_LABEL, font), 3, 0);
        patEditLabel.setAlpha(0.5f);
        patEdit = new EditBox(font, 0, 0, 200, 20, PAT_EDIT_LABEL);
        patEdit.setMaxLength(255);
        patEdit.setResponder(string -> updateTestButtonStatus(false));
        columnLayout.addChild(patEdit, 4, 0);

        // Test credentials button
        testCredentialsButton = Button.builder(Component.translatable("minegit.link.test"), button -> testCredentials()).size(200, 20).build();
        columnLayout.addChild(testCredentialsButton, 5, 0);

        // Test credentials status
        testCredentialsStatus = new StringWidget(Component.empty(), font);
        columnLayout.addChild(testCredentialsStatus, 6, 0);

        updateService();
        updateTestButtonStatus(false);

        // Add layout widgets
        columnLayout.arrangeElements();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.layout.arrangeElements();

        // Back button
        Button backButton = Button.builder(Component.literal("←"), button -> onClose())
            .tooltip(Tooltip.create(Component.translatable("minegit.link.back")))
            .bounds(6, 6, 20, 20)
            .build();
        addRenderableWidget(backButton);

        // Ralsei go spinny
        ralspinWidget = new RalspinWidget(width - 60, height - 80);
        addRenderableWidget(ralspinWidget);

        // Load configuration and update default values
        Config config = ConfigManager.getCurrentConfig();
        usernameEdit.setValue(config.username);
        String pat = config.getPat();
        if (pat != null) patEdit.setValue(pat);

        setInitialFocus(usernameEdit);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderDirtBackground(guiGraphics);
        super.render(guiGraphics, i, j, f);
    }

    private void updateService() {
        selectedService = ConfigManager.getCurrentConfig().gitService;
        selectedServiceLabel.setMessage(Component.literal(selectedService.getDisplayName()));

        if (selectedService == GitService.GITHUB_ORG) {
            usernameEditLabel.setMessage(USERNAME_EDIT_LABEL_ORG);
            usernameEdit.setMessage(USERNAME_EDIT_LABEL_ORG);
        } else {
            usernameEditLabel.setMessage(USERNAME_EDIT_LABEL);
            usernameEdit.setMessage(USERNAME_EDIT_LABEL);
        }
        layout.arrangeElements();
    }

    private void testCredentials() {
        requestInProgress = true;
        updateTestButtonStatus(false);

        // Build config from current UI values
        Config currentConfig = ConfigManager.getCurrentConfig();
        Config testConfig = new Config(usernameEdit.getValue(), CryptoManager.encrypt(patEdit.getValue()), selectedService, currentConfig.customWebUrl, currentConfig.customApiUrl);

        new Thread(() -> {
            int statusCode = NetworkManager.testCredentials(testConfig);
            requestInProgress = false;

            minecraft.submit(() -> {
                updateTestButtonStatus(false);

                switch (statusCode) {
                    case 200:
                        updateTestCredentialsStatus(Component.translatable("minegit.link.status.success"));
                        updateTestButtonStatus(true);
                        break;
                    case 401:
                        updateTestCredentialsStatus(Component.translatable("minegit.link.status.error.pat"));
                        updateTestButtonStatus(true);
                        break;
                    case 404:
                        updateTestCredentialsStatus(Component.translatable("minegit.link.status.error.username"));
                        updateTestButtonStatus(true);
                        break;
                    case -2:
                        updateTestCredentialsStatus(Component.translatable("minegit.link.status.error.custom_url_required"));
                        updateTestButtonStatus(true);
                        break;
                    default:
                        updateTestCredentialsStatus(Component.translatable("minegit.link.status.error.generic", statusCode));
                        updateTestButtonStatus(true);
                        break;
                }
            });
        }).start();
    }

    private void updateTestButtonStatus(boolean forceDisable) {
        testCredentialsButton.active = !forceDisable && !requestInProgress && !usernameEdit.getValue().isBlank() && !patEdit.getValue().isBlank();
    }

    private void updateTestCredentialsStatus(Component message) {
        testCredentialsStatus.setMessage(message);
        testCredentialsStatus.setWidth(font.width(message));
        layout.arrangeElements();
    }

    @Override
    public void onClose() {
        // Save credentials with service configuration
        String username = usernameEdit.getValue();
        String pat = CryptoManager.encrypt(patEdit.getValue());
        Config currentConfig = ConfigManager.getCurrentConfig();
        Config config = new Config(username, pat, currentConfig.gitService, currentConfig.customWebUrl, currentConfig.customApiUrl);
        ConfigManager.save(config);

        minecraft.setScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        ralspinWidget.setPosition(width - 60, height - 80);
    }
}