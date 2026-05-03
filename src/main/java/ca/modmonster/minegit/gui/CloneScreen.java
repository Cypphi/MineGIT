package ca.modmonster.minegit.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.GitManager;

public class CloneScreen extends GuiScreen {
    private final Runnable closeCallback;
    private final Runnable cloneSuccessCallback;
    private GuiTextField repoEdit;
    private GuiButton cloneButton;
    private ImageButton backButton;
    private ImageButton configureButton;
    private RalspinWidget ralspinWidget;

    public CloneScreen(Runnable closeCallback) {
        this(closeCallback, null);
    }

    public CloneScreen(Runnable closeCallback, Runnable cloneSuccessCallback) {
        this.closeCallback = closeCallback;
        this.cloneSuccessCallback = cloneSuccessCallback;
    }

    @Override
    public void initGui() {
        // Repo name text field
        repoEdit = new GuiTextField(0, fontRenderer, this.width / 2 - 100, 107, 200, 20);
        repoEdit.setMaxStringLength(39);

        // Clone button
        cloneButton = new GuiButton(1, this.width / 2 - 100, 135, 200, 20, I18n.format("minegit.clone.confirm"));
        addButton(cloneButton);

        // Back button
        backButton = new ImageButton(2, 6, 6, ImageButton.ImageButtonTex.BACK);
        addButton(backButton);

        // Configure button
        configureButton = new ImageButton(3, width - 26, 6, ImageButton.ImageButtonTex.CLOUD);
        addButton(configureButton);

        // Ralsei go spinny
        ralspinWidget = new RalspinWidget(width - 60, height - 80);

        updateButtonsStatus();
        repoEdit.setFocused(true);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            doClone();
        } else if (button.id == 2) {
            close();
        } else if (button.id == 3) {
            mc.displayGuiScreen(new AccountLinkScreen(CloneScreen.this));
        }
    }

    @Override
    public void keyTyped(char i, int j) {
        if (this.repoEdit.textboxKeyTyped(i, j)) {
            updateButtonsStatus();
        }
        if (j == 1) close();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.repoEdit.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        this.drawDefaultBackground();
        super.drawScreen(i, j, f);
        drawCenteredString(fontRenderer, I18n.format("minegit.clone.title"), this.width / 2, 50, 16777215);
        drawCenteredString(fontRenderer, I18n.format("minegit.clone.repo"), this.width / 2, 90, -2130706433);
        repoEdit.drawTextBox();
        ralspinWidget.render(i, j);
        if (backButton.isMouseOver()) drawHoveringText(I18n.format("minegit.clone.back"), i, j);
        if (configureButton.isMouseOver()) drawHoveringText(I18n.format("minegit.link.setup.open"), i, j);
        if (ralspinWidget.isMouseOver()) drawHoveringText(RalspinWidget.TOOLTIP, i, j);
    }

    private void doClone() {
        GitProgressScreen progressScreen = new GitProgressScreen(I18n.format("minegit.clone.in_progress"));
        mc.displayGuiScreen(progressScreen);
        new Thread(() -> {
            int result = GitManager.cloneRepo(mc, repoEdit.getText(), progressScreen);

            mc.addScheduledTask(() -> {
                if (result == 0) {
                    mc.getToastGui().add(new WideToast(I18n.format("minegit.clone.success")));
                    if (cloneSuccessCallback != null) {
                        cloneSuccessCallback.run();
                    } else {
                        close();
                    }
                } else if (result == 1) {
                    mc.getToastGui().add(new WideToast(I18n.format("minegit.clone.error.invalid_remote")));
                    mc.displayGuiScreen(this);
                    updateButtonsStatus();
                } else {
                    mc.getToastGui().add(new WideToast(I18n.format("minegit.clone.error.generic")));
                    mc.displayGuiScreen(this);
                    updateButtonsStatus();
                }
            });
        }).start();
    }

    private void updateButtonsStatus() {
        cloneButton.enabled = !repoEdit.getText().replace(" ", "").isEmpty();
    }

    public void close() {
        closeCallback.run();
    }
}
