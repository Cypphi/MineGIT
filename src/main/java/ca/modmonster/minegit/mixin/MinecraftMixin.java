package ca.modmonster.minegit.mixin;

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

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    private @Nullable IntegratedServer server;

    @Shadow
    public abstract void openScreen(@Nullable Screen screen);

    @Shadow
    public int width;
    @Unique
    private String prevSaveId = null;

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
