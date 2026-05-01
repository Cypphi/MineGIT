package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.GitManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

public class CloneScreen extends Screen {
    private final Runnable closeCallback;
    private final Runnable cloneSuccessCallback;
    private EditBox repoEdit;
    private Button cloneButton;
    private Button backButton;
    private Button configureButton;
    private RalspinWidget ralspinWidget;

    public CloneScreen(Runnable closeCallback) {
        this(closeCallback, null);
    }

    public CloneScreen(Runnable closeCallback, Runnable cloneSuccessCallback) {
        super(new TranslatableComponent("minegit.clone.title"));
        this.closeCallback = closeCallback;
        this.cloneSuccessCallback = cloneSuccessCallback;
    }

    @Override
    protected void init() {
        // Repo name text field
        repoEdit = new EditBox(font, this.width / 2 - 100, 107, 200, 20, I18n.get("minegit.clone.repo"));
        repoEdit.setMaxLength(39);
        repoEdit.setResponder(string -> updateButtonsStatus());
        this.children.add(repoEdit);

        // Clone button
        cloneButton = new Button(this.width / 2 - 100, 135, 200, 20, I18n.get("minegit.clone.confirm"), button -> doClone());
        addButton(cloneButton);

        // Back button
        backButton = new Button(6, 6, 20, 20, "←", button -> onClose());
        addButton(backButton);

        // Configure button
        configureButton = new Button(width - 26, 6, 20, 20, "☁", button -> minecraft.setScreen(new AccountLinkScreen(this)));
        addButton(configureButton);

        // Ralsei go spinny
        ralspinWidget = new RalspinWidget(width - 60, height - 80);
        this.children.add(ralspinWidget);

        updateButtonsStatus();
        setInitialFocus(repoEdit);
        repoEdit.setFocus(true);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(i, j, f);
        drawCenteredString(this.font, this.title.getString(), this.width / 2, 50, 16777215);
        drawCenteredString(this.font, I18n.get("minegit.clone.repo"), this.width / 2, 90, -2130706433);
        repoEdit.render(i, j, f);
        ralspinWidget.render(i, j, f);
        if (backButton.isHovered()) renderTooltip(I18n.get("minegit.clone.back"), i, j);
        if (configureButton.isHovered()) renderTooltip(I18n.get("minegit.link.setup.open"), i, j);
        if (ralspinWidget.isHovered()) renderTooltip(RalspinWidget.TOOLTIP, i, j);
    }

    @Override
    public void tick() {
        repoEdit.tick();
    }

    private void doClone() {
        GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableComponent("minegit.clone.in_progress"));
        minecraft.setScreen(progressScreen);
        new Thread(() -> {
            int result = GitManager.cloneRepo(minecraft, repoEdit.getValue(), progressScreen);

            minecraft.submit(() -> {
                if (result == 0) {
                    minecraft.getToasts().addToast(new WideToast(I18n.get("minegit.clone.success")));
                    if (cloneSuccessCallback != null) {
                        cloneSuccessCallback.run();
                    } else {
                        onClose();
                    }
                } else if (result == 1) {
                    minecraft.getToasts().addToast(new WideToast(I18n.get("minegit.clone.error.invalid_remote")));
                    minecraft.setScreen(this);
                    updateButtonsStatus();
                } else {
                    minecraft.getToasts().addToast(new WideToast(I18n.get("minegit.clone.error.generic")));
                    minecraft.setScreen(this);
                    updateButtonsStatus();
                }
            });
        }).start();
    }

    private void updateButtonsStatus() {
        cloneButton.active = !repoEdit.getValue().replace(" ", "").isEmpty();
    }

    @Override
    public void onClose() {
        closeCallback.run();
    }
}
