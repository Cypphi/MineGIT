package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.GitConflictScreen;
import ca.modmonster.minegit.gui.GitProgressScreen;
import ca.modmonster.minegit.gui.TwoChoiceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.I18n;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldStorage;
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
    public abstract WorldStorage getStorage();

    @Inject(method = "forceSave", at = @At("TAIL"))
    public void onWorldSave(ProgressListener progressListener, CallbackInfo ci) {
        String levelId = getStorage().getName();
        Minecraft minecraft = MinecraftAccessor.getInstance();
        if (minecraft == null) return;

        if (QuitState.altQuit) return;
        if (!GitManager.syncEnabled(minecraft, levelId)) return;
        MineGIT.LOGGER.info("Pushing current world to GitHub");

        doWorldSave(minecraft, GitManager.getPath(minecraft, levelId));
    }

    @Unique
    private void doWorldSave(Minecraft minecraft, Path worldFolder) {
        GitProgressScreen progressScreen = new GitProgressScreen(I18n.translate("minegit.sync.status.git_push"));
        minecraft.openScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.push(worldFolder, progressScreen);
            switch (status) {
                case SUCCESS:
                    // Success; quit as normal
                    MineGIT.LOGGER.info("SUCCESS!!");
                    minecraft.execute(() -> minecraft.openScreen(null));
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    minecraft.execute(() -> minecraft.openScreen(new GitConflictScreen(
                            () -> minecraft.openScreen(null),
                            null,
                            worldFolder
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    minecraft.execute(() -> minecraft.openScreen(new TwoChoiceScreen(
                            I18n.translate("minegit.sync.push_unreachable.title"),
                            I18n.translate("minegit.sync.push_unreachable.description"),
                            I18n.translate("minegit.sync.push_unreachable.retry"),
                            I18n.translate("minegit.sync.push_unreachable.exit"),
                            () -> minecraft.execute(() -> doWorldSave(minecraft, worldFolder)),
                            () -> minecraft.openScreen(null)
                    )));
                    break;
            }
        }).start();
    }
}
