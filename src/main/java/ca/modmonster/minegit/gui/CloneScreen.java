package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.data.GitManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CloneScreen extends Screen {
    private static final Component REPO_LABEL = Component.translatable("minegit.clone.repo");
    private static final Component BACK_BUTTON_TOOLTIP = Component.translatable("minegit.clone.back");
    private static final Component CONFIGURE_BUTTON_TOOLTIP = Component.translatable("minegit.link.setup.open");

    private final Screen parent;
    private final Runnable closeCallback;
    private final Runnable cloneSuccessCallback;
    private EditBox repoEdit;
    private Button cloneButton;
    private Button backButton;
    private Button configureButton;
    private RalspinWidget ralspinWidget;

    public CloneScreen(Screen parent, Runnable closeCallback) {
        this(parent, closeCallback, null);
    }

    public CloneScreen(Screen parent, Runnable closeCallback, Runnable cloneSuccessCallback) {
        super(Component.translatable("minegit.clone.title"));
        this.parent = parent;
        this.closeCallback = closeCallback;
        this.cloneSuccessCallback = cloneSuccessCallback;
    }

    @Override
    protected void init() {
        // Repo name text field
        repoEdit = new EditBox(font, this.width / 2 - 100, 107, 200, 20, REPO_LABEL);
        repoEdit.setMaxLength(39);
        repoEdit.setResponder(string -> updateButtonsStatus());
        addRenderableWidget(repoEdit);

        // Clone button
        cloneButton = new Button(this.width / 2 - 100, 135, 200, 20, Component.translatable("minegit.clone.confirm"), button -> doClone());
        addRenderableWidget(cloneButton);

        // Back button
        backButton = new Button(6, 6, 20, 20, Component.literal("←"), button -> onClose());
        addRenderableWidget(backButton);

        // Configure button
        configureButton = new Button(width - 26, 6, 20, 20, Component.literal("☁"), button -> minecraft.setScreen(new AccountLinkScreen(this)));
        addRenderableWidget(configureButton);

        // Ralsei go spinny
        ralspinWidget = new RalspinWidget(width - 60, height - 80);
        addRenderableWidget(ralspinWidget);

        updateButtonsStatus();
        setInitialFocus(repoEdit);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 16777215);
        drawCenteredString(poseStack, this.font, REPO_LABEL, this.width / 2, 90, -2130706433);
        if (backButton.isHoveredOrFocused()) renderTooltip(poseStack, BACK_BUTTON_TOOLTIP, i, j);
        if (configureButton.isHoveredOrFocused()) renderTooltip(poseStack, CONFIGURE_BUTTON_TOOLTIP, i, j);
        if (ralspinWidget.isHoveredOrFocused()) renderTooltip(poseStack, RalspinWidget.TOOLTIP, i, j);
    }

    private void doClone() {
        GitProgressScreen progressScreen = new GitProgressScreen(Component.translatable("minegit.clone.in_progress"));
        minecraft.setScreen(progressScreen);
        new Thread(() -> {
            int result = GitManager.cloneRepo(minecraft, repoEdit.getValue(), progressScreen);

            minecraft.submit(() -> {
                if (result == 0) {
                    SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("minegit.clone.success"), null);
                    if (cloneSuccessCallback != null) {
                        cloneSuccessCallback.run();
                    } else {
                        onClose();
                    }
                } else if (result == 1) {
                    SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("minegit.clone.error.invalid_remote"), null);
                    minecraft.setScreen(this);
                    updateButtonsStatus();
                } else {
                    SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("minegit.clone.error.generic"), null);
                    minecraft.setScreen(this);
                    updateButtonsStatus();
                }
            });
        }).start();
    }

    private void updateButtonsStatus() {
        cloneButton.active = !repoEdit.getValue().isBlank();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }
}
