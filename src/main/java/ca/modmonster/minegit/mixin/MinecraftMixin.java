package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.MainThreadTasks;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import net.minecraft.class_52;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    public abstract void setScreen(Screen screen);

    @Unique
    private String prevSaveId = null;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onsetScreen(Screen screen, CallbackInfo ci) {
        if (!(screen instanceof TitleScreen)) return;
        if (prevSaveId == null) return;

        // For some reason IntelliJ thinks this is always false. It is confused.
        if (!QuitState.altQuit && GitManager.syncEnabled(prevSaveId)) {
            ci.cancel();
        }
        prevSaveId = null;
    }

    @Inject(method = "method_2115", at = @At("HEAD"))
    private void onSetWorld(World world, String message, PlayerEntity player, CallbackInfo ci) {
        if (world == null) return;
        class_52 worldStorage = ((WorldAccessor) world).getStorage();
        if (!(worldStorage instanceof AlphaWorldStorageAccessor)) return;
        Path path = ((AlphaWorldStorageAccessor) worldStorage).getDir().toPath();
        prevSaveId = path.getFileName().toString();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        Runnable task;
        while ((task = MainThreadTasks.TASKS.poll()) != null) {
            task.run();
        }
    }
}
