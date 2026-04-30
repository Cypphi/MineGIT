package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.GitManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class CloneScreen extends Screen {
    private static final Component REPO_LABEL = new TranslatableComponent("minegit.clone.repo");
    private static final Component BACK_BUTTON_TOOLTIP = new TranslatableComponent("minegit.clone.back");
    private static final Component CONFIGURE_BUTTON_TOOLTIP = new TranslatableComponent("minegit.link.setup.open");

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
        repoEdit = new EditBox(font, this.width / 2 - 100, 107, 200, 20, REPO_LABEL);
        repoEdit.setMaxLength(39);
        repoEdit.setResponder(string -> updateButtonsStatus());
        this.children.add(repoEdit);

        // Clone button
        cloneButton = new Button(this.width / 2 - 100, 135, 200, 20, new TranslatableComponent("minegit.clone.confirm"), button -> doClone());
        addButton(cloneButton);

        // Back button
        backButton = new Button(6, 6, 20, 20, new TextComponent("←"), button -> onClose());
        addButton(backButton);

        // Configure button
        configureButton = new Button(width - 26, 6, 20, 20, new TextComponent("☁"), button -> minecraft.setScreen(new AccountLinkScreen(this)));
        addButton(configureButton);

        // Ralsei go spinny
        ralspinWidget = new RalspinWidget(width - 60, height - 80);
        this.children.add(ralspinWidget);

        updateButtonsStatus();
        setInitialFocus(repoEdit);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 16777215);
        drawCenteredString(poseStack, this.font, REPO_LABEL, this.width / 2, 90, -2130706433);
        repoEdit.render(poseStack, i, j, f);
        ralspinWidget.render(poseStack, i, j, f);
        if (backButton.isHovered()) renderTooltip(poseStack, BACK_BUTTON_TOOLTIP, i, j);
        if (configureButton.isHovered()) renderTooltip(poseStack, CONFIGURE_BUTTON_TOOLTIP, i, j);
        if (ralspinWidget.isHovered()) renderTooltip(poseStack, RalspinWidget.TOOLTIP, i, j);
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
                    minecraft.getToasts().addToast(WideToast.get(font, new TranslatableComponent("minegit.clone.success")));
                    if (cloneSuccessCallback != null) {
                        cloneSuccessCallback.run();
                    } else {
                        onClose();
                    }
                } else if (result == 1) {
                    minecraft.getToasts().addToast(WideToast.get(font, new TranslatableComponent("minegit.clone.error.invalid_remote")));
                    minecraft.setScreen(this);
                    updateButtonsStatus();
                } else {
                    minecraft.getToasts().addToast(WideToast.get(font, new TranslatableComponent("minegit.clone.error.generic")));
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
        if (closeCallback != null) closeCallback.run();
    }
}
