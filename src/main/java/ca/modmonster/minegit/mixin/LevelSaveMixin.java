package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.GitConflictScreen;
import ca.modmonster.minegit.gui.GitProgressScreen;
import ca.modmonster.minegit.gui.TwoChoiceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(IntegratedServer.class)
public class LevelSaveMixin {
    @Shadow
    @Final
    private Minecraft mc;

    @Inject(method = "stopServer", at = @At("TAIL"))
    private void onWorldSaved(CallbackInfo ci) {
        if (QuitState.altQuit) {
            QuitState.altQuit = false;
            return;
        }

        MinecraftServer server = (MinecraftServer) (Object) this;
        String levelId = server.getFolderName();
        if (!GitManager.syncEnabled(mc, levelId)) return;
        MineGIT.LOGGER.info("Pushing current world to GitHub");

        mineGIT$doWorldSave(GitManager.getPath(mc, levelId));
    }

    @Unique
    private void mineGIT$doWorldSave(Path worldFolder) {
        mc.addScheduledTask(() -> {
            GitProgressScreen progressScreen = new GitProgressScreen(I18n.format("minegit.sync.status.git_push"));
            mc.displayGuiScreen(progressScreen);
            new Thread(() -> {
                SyncResult status = GitManager.push(worldFolder, progressScreen);
                switch (status) {
                    case SUCCESS:
                        // Success; quit as normal
                        mc.addScheduledTask(() -> mc.displayGuiScreen(null));
                        break;
                    case FAIL_GENERIC:
                        // Generic error; show option to keep local or cloud
                        mc.addScheduledTask(() -> mc.displayGuiScreen(new GitConflictScreen(
                                () -> mc.displayGuiScreen(null),
                                null,
                                worldFolder
                        )));
                        break;
                    case FAIL_NETWORK:
                        // Network error; show unreachable screen
                        mc.addScheduledTask(() -> mc.displayGuiScreen(new TwoChoiceScreen(
                                I18n.format("minegit.sync.push_unreachable.title"),
                                I18n.format("minegit.sync.push_unreachable.description"),
                                I18n.format("minegit.sync.push_unreachable.retry"),
                                I18n.format("minegit.sync.push_unreachable.exit"),
                                () -> mineGIT$doWorldSave(worldFolder),
                                () -> mc.displayGuiScreen(null)
                        )));
                        break;
                }
            }).start();
        });
    }
}
