package ca.modmonster.minegit.gui;

import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitService;

public class EndpointURLScreen extends Screen {
    private static final Component API_URL_LABEL = Component.translatable("minegit.endpoint_url.api").append(" ⓘ");
    private static final Component WEB_URL_LABEL = Component.translatable("minegit.endpoint_url.clone").append(" ⓘ");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 20 + 4, 36);

    private final Screen parent;
    private final Runnable finishCallback;
    private final GitService selectedService;
    private EditBox webUrlEdit;
    private EditBox apiUrlEdit;
    private Button continueButton;

    public EndpointURLScreen(Screen parent, Runnable finishCallback, GitService selectedService) {
        super(Component.translatable("minegit.endpoint_url.title", selectedService.getNaturalName()));
        this.parent = parent;
        this.finishCallback = finishCallback;
        this.selectedService = selectedService;
    }

    @Override
    protected void init() {
        // Column layout
        LinearLayout columnLayout = LinearLayout.vertical().spacing(8);
        columnLayout.defaultCellSetting().alignHorizontallyCenter();

        // Menu title
        layout.addTitleHeader(this.title, this.font);

        if (selectedService == GitService.GITLAB) {
            MultiLineTextWidget gitlabMessage = new MultiLineTextWidget(Component.translatable("minegit.endpoint_url.gitlab_hint"), font).setMaxWidth(this.width - 50);
            columnLayout.addChild(gitlabMessage);
        }

        // Custom URL fields
        StringWidget apiUrlLabel = columnLayout.addChild(new StringWidget(API_URL_LABEL, font));
        apiUrlLabel.setAlpha(0.5f);
        apiUrlLabel.setTooltip(Tooltip.create(Component.translatable("minegit.endpoint_url.api.tooltip")));
        apiUrlEdit = new EditBox(font, 0, 0, 200, 20, API_URL_LABEL);
        apiUrlEdit.setMaxLength(255);
        apiUrlEdit.setResponder(string -> updateContinueButtonStatus());
        columnLayout.addChild(apiUrlEdit);

        StringWidget webUrlLabel = columnLayout.addChild(new StringWidget(WEB_URL_LABEL, font));
        webUrlLabel.setAlpha(0.5f);
        webUrlLabel.setTooltip(Tooltip.create(Component.translatable("minegit.endpoint_url.clone.tooltip")));
        webUrlEdit = new EditBox(font, 0, 0, 200, 20, WEB_URL_LABEL);
        webUrlEdit.setMaxLength(255);
        webUrlEdit.setResponder(string -> updateContinueButtonStatus());
        columnLayout.addChild(webUrlEdit);

        // Continue button
        continueButton = Button.builder(Component.translatable("gui.continue"), button -> {
            // Save credentials with service configuration
            Config currentConfig = ConfigManager.getCurrentConfig();
            Config config = new Config(currentConfig.username, currentConfig.patEncrypted, selectedService, webUrlEdit.getValue(), apiUrlEdit.getValue());
            ConfigManager.save(config);
            finishCallback.run();
        }).size(200, 20).build();
        columnLayout.addChild(continueButton);

        // Add layout widgets
        this.layout.addToContents(columnLayout);
        this.layout.visitWidgets(this::addRenderableWidget);
        this.layout.arrangeElements();

        // Back button
        Button backButton = Button.builder(Component.literal("←"), button -> onClose())
                .tooltip(Tooltip.create(Component.translatable("gui.back")))
                .bounds(6, 6, 20, 20)
                .build();
        addRenderableWidget(backButton);

        if (selectedService == GitService.GITLAB) {
            webUrlEdit.setValue(GitService.GITLAB.getDefaultWebUrl());
            apiUrlEdit.setValue(GitService.GITLAB.getDefaultApiUrl());
        }
        updateContinueButtonStatus();
    }

    public void updateContinueButtonStatus() {
        continueButton.active = !webUrlEdit.getValue().isBlank() && !apiUrlEdit.getValue().isBlank();
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    @Override
    protected void setInitialFocus() {
        setInitialFocus(webUrlEdit);
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
    }
}
