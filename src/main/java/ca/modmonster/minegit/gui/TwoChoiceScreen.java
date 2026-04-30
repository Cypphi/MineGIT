package ca.modmonster.minegit.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
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
    private MultiLineLabel descriptionWidget;

    @Override
    protected void init() {
        // Confirmation message
        descriptionWidget = MultiLineLabel.create(this.font, description, this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Continue button
        Button continueButton = new Button(width / 2 - 152, 98 + descriptionHeight, 150, 20, continueMessage, button -> continueCallback.run());
        addButton(continueButton);

        // Cancel button
        Button cancelButton = new Button(width / 2 + 2, 98 + descriptionHeight, 150, 20, cancelMessage, button -> cancelCallback.run());
        addButton(cancelButton);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(poseStack, this.width / 2, 90);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
