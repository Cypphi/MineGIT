package ca.modmonster.minegit.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitService;

public class EndpointURLScreen extends Screen {
    private static final Component API_URL_LABEL = Component.translatable("minegit.endpoint_url.api");
    private static final Component WEB_URL_LABEL = Component.translatable("minegit.endpoint_url.clone");
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
        GridLayout columnLayout = new GridLayout().spacing(8);
        columnLayout.defaultCellSetting().alignHorizontallyCenter();

        // Menu title
        layout.addToHeader(new StringWidget(this.title, this.font));

        int col = 0;

        if (selectedService == GitService.GITLAB) {
            MultiLineTextWidget gitlabMessage = new MultiLineTextWidget(Component.translatable("minegit.endpoint_url.gitlab_hint"), font).setMaxWidth(this.width - 50);
            columnLayout.addChild(gitlabMessage, col++, 0);
        }

        // Custom URL fields
        StringWidget apiUrlLabel = columnLayout.addChild(new StringWidget(API_URL_LABEL, font), col++, 0);
        apiUrlLabel.setAlpha(0.5f);
        apiUrlLabel.setTooltip(Tooltip.create(Component.translatable("minegit.endpoint_url.api.tooltip")));
        apiUrlEdit = new EditBox(font, 0, 0, 200, 20, API_URL_LABEL);
        apiUrlEdit.setMaxLength(255);
        apiUrlEdit.setResponder(string -> updateContinueButtonStatus());
        columnLayout.addChild(apiUrlEdit, col++, 0);

        StringWidget webUrlLabel = columnLayout.addChild(new StringWidget(WEB_URL_LABEL, font), col++, 0);
        webUrlLabel.setAlpha(0.5f);
        webUrlLabel.setTooltip(Tooltip.create(Component.translatable("minegit.endpoint_url.clone.tooltip")));
        webUrlEdit = new EditBox(font, 0, 0, 200, 20, WEB_URL_LABEL);
        webUrlEdit.setMaxLength(255);
        webUrlEdit.setResponder(string -> updateContinueButtonStatus());
        columnLayout.addChild(webUrlEdit, col++, 0);

        // Continue button
        continueButton = Button.builder(Component.translatable("minegit.endpoint_url.continue"), button -> {
            // Save credentials with service configuration
            Config currentConfig = ConfigManager.getCurrentConfig();
            Config config = new Config(currentConfig.username, currentConfig.patEncrypted, selectedService, webUrlEdit.getValue(), apiUrlEdit.getValue());
            ConfigManager.save(config);
            finishCallback.run();
        }).size(200, 20).build();
        columnLayout.addChild(continueButton, col++, 0);

        // Add layout widgets
        this.layout.addToContents(columnLayout);
        this.layout.visitWidgets(this::addRenderableWidget);
        this.layout.arrangeElements();

        // Back button
        Button backButton = Button.builder(Component.literal("←"), button -> onClose())
                .tooltip(Tooltip.create(Component.translatable("minegit.endpoint_url.back")))
                .bounds(6, 6, 20, 20)
                .build();
        addRenderableWidget(backButton);

        if (selectedService == GitService.GITLAB) {
            webUrlEdit.setValue(GitService.GITLAB.getDefaultWebUrl());
            apiUrlEdit.setValue(GitService.GITLAB.getDefaultApiUrl());
        }
        updateContinueButtonStatus();

        setInitialFocus(webUrlEdit);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderDirtBackground(guiGraphics);
        super.render(guiGraphics, i, j, f);
    }

    public void updateContinueButtonStatus() {
        continueButton.active = !webUrlEdit.getValue().isBlank() && !apiUrlEdit.getValue().isBlank();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
    }
}
