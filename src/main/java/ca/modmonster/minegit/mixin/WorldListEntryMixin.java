package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.GitConflictScreen;
import ca.modmonster.minegit.gui.GitProgressScreen;
import ca.modmonster.minegit.gui.TwoChoiceScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldListWidget.Entry.class)
public abstract class WorldListEntryMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private LevelSummary level;

    @Shadow
    @Final
    private SelectWorldScreen screen;

    @Shadow
    public abstract void play();

    @Unique
    private boolean showGitBeforeJoin = true;

    @Inject(method = "delete", at = @At("HEAD"))
    private void beforeWorldDelete(CallbackInfo ci) {
        // Make .git folder writable
        GitManager.makeWritable(client, level.getName());
    }

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void beforeWorldJoin(CallbackInfo ci) {
        if (!showGitBeforeJoin) return;
        String worldId = level.getName();
        if (!GitManager.syncEnabled(client, worldId)) return;
        ci.cancel();
        GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableText("minegit.sync.status.git_pull"));
        client.openScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.pull(GitManager.getPath(client, worldId), progressScreen);
            GitManager.makeWritable(client, worldId);
            switch (status) {
                case SUCCESS:
                    // Success; load world as normal
                    doLoadWorld();
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    client.execute(() -> client.openScreen(new GitConflictScreen(
                            this::doLoadWorld,
                            this::returnToScreen,
                            GitManager.getPath(client, worldId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    client.execute(() -> client.openScreen(new TwoChoiceScreen(
                            new TranslatableText("minegit.sync.pull_unreachable.title"),
                            I18n.translate("minegit.sync.pull_unreachable.description"),
                            I18n.translate("minegit.sync.pull_unreachable.continue"),
                            I18n.translate("minegit.sync.pull_unreachable.cancel"),
                            this::doLoadWorld, // continue
                            this::returnToScreen // cancel
                    )));
                    break;
            }
        }).start();
    }

    @Unique
    private void doLoadWorld() {
        client.execute(() -> {
            showGitBeforeJoin = false;
            play();
            showGitBeforeJoin = true;
        });
    }

    @Unique
    private void returnToScreen() {
        WorldListWidget list = ((SelectWorldScreenAccessor) this).getLevelList();
        ((WorldSelectionListInvoker) list).invokeReloadWorldList(() -> ((SelectWorldScreenAccessor) this.screen).getEditBox().getText(), true);
        client.openScreen(screen);
    }
}
