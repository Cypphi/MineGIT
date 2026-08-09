package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.backport.MinecraftExecutor;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftExecutor {
    @Shadow
    private @Nullable IntegratedServer server;

    @Shadow
    public abstract void openScreen(@Nullable Screen screen);

    @Shadow
    public int width;
    @Unique
    private String prevSaveId = null;

    @Unique
    private final Queue<Runnable> minegit$tasks = new ConcurrentLinkedQueue<>();

    @Override
    public void minegit$execute(Runnable task) {
        minegit$tasks.add(task);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void minegit$runTasks(CallbackInfo ci) {
        Runnable task;
        while ((task = minegit$tasks.poll()) != null) {
            try {
                task.run();
            } catch (Throwable throwable) {
                MineGIT.LOGGER.error("Error while executing a client task", throwable);
            }
        }
    }

    @Inject(method = "openScreen", at = @At("HEAD"), cancellable = true)
    private void onOpenScreen(Screen screen, CallbackInfo ci) {
        if (!(screen instanceof TitleScreen)) return;
        if (prevSaveId == null) return;

        // For some reason IntelliJ thinks this is always false. It is confused.
        if (!QuitState.altQuit && GitManager.syncEnabled((Minecraft) (Object) this, prevSaveId)) {
            // Show the generic dirt screen instead
            openScreen(new Screen() {
                @Override
                public void render(int i, int j, float f) {
                    drawBackgroundTexture(i);
                }
            });
            ci.cancel();
        }
        prevSaveId = null;
    }

    @Inject(method = "setWorld(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V", at = @At("HEAD"))
    private void onSetWorld(ClientWorld world, String message, CallbackInfo ci) {
        if (server == null) return;
        prevSaveId = server.getWorldSaveName();
    }
}
