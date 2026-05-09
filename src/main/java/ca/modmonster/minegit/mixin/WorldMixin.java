package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.backport.MainThreadTasks;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.GitConflictScreen;
import ca.modmonster.minegit.gui.GitProgressScreen;
import ca.modmonster.minegit.gui.TwoChoiceScreen;
import net.minecraft.class_52;
import net.minecraft.class_62;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow
    @Final
    protected class_52 field_219;

    @Inject(method = "method_280", at = @At("TAIL"))
    public void onWorldSave(class_62 progressListener, CallbackInfo ci) {
        if (!(field_219 instanceof AlphaWorldStorageAccessor)) return;
        Path path = ((AlphaWorldStorageAccessor) field_219).getDir().toPath();
        Minecraft minecraft = MinecraftAccessor.getInstance();
        if (minecraft == null) return;

        if (QuitState.altQuit) return;
        if (!GitManager.syncEnabled(path)) return;
        MineGIT.LOGGER.info("Pushing current world to GitHub");

        doWorldSave(minecraft, path);
    }

    @Unique
    private void doWorldSave(Minecraft minecraft, Path worldFolder) {
        GitProgressScreen progressScreen = new GitProgressScreen("Pushing to GitHub...");
        minecraft.setScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.push(worldFolder, progressScreen);
            switch (status) {
                case SUCCESS:
                    // Success; quit as normal
                    MainThreadTasks.execute(() -> minecraft.setScreen(null));
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    MainThreadTasks.execute(() -> minecraft.setScreen(new GitConflictScreen(
                            () -> minecraft.setScreen(null),
                            null,
                            worldFolder
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    MainThreadTasks.execute(() -> minecraft.setScreen(new TwoChoiceScreen(
                            "Error syncing world",
                            "Your latest world changes could not be synced with the cloud. Make sure you're not offline and then try again.",
                            "Retry sync",
                            "Exit without syncing",
                            () -> MainThreadTasks.execute(() -> doWorldSave(minecraft, worldFolder)),
                            () -> minecraft.setScreen(null)
                    )));
                    break;
            }
        }).start();
    }
}
