package ca.modmonster.minegit.backport;

import net.minecraft.client.Minecraft;

public interface MinecraftExecutor {
    static void execute(Minecraft minecraft, Runnable task) {
        ((MinecraftExecutor) (Object) minecraft).minegit$execute(task);
    }

    void minegit$execute(Runnable task);
}
