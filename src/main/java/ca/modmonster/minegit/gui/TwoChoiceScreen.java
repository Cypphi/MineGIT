package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.MultiLineLabel;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class TwoChoiceScreen extends Screen {
    public TwoChoiceScreen(String title, String description, String continueMessage, String cancelMessage, Runnable continueCallback, Runnable cancelCallback) {
        this.title = title;
        this.description = description;
        this.continueMessage = continueMessage;
        this.cancelMessage = cancelMessage;
        this.continueCallback = continueCallback;
        this.cancelCallback = cancelCallback;
    }

    private final String title;
    private final String description;
    private final String continueMessage;
    private final String cancelMessage;
    private final Runnable continueCallback;
    private final Runnable cancelCallback;
    private MultiLineLabel descriptionWidget;

    @Override
    protected void init() {
        // Confirmation message
        descriptionWidget = MultiLineLabel.create(this.textRenderer, description, this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Continue button
        ButtonWidget continueButton = new ButtonWidget(0, width / 2 - 152, 98 + descriptionHeight, 150, 20, continueMessage) {
            @Override
            public void click(double mouseX, double mouseY) {
                continueCallback.run();
            }
        };
        addButton(continueButton);

        // Cancel button
        ButtonWidget cancelButton = new ButtonWidget(1, width / 2 + 2, 98 + descriptionHeight, 150, 20, cancelMessage) {
            @Override
            public void click(double mouseX, double mouseY) {
                cancelCallback.run();
            }
        };
        addButton(cancelButton);
    }

    @Override
    public void render(int i, int j, float f) {
        this.drawBackgroundTexture(i);
        super.render(i, j, f);
        drawCenteredString(textRenderer, title, this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(this.width / 2, 90);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
