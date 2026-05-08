package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldStorage;
import net.ornithemc.osl.executors.api.MainThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MainThreadExecutor {
    @Shadow
    public int width;

    @Shadow
    public abstract void openScreen(Screen screen);

    @Shadow
    public abstract void startGame(String saveName, String name, long seed);

    @Unique
    private String prevSaveId = null;

    @Inject(method = "openScreen", at = @At("HEAD"), cancellable = true)
    private void onOpenScreen(Screen screen, CallbackInfo ci) {
        if (!(screen instanceof TitleScreen)) return;
        if (prevSaveId == null) return;

        // For some reason IntelliJ thinks this is always false. It is confused.
        if (!QuitState.altQuit && GitManager.syncEnabled(prevSaveId)) {
            ci.cancel();
        }
        prevSaveId = null;
    }

    @Inject(method = "setWorld(Lnet/minecraft/world/World;Ljava/lang/String;Lnet/minecraft/entity/mob/player/PlayerEntity;)V", at = @At("HEAD"))
    private void onSetWorld(World world, String message, PlayerEntity player, CallbackInfo ci) {
        if (world == null) return;
        WorldStorage worldStorage = ((WorldAccessor) world).getStorage();
        if (!(worldStorage instanceof AlphaWorldStorageAccessor)) return;
        Path path = ((AlphaWorldStorageAccessor) worldStorage).getDir().toPath();
        prevSaveId = path.getFileName().toString();
    }
}
