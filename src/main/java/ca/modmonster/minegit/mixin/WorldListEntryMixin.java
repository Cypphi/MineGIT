package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.GitConflictScreen;
import ca.modmonster.minegit.gui.GitProgressScreen;
import ca.modmonster.minegit.gui.TwoChoiceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldSelectionList;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.storage.WorldSaveInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSelectionList.C_13896933.class)
public abstract class WorldListEntryMixin {
    @Shadow
    @Final
    private Minecraft f_11839034;

    @Shadow
    @Final
    private WorldSaveInfo f_65569109;

    @Shadow
    @Final
    private SelectWorldScreen f_80852037;

    @Shadow
    public abstract void m_29356828();

    @Unique
    private boolean showGitBeforeJoin = true;

    @Inject(method = "m_23399258", at = @At("HEAD"))
    private void beforeWorldDelete(CallbackInfo ci) {
        // Make .git folder writable
        GitManager.makeWritable(f_11839034, f_65569109.getName());
    }

    @Inject(method = "m_29356828", at = @At("HEAD"), cancellable = true)
    private void beforeWorldJoin(CallbackInfo ci) {
        if (!showGitBeforeJoin) return;
        String worldId = f_65569109.getName();
        if (!GitManager.syncEnabled(f_11839034, worldId)) return;
        ci.cancel();
        GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableText("minegit.sync.status.git_pull"));
        f_11839034.openScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.pull(GitManager.getPath(f_11839034, worldId), progressScreen);
            GitManager.makeWritable(f_11839034, worldId);
            switch (status) {
                case SUCCESS:
                    // Success; load world as normal
                    doLoadWorld();
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    f_11839034.execute(() -> f_11839034.openScreen(new GitConflictScreen(
                            this::doLoadWorld,
                            this::returnToScreen,
                            GitManager.getPath(f_11839034, worldId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    f_11839034.execute(() -> f_11839034.openScreen(new TwoChoiceScreen(
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
        f_11839034.execute(() -> {
            showGitBeforeJoin = false;
            m_29356828();
            showGitBeforeJoin = true;
        });
    }

    @Unique
    private void returnToScreen() {
        WorldSelectionList list = ((SelectWorldScreenAccessor) this).getLevelList();
        ((WorldSelectionListInvoker) list).invokeReloadWorldList(() -> ((SelectWorldScreenAccessor) this.f_80852037).getEditBox().getText(), true);
        f_11839034.openScreen(f_80852037);
    }
}
