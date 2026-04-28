package ca.modmonster.minegit.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ca.modmonster.minegit.backport.RalspinWidget;
import ca.modmonster.minegit.data.GitManager;

public class CloneScreen extends Screen {
    private static final Component REPO_LABEL = Component.translatable("minegit.clone.repo");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 20 + 4, 60);

    private final Screen parent;
    private final Runnable closeCallback;
    private final Runnable cloneSuccessCallback;
    private EditBox repoEdit;
    private Button testCredentialsButton;
    private RalspinWidget ralspinWidget;
    private Button configureButton;

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
        // Column layout
        GridLayout columnLayout = this.layout.addToContents(new GridLayout().spacing(8));
        columnLayout.defaultCellSetting().alignHorizontallyCenter();

        // Menu title
        layout.addToHeader(new StringWidget(this.title, this.font));

        // Repo name text field
        StringWidget usernameEditLabel = columnLayout.addChild(new StringWidget(REPO_LABEL, font), 0, 0);
        usernameEditLabel.setAlpha(0.5f);
        repoEdit = new EditBox(font, 0, 0, 200, 20, REPO_LABEL);
        repoEdit.setMaxLength(39);
        repoEdit.setResponder(string -> updateButtonsStatus());
        columnLayout.addChild(repoEdit, 1, 0);

        // Clone button
        testCredentialsButton = Button.builder(Component.translatable("minegit.clone.confirm"), button -> doClone()).size(200, 20).build();
        columnLayout.addChild(testCredentialsButton, 2, 0);

        // Add layout widgets
        this.layout.visitWidgets(this::addRenderableWidget);

        // Back button
        Button backButton = Button.builder(Component.literal("←"), button -> onClose())
                .tooltip(Tooltip.create(Component.translatable("minegit.clone.back")))
                .bounds(6, 6, 20, 20)
                .build();
        addRenderableWidget(backButton);

        // Configure button
        configureButton = Button.builder(Component.literal("☁"), button -> minecraft.setScreen(new AccountLinkScreen(this)))
                .tooltip(Tooltip.create(Component.translatable("minegit.link.setup.open")))
                .bounds(6, width - 26, 20, 20)
                .build();
        addRenderableWidget(configureButton);

        // Ralsei go spinny
        ralspinWidget = new RalspinWidget(width - 60, height - 80);
        addRenderableWidget(ralspinWidget);

        updateButtonsStatus();
        repositionElements();
        setInitialFocus(repoEdit);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(poseStack);
        super.render(poseStack, i, j, f);
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
                    repositionElements();
                    updateButtonsStatus();
                } else {
                    SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("minegit.clone.error.generic"), null);
                    minecraft.setScreen(this);
                    repositionElements();
                    updateButtonsStatus();
                }
            });
        }).start();
    }

    private void updateButtonsStatus() {
        testCredentialsButton.active = !repoEdit.getValue().isBlank();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
        if (closeCallback != null) closeCallback.run();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        ralspinWidget.setPosition(width - 60, height - 80);
        configureButton.setPosition(width - 26, 6);
    }
}
