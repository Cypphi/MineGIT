package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.*;
import ca.modmonster.minegit.backport.toast.Toast;
import ca.modmonster.minegit.backport.toast.ToastManager;
import ca.modmonster.minegit.data.GitManager;
import io.github.prospector.modmenu.gui.TextFieldWidget;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;

public class CloneScreen extends Screen {
    private final Runnable closeCallback;
    private final Runnable cloneSuccessCallback;
    private TextFieldWidget repoEdit;
    private ButtonElement cloneButton;
    private ButtonElement backButton;
    private ButtonElement configureButton;
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
        repoEdit = new TextFieldWidget(null, this.width / 2 - 100, 107, 200, 20);
        repoEdit.setMaxStringLength(39);

        // Clone button
        cloneButton = new ButtonElement(1, this.width / 2 - 100, 135, 200, 20, "Clone");
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
    protected void buttonClicked(ButtonElement button) {
        if (button.id == 1) {
            doClone();
        } else if (button.id == 2) {
            close();
        } else if (button.id == 3) {
            mc.displayScreen(new AccountLinkScreen(CloneScreen.this));
        }
    }

    @Override
    public void keyPressed(char i, int j, int k, int l) {
        if (this.repoEdit.isFocused()) {
            this.repoEdit.textboxKeyTyped(i, j);
            updateButtonsStatus();
        }
        if (j == 1) close();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.repoEdit.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderTexturedBackground();
        drawStringCenteredShadow(fontRenderer, "Clone World", this.width / 2, 50, 16777215);
        drawStringCenteredShadow(fontRenderer, "Repository Name", this.width / 2, 90, -2130706433);
        repoEdit.drawTextBox();
        ralspinWidget.render(mc, i, j);
        super.render(i, j, f);
        if (ScreenUtil.isHovered(i, j, backButton.xPosition, backButton.yPosition, 20, 20))
            ((ScreenTooltipRenderer) this).renderTooltip("Back", i, j);
        if (ScreenUtil.isHovered(i, j, configureButton.xPosition, configureButton.yPosition, 20, 20))
            ((ScreenTooltipRenderer) this).renderTooltip("Open Cloud Sync Setup", i, j);
        if (ralspinWidget.isHovered()) ((ScreenTooltipRenderer) this).renderTooltip(RalspinWidget.TOOLTIP, i, j);
    }

    @Override
    public void tick() {
        repoEdit.tick();
    }

    private void doClone() {
        GitProgressScreen progressScreen = new GitProgressScreen("Cloning world...");
        mc.displayScreen(progressScreen);
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
                    mc.displayScreen(this);
                    updateButtonsStatus();
                } else {
                    ToastManager.INSTANCE.add(new Toast("Something went wrong when trying to clone the world."));
                    mc.displayScreen(this);
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
