package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.data.GitManager;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.HashMap;
import java.util.Map;

public class RevertScreen extends Screen {
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 20 + 4, 60);

    private final Screen parent;
    private final LevelStorageSource.LevelStorageAccess levelAccess;
    private final BooleanConsumer callback;

    private ScrollableLayout scrollable;
    private MultiLineTextWidget descriptionWidget;
    private final Map<RevCommit, Button> buttons = new HashMap<>();
    private RevCommit selectedCommit = null;
    private Button revertButton;

    public RevertScreen(Screen parent, LevelStorageSource.LevelStorageAccess levelAccess, BooleanConsumer callback) {
        super(Component.translatable("minegit.revert.title"));
        this.parent = parent;
        this.levelAccess = levelAccess;
        this.callback = callback;
    }

    @Override
    protected void init() {
        // Column layout
        LinearLayout columnLayout = this.layout.addToContents(LinearLayout.vertical().spacing(8));
        columnLayout.defaultCellSetting().alignHorizontallyCenter();

        // Menu title
        layout.addTitleHeader(this.title, this.font);

        // Confirmation message
        descriptionWidget = new MultiLineTextWidget(Component.translatable("minegit.revert.description"), this.font).setMaxWidth(this.width - 50);
        columnLayout.addChild(descriptionWidget);
        columnLayout.addChild(new SpacerElement(200, 8));

        // Commits scroll view
        LinearLayout commitsColumn = new LinearLayout(0, 0, LinearLayout.Orientation.VERTICAL);

        Iterable<RevCommit> commits = GitManager.listCommits(minecraft, levelAccess.getLevelId());
        for (RevCommit commit : commits) {
            Button button = Button.builder(Component.literal(commit.getShortMessage()), (b) -> {
                if (selectedCommit != null) buttons.get(selectedCommit).active = true; // re-enable old button
                selectedCommit = commit;
                b.active = false;
                revertButton.active = true;
            }).width(240).build();
            commitsColumn.addChild(button);
            buttons.put(commit, button);
        }

        scrollable = new ScrollableLayout(minecraft, commitsColumn, this.height - layout.getHeaderHeight() - layout.getFooterHeight() - descriptionWidget.getHeight() - 16);
        columnLayout.addChild(scrollable);

        // Revert button
        LinearLayout buttonRowLayout = layout.addToFooter(LinearLayout.horizontal().spacing(8));
        revertButton = Button.builder(Component.translatable("minegit.revert"), button -> revert()).build();
        revertButton.active = false;
        buttonRowLayout.addChild(revertButton);

        // Cancel button
        Button cancelButton = Button.builder(Component.translatable("gui.cancel"), button -> onClose()).build();
        buttonRowLayout.addChild(cancelButton);

        // Add layout widgets
        this.layout.visitWidgets(this::addRenderableWidget);
        this.layout.arrangeElements();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void revert() {
        levelAccess.safeClose();
        String worldId = levelAccess.getLevelId();
        GitProgressScreen progressScreen = new GitProgressScreen(Component.translatable("minegit.revert.in_progress"));
        minecraft.gui.setScreen(progressScreen);
        new Thread(() -> {
            GitManager.makeWritable(minecraft, worldId);
            GitManager.revert(minecraft, worldId, selectedCommit, progressScreen);
            GitManager.forcePush(GitManager.getPath(minecraft, worldId), progressScreen);
            SystemToast.add(minecraft.gui.toastManager(), new SystemToast.SystemToastId(), Component.translatable("minegit.revert.complete"), null);
            minecraft.submit(() -> callback.accept(true));
        }).start();
    }

    @Override
    protected void repositionElements() {
        if (descriptionWidget != null) descriptionWidget.setMaxWidth(this.width - 50);
        scrollable.setMaxHeight(this.height - layout.getHeaderHeight() - layout.getFooterHeight() - descriptionWidget.getHeight() - 16);
        layout.arrangeElements();
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}