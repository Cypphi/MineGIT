package ca.modmonster.minegit.gui;

import net.minecraft.client.gui.screen.Screen;
import org.eclipse.jgit.lib.ProgressMonitor;

public class GitProgressScreen extends Screen implements ProgressMonitor {
    public static final int PROGRESS_BAR_WIDTH = 128;

    private final String title;
    private String currentTask = "";
    private int currentTaskWork = 0;
    private int currentTaskTotalWork = 1;

    public GitProgressScreen(String title) {
        this.title = title;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(int i, int j, float f) {
        this.drawBackgroundTexture(i);
        super.render(i, j, f);

        // Render progress bar
        int barLeft = this.width / 2 - PROGRESS_BAR_WIDTH / 2;
        fill(barLeft, this.height - 16, barLeft + PROGRESS_BAR_WIDTH, this.height - 18, 0xFFA0A0A0);

        float progress = (float) currentTaskWork / currentTaskTotalWork;
        if (progress > 1) progress = 1;
        int barPixels = (int) (PROGRESS_BAR_WIDTH * progress);
        fill(barLeft, this.height - 16, barLeft + barPixels, this.height - 18, 0xFF80FF80);

        // Draw message
        drawCenteredString(this.textRenderer, title, this.width / 2, 70, 16777215);

        // Draw status
        drawCenteredString(textRenderer, currentTask, this.width / 2, this.height - 32, 16777215);
    }

    @Override
    public void start(int totalTasks) {}

    @Override
    public void beginTask(String title, int totalWork) {
        currentTask = title;
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
}
