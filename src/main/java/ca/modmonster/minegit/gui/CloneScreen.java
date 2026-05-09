package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.*;
import ca.modmonster.minegit.backport.toast.Toast;
import ca.modmonster.minegit.backport.toast.ToastManager;
import ca.modmonster.minegit.data.GitManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class CloneScreen extends Screen {
    private final Runnable closeCallback;
    private final Runnable cloneSuccessCallback;
    private TextFieldWidget repoEdit;
    private ButtonWidget cloneButton;
    private ButtonWidget backButton;
    private ButtonWidget configureButton;
    private RalspinWidget ralspinWidget;

    public CloneScreen(Runnable closeCallback) {
        this(closeCallback, null);
    }

    public CloneScreen(Runnable closeCallback, Runnable cloneSuccessCallback) {
        this.closeCallback = closeCallback;
        this.cloneSuccessCallback = cloneSuccessCallback;
    }

    @Override
    public void init() {
        // Repo name text field
        repoEdit = new TextFieldWidget(this, textRenderer, this.width / 2 - 100, 107, 200, 20, "");
        repoEdit.setMaxLength(39);

        // Clone button
        cloneButton = new ButtonWidget(1, this.width / 2 - 100, 135, 200, 20, "Clone");
        buttons.add(cloneButton);

        // Back button
        backButton = new ImageButton(2, 6, 6, ImageButton.ImageButtonTex.BACK);
        buttons.add(backButton);

        // Configure button
        configureButton = new ImageButton(3, width - 26, 6, ImageButton.ImageButtonTex.CLOUD);
        buttons.add(configureButton);

        // Ralsei go spinny
        ralspinWidget = new RalspinWidget(width - 60, height - 80);

        updateButtonsStatus();
        repoEdit.setFocused(true);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 1) {
            doClone();
        } else if (button.id == 2) {
            close();
        } else if (button.id == 3) {
            minecraft.setScreen(new AccountLinkScreen(CloneScreen.this));
        }
    }

    @Override
    public void keyPressed(char i, int j) {
        this.repoEdit.keyPressed(i, j);
        updateButtonsStatus();
        if (j == 1) close();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.repoEdit.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackgroundTexture(i);
        drawCenteredTextWithShadow(textRenderer, "Clone World", this.width / 2, 50, 16777215);
        drawCenteredTextWithShadow(textRenderer, "Repository Name", this.width / 2, 90, -2130706433);
        repoEdit.render();
        ralspinWidget.render(minecraft, i, j);
        super.render(i, j, f);
        if (ScreenUtil.isHovered(i, j, backButton.x, backButton.y, 20, 20))
            ((ScreenTooltipRenderer) this).renderTooltip("Back", i, j);
        if (ScreenUtil.isHovered(i, j, configureButton.x, configureButton.y, 20, 20))
            ((ScreenTooltipRenderer) this).renderTooltip("Open Cloud Sync Setup", i, j);
        if (ralspinWidget.isHovered()) ((ScreenTooltipRenderer) this).renderTooltip(RalspinWidget.TOOLTIP, i, j);
    }

    @Override
    public void tick() {
        repoEdit.tick();
    }

    private void doClone() {
        GitProgressScreen progressScreen = new GitProgressScreen("Cloning world...");
        minecraft.setScreen(progressScreen);
        new Thread(() -> {
            int result = GitManager.cloneRepo(repoEdit.getText(), progressScreen);

            MainThreadTasks.execute(() -> {
                if (result == 0) {
                    ToastManager.INSTANCE.add(new Toast("Successfully cloned the world from GitHub!"));
                    if (cloneSuccessCallback != null) {
                        cloneSuccessCallback.run();
                    } else {
                        close();
                    }
                } else if (result == 1) {
                    ToastManager.INSTANCE.add(new Toast("The world you are looking for doesn't exist!"));
                    minecraft.setScreen(this);
                    updateButtonsStatus();
                } else {
                    ToastManager.INSTANCE.add(new Toast("Something went wrong when trying to clone the world."));
                    minecraft.setScreen(this);
                    updateButtonsStatus();
                }
            });
        }).start();
    }

    private void updateButtonsStatus() {
        cloneButton.active = !repoEdit.getText().replace(" ", "").isEmpty();
    }

    public void close() {
        closeCallback.run();
    }
}
