package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.GitConflictScreen;
import ca.modmonster.minegit.gui.GitProgressScreen;
import ca.modmonster.minegit.gui.TwoChoiceScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Environment(EnvType.CLIENT)
@Mixin(IntegratedServer.class)
public class LevelSaveMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "stopServer", at = @At("TAIL"))
    private void onWorldSaved(CallbackInfo ci) {
        if (QuitState.altQuit) {
            QuitState.altQuit = false;
            return;
        }

        MinecraftServer server = (MinecraftServer) (Object) this;
        String levelId = server.getLevelIdName();
        if (!GitManager.syncEnabled(minecraft, levelId)) return;
        MineGIT.LOGGER.info("Pushing current world to GitHub");

        doWorldSave(GitManager.getPath(minecraft, levelId));
    }

    @Unique
    private void doWorldSave(Path worldFolder) {
        minecraft.submit(() -> {
            GitProgressScreen progressScreen = new GitProgressScreen(new TranslatableComponent("minegit.sync.status.git_push"));
            minecraft.setScreen(progressScreen);
            new Thread(() -> {
                SyncResult status = GitManager.push(worldFolder, progressScreen);
                switch (status) {
                    case SUCCESS:
                        // Success; quit as normal
                        minecraft.submit(() -> minecraft.setScreen(null));
                        break;
                    case FAIL_GENERIC:
                        // Generic error; show option to keep local or cloud
                        minecraft.submit(() -> minecraft.setScreen(new GitConflictScreen(
                                () -> minecraft.setScreen(null),
                                null,
                                worldFolder
                        )));
                        break;
                    case FAIL_NETWORK:
                        // Network error; show unreachable screen
                        minecraft.submit(() -> minecraft.setScreen(new TwoChoiceScreen(
                                new TranslatableComponent("minegit.sync.push_unreachable.title"),
                                I18n.get("minegit.sync.push_unreachable.description"),
                                I18n.get("minegit.sync.push_unreachable.retry"),
                                I18n.get("minegit.sync.push_unreachable.exit"),
                                () -> doWorldSave(worldFolder),
                                () -> minecraft.setScreen(null)
                        )));
                        break;
                }
            }).start();
        });
    }
}
