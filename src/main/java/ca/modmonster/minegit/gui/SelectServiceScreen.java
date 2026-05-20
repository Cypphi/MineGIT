package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.data.GitService;
import ca.modmonster.minegit.widget.GitServiceButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class SelectServiceScreen extends Screen {
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 20 + 4, 36);
    private final Consumer<GitService> closeCallback;

    public SelectServiceScreen(Consumer<GitService> closeCallback) {
        super(Component.translatable("minegit.select_service.title"));
        this.closeCallback = closeCallback;
    }

    @Override
    protected void init() {
        layout.addTitleHeader(this.title, this.font);

        GridLayout grid = new GridLayout();
        grid.spacing(8);

        int row = 0;
        int col = 0;
        for (GitService service : GitService.values()) {
            Button githubButton = new GitServiceButton(
                    Component.literal(service.getShortName()), service.getIcon(),
                    (button) -> closeCallback.accept(service)
            );
            grid.addChild(githubButton, row, col);

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
                .tooltip(Tooltip.create(Component.translatable("minegit.select_service.back")))
                .bounds(6, 6, 20, 20)
                .build();
        addRenderableWidget(backButton);
    }

    @Override
    public void onClose() {
        closeCallback.accept(null);
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
    }
}
