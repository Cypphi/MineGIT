package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.MainThreadTasks;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ScreenMainMenu;
import net.minecraft.client.world.WorldClient;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.save.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftMixin {
    @Unique
    private String prevSaveId = null;

    @Inject(method = "displayScreen", at = @At("HEAD"), cancellable = true)
    private void ondisplayScreen(Screen screen, CallbackInfo ci) {
        if (!(screen instanceof ScreenMainMenu)) return;
        if (prevSaveId == null) return;

        // For some reason IntelliJ thinks this is always false. It is confused.
        if (!QuitState.altQuit && GitManager.syncEnabled(prevSaveId)) {
            ci.cancel();
        }
        prevSaveId = null;
    }

    @Inject(method = "changeWorld(Lnet/minecraft/client/world/WorldClient;Ljava/lang/String;Lnet/minecraft/core/entity/player/Player;)V", at = @At("HEAD"))
    private void onSetWorld(WorldClient world, String loadingTitle, Player player, CallbackInfo ci) {
        if (world == null) return;
        LevelStorage worldStorage = ((WorldAccessor) world).getStorage();
        if (!(worldStorage instanceof AlphaWorldStorageAccessor)) return;
        Path path = ((AlphaWorldStorageAccessor) worldStorage).getDir().toPath();
        prevSaveId = path.getFileName().toString();
    }

    @Inject(method = "runTick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        Runnable task;
        while ((task = MainThreadTasks.TASKS.poll()) != null) {
            task.run();
        }
    }
}
