package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Shadow
    @Nullable
    private Button disconnectButton;

    @Unique
    private final Tooltip tooltip = Tooltip.create(Component.translatable("minegit.exit_without_push"));

    @Inject(at = @At("TAIL"), method = "render", remap = false)
    private void render(final PoseStack poseStack, final int mouseX, final int mouseY, final float a, CallbackInfo info) {
        if (disconnectButton == null) return;
        if (!minecraft.isLocalServer()) return;
        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server == null) return;
        if (!GitManager.syncEnabled(server.getWorldPath(LevelResource.ROOT))) return;
        if (!QuitState.altQuit) {
            disconnectButton.setTooltip(null);
            return;
        }
        disconnectButton.setTooltip(tooltip);

        if (disconnectButton.isHoveredOrFocused()) {
            // draw red border
            renderOutline(
                    poseStack,
                    disconnectButton.getX(),
                    disconnectButton.getY(),
                    disconnectButton.getWidth(),
                    disconnectButton.getHeight(),
                    -65536
            );
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
        if (i == InputConstants.KEY_LALT) QuitState.altQuit = true;
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        if (i == InputConstants.KEY_LALT) QuitState.altQuit = false;
        return super.keyReleased(i, j, k);
    }
}
