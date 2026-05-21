package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.data.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class AccountLinkScreen extends Screen {
    private static final Component USERNAME_EDIT_LABEL = Component.translatable("minegit.link.username");
    private static final Component PAT_EDIT_LABEL = Component.translatable("minegit.link.pat");
    private static final Identifier RALSPIN = Identifier.fromNamespaceAndPath("minegit", "ralspin");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 20 + 4, 36);

    private final Screen parent;
    private final Runnable closeCallback;
    private EditBox usernameEdit;
    private EditBox patEdit;
    private Button testCredentialsButton;
    private ImageWidget ralspinWidget;
    private boolean requestInProgress = false;
    private StringWidget testCredentialsStatus;
    private StringWidget selectedServiceLabel;

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
        LinearLayout columnLayout = LinearLayout.vertical().spacing(8);
        columnLayout.defaultCellSetting().alignHorizontallyCenter();

        // Menu title
        layout.addTitleHeader(this.title, this.font);

        LinearLayout serviceRow = columnLayout.addChild(LinearLayout.horizontal().spacing(4));

        selectedServiceLabel = new StringWidget(Component.empty(), font);
        updateService();
        serviceRow.addChild(selectedServiceLabel);

        SelectServiceScreen selectServiceScreen = new SelectServiceScreen(this, () -> {
            minecraft.setScreen(this);
            updateService();
            updateTestButtonStatus(false);
        });
        Button changeServiceButton = Button.builder(Component.translatable("minegit.link.select_service"), (button) ->
                minecraft.setScreen(selectServiceScreen)).size(70, 20).build();
        serviceRow.addChild(changeServiceButton);

        // Username text field
        StringWidget usernameEditLabel = columnLayout.addChild(new StringWidget(USERNAME_EDIT_LABEL, font));
        usernameEditLabel.setAlpha(0.5f);
        usernameEdit = new EditBox(font, 0, 0, 200, 20, USERNAME_EDIT_LABEL);
        usernameEdit.setMaxLength(39);
        usernameEdit.setResponder(string -> updateTestButtonStatus(false));
        columnLayout.addChild(usernameEdit);

        // PAT text field
        StringWidget patEditLabel = columnLayout.addChild(new StringWidget(PAT_EDIT_LABEL, font));
        patEditLabel.setAlpha(0.5f);
        patEdit = new EditBox(font, 0, 0, 200, 20, PAT_EDIT_LABEL);
        patEdit.setMaxLength(255);
        patEdit.setResponder(string -> updateTestButtonStatus(false));
        columnLayout.addChild(patEdit);

        // Test credentials button
        testCredentialsButton = Button.builder(Component.translatable("minegit.link.test"), button -> testCredentials()).size(200, 20).build();
        columnLayout.addChild(testCredentialsButton);

        // Test credentials status
        testCredentialsStatus = new StringWidget(Component.empty(), font);
        columnLayout.addChild(testCredentialsStatus);

        // Add layout widgets
        layout.addToContents(columnLayout);
        this.layout.visitWidgets(this::addRenderableWidget);
        this.layout.arrangeElements();

        // Back button
        Button backButton = Button.builder(Component.literal("←"), button -> onClose())
            .tooltip(Tooltip.create(Component.translatable("minegit.link.back")))
            .bounds(6, 6, 20, 20)
            .build();
        addRenderableWidget(backButton);

        // Ralsei go spinny
        ralspinWidget = ImageWidget.sprite(42, 80, RALSPIN);
        ralspinWidget.setPosition(width - 60, height - 80);
        ralspinWidget.setTooltip(Tooltip.create(Component.literal("hiiiii!! ^-^")));
        addRenderableWidget(ralspinWidget);

        updateTestButtonStatus(false);

        // Load configuration and update default values
        Config config = ConfigManager.getCurrentConfig();
        usernameEdit.setValue(config.username);
        String pat = config.getPat();
        if (pat != null) patEdit.setValue(pat);
    }

    private void updateService() {
        selectedService = ConfigManager.getCurrentConfig().gitService;
        selectedServiceLabel.setMessage(Component.literal(selectedService.getDisplayName()));
        selectedServiceLabel.setSize(126, 22);
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
    protected void setInitialFocus() {
        setInitialFocus(usernameEdit);
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        ralspinWidget.setPosition(width - 60, height - 80);
    }
}