package ca.modmonster.minegit.gui;

import net.minecraft.client.gui.GuiScreen;

import org.eclipse.jgit.lib.ProgressMonitor;

public class GitProgressScreen extends GuiScreen implements ProgressMonitor {
    public static final int PROGRESS_BAR_WIDTH = 128;

    private final String title;
    private String currentTask = "";
    private int currentTaskWork = 0;
    private int currentTaskTotalWork = 1;

    public GitProgressScreen(String title) {
        this.title = title;
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        this.drawDefaultBackground();
        super.drawScreen(i, j, f);

        // Render progress bar
        int barLeft = this.width / 2 - PROGRESS_BAR_WIDTH / 2;
        drawRect(barLeft, this.height - 16, barLeft + PROGRESS_BAR_WIDTH, this.height - 18, 0xFFA0A0A0);

        float progress = (float) currentTaskWork / currentTaskTotalWork;
        if (progress > 1) progress = 1;
        int barPixels = (int) (PROGRESS_BAR_WIDTH * progress);
        drawRect(barLeft, this.height - 16, barLeft + barPixels, this.height - 18, 0xFF80FF80);

        // Draw message
        drawCenteredString(fontRenderer, this.title, this.width / 2, 70, 16777215);

        // Draw status
        drawCenteredString(fontRenderer, currentTask, this.width / 2, this.height - 32, 16777215);
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

    @Override
    protected void keyTyped(char typedChar, int keyCode) {}
}
