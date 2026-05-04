package ca.modmonster.minegit.gui;

import ca.modmonster.minegit.backport.MultiLineLabel;
import ca.modmonster.minegit.backport.WideToast;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import net.minecraft.unmapped.C_01559903;
import org.eclipse.jgit.lib.ProgressMonitor;

public class PruneWorldScreen extends Screen {
    private final Screen parent;
    private final String levelId;
    private final BooleanConsumer callback;

    private MultiLineLabel descriptionWidget;

    public PruneWorldScreen(Screen parent, String levelId, BooleanConsumer callback) {
        super(new TranslatableText("minegit.prune.title"));
        this.parent = parent;
        this.levelId = levelId;
        this.callback = callback;
    }

    @Override
    protected void init() {
        // Confirmation message
        descriptionWidget = MultiLineLabel.create(textRenderer, I18n.translate("minegit.prune.description"), this.width - 50);
        int descriptionHeight = descriptionWidget.getLineCount() * 9;

        // Confirm button
        C_01559903 confirmButton = new C_01559903(this.width / 2 - 152, 98 + descriptionHeight, 150, 20, I18n.translate("minegit.prune.confirm"), button -> pullThenPrune());
        addButton(confirmButton);

        // Cancel button
        C_01559903 cancelButton = new C_01559903(this.width / 2 + 2, 98 + descriptionHeight, 150, 20, I18n.translate("minegit.prune.cancel"), button -> close());
        addButton(cancelButton);
    }

    @Override
    public void render(int i, int j, float f) {
        this.drawBackgroundTexture(i);
        super.render(i, j, f);
        drawCenteredString(textRenderer, this.f_89436361.getString(), this.width / 2, 50, 16777215);
        descriptionWidget.renderCentered(this.width / 2, 90);
    }

    private void doPrune(ProgressMonitor progress) {
        boolean ok = GitManager.prune(minecraft, levelId, progress);
        if (minecraft == null) return;
        if (ok) {
            minecraft.getToasts().add(new WideToast(I18n.translate("minegit.prune.complete")));
        } else {
            minecraft.getToasts().add(new WideToast(I18n.translate("minegit.prune.failed")));
        }
        minecraft.execute(() -> this.callback.accept(true));
    }

    private void pullThenPrune() {
        GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableText("minegit.prune.in_progress"));
        minecraft.openScreen(progressScreen);
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
                    minecraft.execute(() -> minecraft.openScreen(new GitConflictScreen(
                            () -> doPrune(progressScreen),
                            this::close,
                            GitManager.getPath(minecraft, levelId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    minecraft.execute(() -> minecraft.openScreen(new TwoChoiceScreen(
                            new TranslatableText("minegit.sync.pull_unreachable.title"),
                            I18n.translate("minegit.sync.pull_unreachable.description"),
                            I18n.translate("minegit.sync.pull_unreachable.continue"),
                            I18n.translate("minegit.sync.pull_unreachable.cancel"),
                            () -> doPrune(progressScreen),
                            this::close
                    )));
                    break;
            }
        }).start();
    }

    @Override
    public void close() {
        minecraft.openScreen(parent);
    }
}
