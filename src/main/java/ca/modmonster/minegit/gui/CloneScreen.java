package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.GitManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;

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
    protected void init() {
        // Repo name text field
        repoEdit = new TextFieldWidget(0, textRenderer, this.width / 2 - 100, 107, 200, 20);
        repoEdit.setMaxLength(39);
        this.children.add(repoEdit);

        // Clone button
        cloneButton = new ButtonWidget(1, this.width / 2 - 100, 135, 200, 20, I18n.translate("minegit.clone.confirm")) {
            @Override
            public void click(double mouseX, double mouseY) {
                doClone();
            }
        };
        addButton(cloneButton);

        // Back button
        backButton = new ImageButton(2, 6, 6, ImageButton.ImageButtonTex.BACK) {
            @Override
            public void click(double mouseX, double mouseY) {
                close();
            }
        };
        addButton(backButton);

        // Configure button
        configureButton = new ImageButton(3, width - 26, 6, ImageButton.ImageButtonTex.CLOUD) {
            @Override
            public void click(double mouseX, double mouseY) {
                minecraft.openScreen(new AccountLinkScreen(CloneScreen.this));
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
        } else if (i != 257 && i != 335) {
            return false;
        } else {
            close();
            return true;
        }
    }

    @Override
    public void render(int i, int j, float f) {
        this.drawBackgroundTexture(i);
        super.render(i, j, f);
        drawCenteredString(textRenderer, I18n.translate("minegit.clone.title"), this.width / 2, 50, 16777215);
        drawCenteredString(textRenderer, I18n.translate("minegit.clone.repo"), this.width / 2, 90, -2130706433);
        repoEdit.render(i, j, f);
        ralspinWidget.render(i, j, f);
        if (backButton.isHovered()) renderTooltip(I18n.translate("minegit.clone.back"), i, j);
        if (configureButton.isHovered()) renderTooltip(I18n.translate("minegit.link.setup.open"), i, j);
        if (ralspinWidget.isHovered()) renderTooltip(RalspinWidget.TOOLTIP, i, j);
    }

    @Override
    public void tick() {
        repoEdit.tick();
    }

    private void doClone() {
        GitProgressScreen progressScreen = new GitProgressScreen(I18n.translate("minegit.clone.in_progress"));
        minecraft.openScreen(progressScreen);
        new Thread(() -> {
            int result = GitManager.cloneRepo(minecraft, repoEdit.getText(), progressScreen);

            minecraft.executeTask(() -> {
                if (result == 0) {
                    minecraft.getToasts().add(new WideToast(I18n.translate("minegit.clone.success")));
                    if (cloneSuccessCallback != null) {
                        cloneSuccessCallback.run();
                    } else {
                        close();
                    }
                } else if (result == 1) {
                    minecraft.getToasts().add(new WideToast(I18n.translate("minegit.clone.error.invalid_remote")));
                    minecraft.openScreen(this);
                    updateButtonsStatus();
                } else {
                    minecraft.getToasts().add(new WideToast(I18n.translate("minegit.clone.error.generic")));
                    minecraft.openScreen(this);
                    updateButtonsStatus();
                }
            });
        }).start();
    }

    private void updateButtonsStatus() {
        cloneButton.active = !repoEdit.getText().replace(" ", "").isEmpty();
    }

    @Override
    public void close() {
        closeCallback.run();
    }
}
