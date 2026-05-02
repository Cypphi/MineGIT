package ca.modmonster.minegit.event;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.GitConflictScreen;
import ca.modmonster.minegit.gui.GitProgressScreen;
import ca.modmonster.minegit.gui.TwoChoiceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

import java.nio.file.Path;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class LevelSaveEvents {
    @SubscribeEvent
    public static void onServerStop(FMLServerStoppedEvent event) {
        if (QuitState.altQuit) {
            QuitState.altQuit = false;
            return;
        }

        String levelId = event.getServer().getFolderName();
        Minecraft minecraft = Minecraft.getInstance();
        if (!GitManager.syncEnabled(minecraft, levelId)) return;
        MineGIT.LOGGER.info("Pushing current world to GitHub");

        doWorldSave(minecraft, GitManager.getPath(minecraft, levelId));
    }

    private static void doWorldSave(Minecraft minecraft, Path worldFolder) {
        GitProgressScreen progressScreen = new GitProgressScreen(I18n.format("minegit.sync.status.git_push"));
        minecraft.displayGuiScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.push(worldFolder, progressScreen);
            switch (status) {
                case SUCCESS:
                    // Success; quit as normal
                    minecraft.addScheduledTask(() -> minecraft.displayGuiScreen(null));
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    minecraft.addScheduledTask(() -> minecraft.displayGuiScreen(new GitConflictScreen(
                            () -> minecraft.displayGuiScreen(null),
                            null,
                            worldFolder
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    minecraft.addScheduledTask(() -> minecraft.displayGuiScreen(new TwoChoiceScreen(
                            I18n.format("minegit.sync.push_unreachable.title"),
                            I18n.format("minegit.sync.push_unreachable.description"),
                            I18n.format("minegit.sync.push_unreachable.retry"),
                            I18n.format("minegit.sync.push_unreachable.exit"),
                            () -> minecraft.addScheduledTask(() -> doWorldSave(minecraft, worldFolder)),
                            () -> minecraft.displayGuiScreen(null)
                    )));
                    break;
            }
        }).start();
    }
}
