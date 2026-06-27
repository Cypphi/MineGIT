package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.LevelSaver;
import ca.modmonster.minegit.data.QuitState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

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
        Path worldFolder = server.getWorldPath(LevelResource.ROOT); // get world folder
        if (!GitManager.syncEnabled(worldFolder)) return;
        MineGIT.LOGGER.info("Pushing current world to remote");

        LevelSaver.doWorldSave(minecraft, worldFolder, () -> minecraft.setScreen(null));
    }
}
