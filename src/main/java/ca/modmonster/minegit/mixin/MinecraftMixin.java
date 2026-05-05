package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    private IntegratedServer integratedServer;

    @Shadow
    public abstract void displayGuiScreen(@Nullable GuiScreen guiScreenIn);

    @Unique
    private String prevSaveId = null;

    @Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
    private void onOpenScreen(GuiScreen screen, CallbackInfo ci) {
        if (!(screen instanceof GuiMainMenu)) return;
        if (prevSaveId == null) return;

        // For some reason IntelliJ thinks this is always false. It is confused.
        if (!QuitState.altQuit && GitManager.syncEnabled((Minecraft) (Object) this, prevSaveId)) {
            // Show the generic dirt screen instead
            displayGuiScreen(new GuiScreen() {
                @Override
                public void drawScreen(int mouseX, int mouseY, float partialTicks) {
                    drawDefaultBackground();
                }
            });
            ci.cancel();
        }
        prevSaveId = null;
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void onLoadWorld(WorldClient worldClientIn, String loadingMessage, CallbackInfo ci) {
        if (integratedServer == null) return;
        prevSaveId = integratedServer.getFolderName();
    }
}
