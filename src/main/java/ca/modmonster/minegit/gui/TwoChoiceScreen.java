package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.MultiLineLabel;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class TwoChoiceScreen extends GuiScreen {
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
    public void initGui() {
        // Confirmation message
        descriptionWidget = MultiLineLabel.create(this.fontRendererObj, description, this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Continue button
        GuiButton continueButton = new GuiButton(0, width / 2 - 152, 98 + descriptionHeight, 150, 20, continueMessage);
        buttonList.add(continueButton);

        // Cancel button
        GuiButton cancelButton = new GuiButton(1, width / 2 + 2, 98 + descriptionHeight, 150, 20, cancelMessage);
        buttonList.add(cancelButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            continueCallback.run();
        } else if (button.id == 1) {
            cancelCallback.run();
        }
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        this.drawDefaultBackground();
        drawCenteredString(fontRendererObj, title, this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(this.width / 2, 90);
        super.drawScreen(i, j, f);
    }

    @Override
    protected void keyTyped(char i, int j) {}
}
