package ca.modmonster.minegit.data;

import ca.modmonster.minegit.gui.GitConflictScreen;
import ca.modmonster.minegit.gui.GitProgressScreen;
import ca.modmonster.minegit.gui.TwoChoiceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public class LevelSaver {
    public static void doWorldSave(Minecraft minecraft, Path worldFolder, Runnable onFinish) {
        minecraft.submit(() -> {
            GitProgressScreen progressScreen = new GitProgressScreen(Component.translatable("minegit.sync.status.git_push", ConfigManager.getCurrentConfig().gitService.getNaturalName()));
            minecraft.gui.setScreen(progressScreen);
            new Thread(() -> {
                SyncResult status = GitManager.push(worldFolder, progressScreen);
                switch (status) {
                    case SUCCESS:
                        // Success; quit as normal
                        minecraft.submit(onFinish);
                        break;
                    case FAIL_GENERIC:
                        // Generic error; show option to keep local or cloud
                        minecraft.submit(() -> minecraft.gui.setScreen(new GitConflictScreen(
                                onFinish,
                                null,
                                worldFolder
                        )));
                        break;
                    case FAIL_NETWORK:
                        // Network error; show unreachable screen
                        minecraft.submit(() -> minecraft.gui.setScreen(new TwoChoiceScreen(
                                Component.translatable("minegit.sync.error"),
                                Component.translatable("minegit.sync.push_unreachable.description"),
                                Component.translatable("minegit.sync.push_unreachable.retry"),
                                Component.translatable("minegit.sync.push_unreachable.exit"),
                                () -> doWorldSave(minecraft, worldFolder, onFinish),
                                onFinish
                        )));
                        break;
                }
            }).start();
        });
    }
}
