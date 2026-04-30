package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.storage.LevelResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {
    @Unique
    private static final Component TOOLTIP = new TranslatableComponent("minegit.exit_without_push");

    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private Button disconnectButton;

    @Inject(at = @At("TAIL"), method = "createPauseMenu", remap = false)
    protected void createPauseMenu(CallbackInfo ci) {
        // Find the disconnect button
        for (GuiEventListener child : this.children()) {
            if (!(child instanceof Button)) return;
            Button button = (Button) child;
            if (!(button.getMessage() instanceof TranslatableComponent)) return;
            TranslatableComponent translatable = (TranslatableComponent) button.getMessage();
            String key = translatable.getKey();
            if (key.equals("menu.returnToMenu")) disconnectButton = button;
        }
    }

    @Inject(at = @At("TAIL"), method = "render", remap = false)
    private void render(final PoseStack poseStack, final int mouseX, final int mouseY, final float a, CallbackInfo info) {
        if (disconnectButton == null) return;
        if (!minecraft.isLocalServer()) return;
        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server == null) return;
        if (!GitManager.syncEnabled(server.getWorldPath(LevelResource.ROOT))) return;
        if (!QuitState.altQuit) return;

        if (disconnectButton.isHovered()) {
            // draw red border
            renderOutline(
                    poseStack,
                    disconnectButton.x,
                    disconnectButton.y,
                    disconnectButton.getWidth(),
                    disconnectButton.getHeight(),
                    -65536
            );

            // draw tooltip
            renderTooltip(poseStack, TOOLTIP, mouseX, mouseY);
        }
    }

    @Unique
    private void renderOutline(final PoseStack poseStack, final int x, final int y, final int width, final int height, final int color) {
        fill(poseStack, x, y, x + width, y + 1, color);
        fill(poseStack, x, y + height - 1, x + width, y + height, color);
        fill(poseStack, x, y + 1, x + 1, y + height - 1, color);
        fill(poseStack, x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 342) QuitState.altQuit = true;
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        if (i == 342) QuitState.altQuit = false;
        return super.keyReleased(i, j, k);
    }
}
