package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.MultiLineLabel;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import org.eclipse.jgit.lib.ProgressMonitor;

public class PruneWorldScreen extends Screen {
    private final Screen parent;
    private final String levelId;
    private final BooleanConsumer callback;

    private MultiLineLabel descriptionWidget;

    public PruneWorldScreen(Screen parent, String levelId, BooleanConsumer callback) {
        super(new TranslatableComponent("minegit.prune.title"));
        this.parent = parent;
        this.levelId = levelId;
        this.callback = callback;
    }

    @Override
    protected void init() {
        // Confirmation message
        descriptionWidget = MultiLineLabel.create(this.font, I18n.get("minegit.prune.description"), this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Confirm button
        Button confirmButton = new Button(this.width / 2 - 152, 98 + descriptionHeight, 150, 20, I18n.get("minegit.prune.confirm"), button -> pullThenPrune());
        addButton(confirmButton);

        // Cancel button
        Button cancelButton = new Button(this.width / 2 + 2, 98 + descriptionHeight, 150, 20, I18n.get("minegit.prune.cancel"), button -> onClose());
        addButton(cancelButton);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(i, j, f);
        drawCenteredString(this.font, this.title.getString(), this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(this.width / 2, 90);
    }

    private void doPrune(ProgressMonitor progress) {
        boolean ok = GitManager.prune(minecraft, levelId, progress);
        if (minecraft == null) return;
        if (ok) {
            minecraft.getToasts().addToast(new WideToast(I18n.get("minegit.prune.complete")));
        } else {
            minecraft.getToasts().addToast(new WideToast(I18n.get("minegit.prune.failed")));
        }
        minecraft.submit(() -> this.callback.accept(true));
    }

    private void pullThenPrune() {
        GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableComponent("minegit.prune.in_progress"));
        minecraft.setScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.pull(GitManager.getPath(minecraft, levelId), progressScreen);
            GitManager.makeWritable(minecraft, levelId);
            switch (status) {
                case SUCCESS:
                    // Success; load world as normal
                    doPrune(progressScreen);
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    minecraft.submit(() -> minecraft.setScreen(new GitConflictScreen(
                            () -> doPrune(progressScreen),
                            this::onClose,
                            GitManager.getPath(minecraft, levelId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    minecraft.submit(() -> minecraft.setScreen(new TwoChoiceScreen(
                            new TranslatableComponent("minegit.sync.pull_unreachable.title"),
                            I18n.get("minegit.sync.pull_unreachable.description"),
                            I18n.get("minegit.sync.pull_unreachable.continue"),
                            I18n.get("minegit.sync.pull_unreachable.cancel"),
                            () -> doPrune(progressScreen),
                            this::onClose
                    )));
                    break;
            }
        }).start();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
