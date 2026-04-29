package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.data.GitManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CloneScreen extends Screen {
    private static final Component REPO_LABEL = Component.translatable("minegit.clone.repo");

    private final Screen parent;
    private final Runnable closeCallback;
    private final Runnable cloneSuccessCallback;
    private EditBox repoEdit;
    private Button cloneButton;

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
        repoEdit = new EditBox(font, 0, 0, 200, 20, REPO_LABEL);
        repoEdit.setMaxLength(39);
        repoEdit.setResponder(string -> updateButtonsStatus());
        repoEdit.setPosition(this.width / 2 - 100, 107);
        addRenderableWidget(repoEdit);

        // Clone button
        cloneButton = Button.builder(Component.translatable("minegit.clone.confirm"), button -> doClone()).size(200, 20).build();
        cloneButton.setPosition(this.width / 2 - 100, 135);
        addRenderableWidget(cloneButton);

        // Back button
        Button backButton = Button.builder(Component.literal("←"), button -> onClose())
                .tooltip(Tooltip.create(Component.translatable("minegit.clone.back")))
                .bounds(6, 6, 20, 20)
                .build();
        addRenderableWidget(backButton);

        // Configure button
        Button configureButton = Button.builder(Component.literal("☁"), button -> minecraft.setScreen(new AccountLinkScreen(this)))
                .tooltip(Tooltip.create(Component.translatable("minegit.link.setup.open")))
                .bounds(width - 26, 6, 20, 20)
                .build();
        addRenderableWidget(configureButton);

        // Ralsei go spinny
        RalspinWidget ralspinWidget = new RalspinWidget(width - 60, height - 80);
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
