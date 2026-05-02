package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.GitManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class CloneScreen extends GuiScreen {
    private final Runnable closeCallback;
    private final Runnable cloneSuccessCallback;
    private GuiTextField repoEdit;
    private GuiButton cloneButton;
    private GuiButton backButton;
    private GuiButton configureButton;
    private RalspinWidget ralspinWidget;

    public CloneScreen(Runnable closeCallback) {
        this(closeCallback, null);
    }

    public CloneScreen(Runnable closeCallback, Runnable cloneSuccessCallback) {
        this.closeCallback = closeCallback;
        this.cloneSuccessCallback = cloneSuccessCallback;
    }

    @Override
    protected void initGui() {
        // Repo name text field
        repoEdit = new GuiTextField(0, fontRenderer, this.width / 2 - 100, 107, 200, 20);
        repoEdit.setMaxStringLength(39);
        this.children.add(repoEdit);

        // Clone button
        cloneButton = new GuiButton(1, this.width / 2 - 100, 135, 200, 20, I18n.format("minegit.clone.confirm")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                doClone();
            }
        };
        addButton(cloneButton);

        // Back button
        backButton = new GuiButton(2, 6, 6, 20, 20, "←") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                close();
            }
        };
        addButton(backButton);

        // Configure button
        configureButton = new GuiButton(3, width - 26, 6, 20, 20, "☁") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                mc.displayGuiScreen(new AccountLinkScreen(CloneScreen.this));
            }
        };
        addButton(configureButton);

        // Ralsei go spinny
        ralspinWidget = new RalspinWidget(width - 60, height - 80);
        this.children.add(ralspinWidget);

        updateButtonsStatus();
        setFocused(repoEdit);
        repoEdit.setFocused(true);
    }

    @Override
    public boolean charTyped(char i, int j) {
        if (this.repoEdit.charTyped(i, j)) {
            updateButtonsStatus();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (this.repoEdit.keyPressed(i, j, k)) {
            updateButtonsStatus();
            return true;
        } else if (i != 257 && i != 335) { // TODO: wtf is thissssssss
            return false;
        } else {
            close();
            return true;
        }
    }

    @Override
    public void render(int i, int j, float f) {
        this.drawDefaultBackground();
        super.render(i, j, f);
        drawCenteredString(fontRenderer, I18n.format("minegit.clone.title"), this.width / 2, 50, 16777215);
        drawCenteredString(fontRenderer, I18n.format("minegit.clone.repo"), this.width / 2, 90, -2130706433);
        repoEdit.drawTextField(i, j, f);
        ralspinWidget.render(i, j);
        if (backButton.isMouseOver()) drawHoveringText(I18n.format("minegit.clone.back"), i, j);
        if (configureButton.isMouseOver()) drawHoveringText(I18n.format("minegit.link.setup.open"), i, j);
        if (ralspinWidget.isMouseOver()) drawHoveringText(RalspinWidget.TOOLTIP, i, j);
    }

    @Override
    public void tick() {
        repoEdit.tick();
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

    @Override
    public void close() {
        closeCallback.run();
    }
}
