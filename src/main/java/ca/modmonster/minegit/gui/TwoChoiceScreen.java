package ca.modmonster.minegit.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TwoChoiceScreen extends Screen {
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

    @Override
    protected void init() {
        // Confirmation message
        MultiLineTextWidget descriptionWidget = MultiLineTextWidget.createCentered(this.width - 50, this.font, description);
        descriptionWidget.setPosition((this.width - descriptionWidget.getWidth()) / 2, 90);
        addRenderableWidget(descriptionWidget);

        // Continue button
        Button continueButton = Button.builder(continueMessage, button -> continueCallback.run()).build();
        continueButton.setPosition(width / 2 - 152, 98 + descriptionWidget.getHeight());
        addRenderableWidget(continueButton);

        // Cancel button
        Button cancelButton = Button.builder(cancelMessage, button -> cancelCallback.run()).build();
        cancelButton.setPosition(width / 2 + 2, 98 + descriptionWidget.getHeight());
        addRenderableWidget(cancelButton);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 16777215);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
