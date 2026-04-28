package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.GitConflictScreen;
import ca.modmonster.minegit.gui.GitProgressScreen;
import ca.modmonster.minegit.gui.TwoChoiceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldListEntryMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    LevelSummary summary;

    @Shadow
    @Final
    private SelectWorldScreen screen;

    @Inject(method = "doDeleteWorld", at = @At("HEAD"))
    private void beforeWorldDelete(CallbackInfo ci) {
        // Make .git folder writable
        GitManager.makeWritable(minecraft, summary.getLevelId());
    }

    @Inject(method = "joinWorld", at = @At("HEAD"), cancellable = true)
    private void beforeWorldJoin(CallbackInfo ci) {
        String worldId = summary.getLevelId();
        if (!GitManager.syncEnabled(minecraft, worldId)) return;
        ci.cancel();
        GitProgressScreen progressScreen = new GitProgressScreen(Component.translatable("minegit.sync.status.git_pull"));
        minecraft.setScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.pull(GitManager.getPath(minecraft, worldId), progressScreen);
            GitManager.makeWritable(minecraft, worldId);
            switch (status) {
                case SUCCESS:
                    // Success; load world as normal
                    doLoadWorld();
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    minecraft.submit(() -> minecraft.setScreen(new GitConflictScreen(
                            this::doLoadWorld,
                            this::returnToScreen,
                            GitManager.getPath(minecraft, worldId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    minecraft.submit(() -> minecraft.setScreen(new TwoChoiceScreen(
                            Component.translatable("minegit.sync.pull_unreachable.title"),
                            Component.translatable("minegit.sync.pull_unreachable.description"),
                            Component.translatable("minegit.sync.pull_unreachable.continue"),
                            Component.translatable("minegit.sync.pull_unreachable.cancel"),
                            this::doLoadWorld, // continue
                            this::returnToScreen // cancel
                    )));
                    break;
            }
        }).start();
    }

    @Unique
    private void doLoadWorld() {
        minecraft.submit(() -> minecraft.createWorldOpenFlows().checkForBackupAndLoad(summary.getLevelId(), this::returnToScreen));
    }

    @Unique
    private void returnToScreen() {
        // disgusting
        WorldSelectionList list = ((SelectWorldScreenAccessor) screen).getLevelList();
        ((WorldSelectionListInvoker) list).invokeReloadWorldList();
        minecraft.setScreen(screen);
    }
}
