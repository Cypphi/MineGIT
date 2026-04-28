package ca.modmonster.minegit.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TwoChoiceScreen extends Screen {
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 20 + 4, 60);

    public TwoChoiceScreen(Component title, Component description, Component continueMessage, Component cancelMessage, Runnable continueCallback, Runnable cancelCallback) {
        super(title);
        this.description = description;
        this.continueMessage = continueMessage;
        this.cancelMessage = cancelMessage;
        this.continueCallback = continueCallback;
        this.cancelCallback = cancelCallback;
    }

    private final Component description;
    private final Component continueMessage;
    private final Component cancelMessage;
    private final Runnable continueCallback;
    private final Runnable cancelCallback;

    private MultiLineTextWidget descriptionWidget;

    @Override
    protected void init() {
        // Column layout
        GridLayout columnLayout = this.layout.addToContents(new GridLayout().spacing(8));
        columnLayout.defaultCellSetting().alignHorizontallyCenter();

        // Menu title
        layout.addToHeader(new StringWidget(this.title, this.font));

        // Confirmation message
        descriptionWidget = new MultiLineTextWidget(description, this.font).setMaxWidth(this.width - 50);
        columnLayout.addChild(descriptionWidget, 0, 0);
        columnLayout.addChild(new SpacerElement(200, 20), 1, 0);

        // Continue button
        GridLayout buttonRowLayout = columnLayout.addChild(new GridLayout().spacing(8), 2, 0);
        Button continueButton = Button.builder(continueMessage, button -> continueCallback.run()).build();
        buttonRowLayout.addChild(continueButton, 0, 0);

        // Cancel button
        Button cancelButton = Button.builder(cancelMessage, button -> cancelCallback.run()).build();
        buttonRowLayout.addChild(cancelButton, 0, 1);

        // Add layout widgets
        this.layout.visitWidgets(this::addRenderableWidget);
        this.layout.arrangeElements();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderDirtBackground(guiGraphics);
        super.render(guiGraphics, i, j, f);
    }

    @Override
    protected void repositionElements() {
        if (descriptionWidget != null) descriptionWidget.setMaxWidth(this.width - 50);
        layout.arrangeElements();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
