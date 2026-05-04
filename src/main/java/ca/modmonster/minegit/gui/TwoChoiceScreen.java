package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.MultiLineLabel;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_01559903;

public class TwoChoiceScreen extends Screen {
    public TwoChoiceScreen(Text title, String description, String continueMessage, String cancelMessage, Runnable continueCallback, Runnable cancelCallback) {
        super(title);
        this.description = description;
        this.continueMessage = continueMessage;
        this.cancelMessage = cancelMessage;
        this.continueCallback = continueCallback;
        this.cancelCallback = cancelCallback;
    }

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
        C_01559903 continueButton = new C_01559903(width / 2 - 152, 98 + descriptionHeight, 150, 20, continueMessage, button -> continueCallback.run());
        addButton(continueButton);

        // Cancel button
        C_01559903 cancelButton = new C_01559903(width / 2 + 2, 98 + descriptionHeight, 150, 20, cancelMessage, button -> cancelCallback.run());
        addButton(cancelButton);
    }

    @Override
    public void render(int i, int j, float f) {
        this.drawBackgroundTexture(i);
        super.render(i, j, f);
        drawCenteredString(textRenderer, this.f_89436361.getString(), this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(this.width / 2, 90);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
