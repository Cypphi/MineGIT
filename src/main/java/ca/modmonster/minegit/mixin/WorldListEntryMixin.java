package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.GitConflictScreen;
import ca.modmonster.minegit.gui.GitProgressScreen;
import ca.modmonster.minegit.gui.TwoChoiceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListWorldSelection;
import net.minecraft.client.gui.GuiListWorldSelectionEntry;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.storage.WorldSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiListWorldSelectionEntry.class)
public abstract class WorldListEntryMixin {
    @Shadow
    @Final
    private Minecraft client;

    @Shadow
    @Final
    private WorldSummary worldSummary;

    @Shadow
    @Final
    private GuiWorldSelection worldSelScreen;

    @Shadow
    public abstract void joinWorld();

    @Unique
    private boolean mineGIT$showGitBeforeJoin = true;

    @Inject(method = "deleteWorld", at = @At("HEAD"))
    private void beforeWorldDelete(CallbackInfo ci) {
        // Make .git folder writable
        GitManager.makeWritable(client, worldSummary.getFileName());
    }

    @Inject(method = "joinWorld", at = @At("HEAD"), cancellable = true)
    private void beforeWorldJoin(CallbackInfo ci) {
        if (!mineGIT$showGitBeforeJoin) return;
        String worldId = worldSummary.getFileName();
        if (!GitManager.syncEnabled(client, worldId)) return;
        ci.cancel();
        GitProgressScreen progressScreen = new GitProgressScreen(I18n.format("minegit.sync.status.git_pull"));
        client.displayGuiScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.pull(GitManager.getPath(client, worldId), progressScreen);
            GitManager.makeWritable(client, worldId);
            switch (status) {
                case SUCCESS:
                    // Success; load world as normal
                    mineGIT$doLoadWorld();
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    client.addScheduledTask(() -> client.displayGuiScreen(new GitConflictScreen(
                            this::mineGIT$doLoadWorld,
                            this::mineGIT$returnToScreen,
                            GitManager.getPath(client, worldId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    client.addScheduledTask(() -> client.displayGuiScreen(new TwoChoiceScreen(
                            I18n.format("minegit.sync.pull_unreachable.title"),
                            I18n.format("minegit.sync.pull_unreachable.description"),
                            I18n.format("minegit.sync.pull_unreachable.continue"),
                            I18n.format("minegit.sync.pull_unreachable.cancel"),
                            this::mineGIT$doLoadWorld, // continue
                            this::mineGIT$returnToScreen // cancel
                    )));
                    break;
            }
        }).start();
    }

    @Unique
    private void mineGIT$doLoadWorld() {
        client.addScheduledTask(() -> {
            mineGIT$showGitBeforeJoin = false;
            joinWorld();
            mineGIT$showGitBeforeJoin = true;
        });
    }

    @Unique
    private void mineGIT$returnToScreen() {
        GuiListWorldSelection list = ((SelectWorldScreenAccessor) this).getLevelList();
        ((WorldSelectionListInvoker) list).invokeReloadWorldList(() -> ((SelectWorldScreenAccessor) this.worldSelScreen).getEditBox().getText(), true);
        client.displayGuiScreen(worldSelScreen);
    }
}
