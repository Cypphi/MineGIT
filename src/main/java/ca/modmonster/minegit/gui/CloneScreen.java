package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.GitManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;

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
        super(new TranslatableText("minegit.clone.title"));
        this.closeCallback = closeCallback;
        this.cloneSuccessCallback = cloneSuccessCallback;
    }

    @Override
    protected void init() {
        // Repo name text field
        repoEdit = new TextFieldWidget(font, this.width / 2 - 100, 107, 200, 20, I18n.translate("minegit.clone.repo"));
        repoEdit.setMaxLength(39);
        repoEdit.setChangedListener(string -> updateButtonsStatus());
        this.children.add(repoEdit);

        // Clone button
        cloneButton = new ButtonWidget(this.width / 2 - 100, 135, 200, 20, I18n.translate("minegit.clone.confirm"), button -> doClone());
        addButton(cloneButton);

        // Back button
        backButton = new ButtonWidget(6, 6, 20, 20, "←", button -> onClose());
        addButton(backButton);

        // Configure button
        configureButton = new ButtonWidget(width - 26, 6, 20, 20, "☁", button -> minecraft.openScreen(new AccountLinkScreen(this)));
        addButton(configureButton);

        // Ralsei go spinny
        ralspinWidget = new RalspinWidget(width - 60, height - 80);
        this.children.add(ralspinWidget);

        updateButtonsStatus();
        setInitialFocus(repoEdit);
        repoEdit.changeFocus(true);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(i, j, f);
        drawCenteredString(this.font, this.title.getString(), this.width / 2, 50, 16777215);
        drawCenteredString(this.font, I18n.translate("minegit.clone.repo"), this.width / 2, 90, -2130706433);
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
        GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableText("minegit.clone.in_progress"));
        minecraft.openScreen(progressScreen);
        new Thread(() -> {
            int result = GitManager.cloneRepo(minecraft, repoEdit.getText(), progressScreen);

            minecraft.execute(() -> {
                if (result == 0) {
                    minecraft.getToastManager().add(new WideToast(I18n.translate("minegit.clone.success")));
                    if (cloneSuccessCallback != null) {
                        cloneSuccessCallback.run();
                    } else {
                        onClose();
                    }
                } else if (result == 1) {
                    minecraft.getToastManager().add(new WideToast(I18n.translate("minegit.clone.error.invalid_remote")));
                    minecraft.openScreen(this);
                    updateButtonsStatus();
                } else {
                    minecraft.getToastManager().add(new WideToast(I18n.translate("minegit.clone.error.generic")));
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
    public void onClose() {
        closeCallback.run();
    }
}
