package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitService;
import ca.modmonster.minegit.widget.GitServiceButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SelectServiceScreen extends Screen {
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 20 + 4, 36);
    private final Screen parent;
    private final Runnable selectCallback;

    public SelectServiceScreen(Screen parent, Runnable selectCallback) {
        super(Component.translatable("minegit.select_service.title"));
        this.parent = parent;
        this.selectCallback = selectCallback;
    }

    @Override
    protected void init() {
        layout.addTitleHeader(this.title, this.font);

        GridLayout grid = new GridLayout();
        grid.spacing(8);

        int row = 0;
        int col = 0;
        for (GitService service : GitService.values()) {
            Button button = new GitServiceButton(
                    Component.literal(service.getShortName()), service.getIcon(),
                    (b) -> selectService(service)
            );
            grid.addChild(button, row, col);

            col++;
            if (col > 3) {
                col = 0;
                row++;
            }
        }

        layout.addToContents(grid);
        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();

        // Back button
        Button backButton = Button.builder(Component.literal("←"), button -> onClose())
                .tooltip(Tooltip.create(Component.translatable("gui.back")))
                .bounds(6, 6, 20, 20)
                .build();
        addRenderableWidget(backButton);
    }

    public void selectService(GitService service) {
        if (service.requiresCustomUrl()) {
            // Open screen to provide custom URL
            minecraft.setScreen(new EndpointURLScreen(this, selectCallback, service));
        } else {
            // We do not need a custom URL; do a save
            Config currentConfig = ConfigManager.getCurrentConfig();
            Config config = new Config(currentConfig.username, currentConfig.patEncrypted, service, "", "", false);
            ConfigManager.save(config);
            selectCallback.run();
        }
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
