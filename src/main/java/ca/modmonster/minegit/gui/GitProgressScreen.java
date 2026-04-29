package ca.modmonster.minegit.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.jetbrains.annotations.NotNull;

public class GitProgressScreen extends Screen implements ProgressMonitor {
    public static final int PROGRESS_BAR_WIDTH = 128;

    private Component currentTask = CommonComponents.EMPTY;
    private int currentTaskWork = 0;
    private int currentTaskTotalWork = 1;

    public GitProgressScreen(Component component) {
        super(component);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(i);
        super.render(poseStack, i, j, f);

        // Render progress bar
        int barLeft = this.width / 2 - PROGRESS_BAR_WIDTH / 2;
        fill(poseStack, barLeft, this.height - 16, barLeft + PROGRESS_BAR_WIDTH, this.height - 18, 0xFFA0A0A0);

        float progress = (float) currentTaskWork / currentTaskTotalWork;
        if (progress > 1) progress = 1;
        int barPixels = (int) (PROGRESS_BAR_WIDTH * progress);
        fill(poseStack, barLeft, this.height - 16, barLeft + barPixels, this.height - 18, 0xFF80FF80);

        // Draw message
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 70, 16777215);

        // Draw status
        drawCenteredString(poseStack, font, currentTask, this.width / 2, this.height - 32, 16777215);
    }

    @Override
    public void start(int totalTasks) {}

    @Override
    public void beginTask(String title, int totalWork) {
        currentTask = Component.literal(title);
        currentTaskWork = 0;
        currentTaskTotalWork = totalWork != 0? totalWork : 1;
    }

    @Override
    public void update(int completed) {
        currentTaskWork += completed;
    }

    @Override
    public void endTask() {}

    @Override
    public boolean isCancelled() {return false;}

    @Override
    public void showDuration(boolean enabled) {}
}
