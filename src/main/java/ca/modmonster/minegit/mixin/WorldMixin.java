package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.backport.MainThreadTasks;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.GitConflictScreen;
import ca.modmonster.minegit.gui.GitProgressScreen;
import ca.modmonster.minegit.gui.TwoChoiceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.world.ProgressListener;
import net.minecraft.core.world.World;
import net.minecraft.core.world.save.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(value = World.class, remap = false)
public abstract class WorldMixin {
    @Shadow
    public LevelStorage saveHandler;

    @Inject(method = "saveWorldIndirectly", at = @At("TAIL"))
    public void onWorldSave(ProgressListener iprogressupdate, CallbackInfo ci) {
        Minecraft minecraft = MinecraftAccessor.getInstance();
        if (minecraft == null || !minecraft.running) return;
        if (!(saveHandler instanceof AlphaWorldStorageAccessor)) return;
        Path path = ((AlphaWorldStorageAccessor) saveHandler).getDir().toPath();

        if (QuitState.altQuit) return;
        if (!GitManager.syncEnabled(path)) return;
        MineGIT.LOGGER.info("Pushing current world to GitHub");

        doWorldSave(minecraft, path);
    }

    @Unique
    private void doWorldSave(Minecraft minecraft, Path worldFolder) {
        GitProgressScreen progressScreen = new GitProgressScreen("Pushing to GitHub...");
        minecraft.displayScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.push(worldFolder, progressScreen);
            switch (status) {
                case SUCCESS:
                    // Success; quit as normal
                    MainThreadTasks.execute(() -> minecraft.displayScreen(null));
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    MainThreadTasks.execute(() -> minecraft.displayScreen(new GitConflictScreen(
                            () -> minecraft.displayScreen(null),
                            null,
                            worldFolder
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    MainThreadTasks.execute(() -> minecraft.displayScreen(new TwoChoiceScreen(
                            "Error syncing world",
                            "Your latest world changes could not be synced with the cloud. Make sure you're not offline and then try again.",
                            "Retry sync",
                            "Exit without syncing",
                            () -> MainThreadTasks.execute(() -> doWorldSave(minecraft, worldFolder)),
                            () -> minecraft.displayScreen(null)
                    )));
                    break;
            }
        }).start();
    }
}
