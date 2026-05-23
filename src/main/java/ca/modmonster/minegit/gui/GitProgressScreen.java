package ca.modmonster.minegit.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.eclipse.jgit.lib.ProgressMonitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GitProgressScreen extends Screen implements ProgressMonitor {
    public static final int PROGRESS_BAR_WIDTH = 128;

    @Nullable
    private StringWidget currentTaskWidget;

    private float currentTaskProgress = 0;
    private float currentTaskProgressTarget = 0;
    private int currentTaskTotalWork = 1;
    private float indeterminateTimer = 0;

    public GitProgressScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        this.currentTaskWidget = this.addRenderableWidget(new StringWidget(Component.empty(), font));
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (this.currentTaskWidget != null) {
            this.currentTaskWidget.setPosition(this.width / 2 - this.currentTaskWidget.getWidth() / 2, this.height - 32);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected boolean shouldNarrateNavigation() {
        return false;
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics) {
        renderDirtBackground(guiGraphics);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderDirtBackground(guiGraphics);
        super.render(guiGraphics, i, j, f);

        // Render progress bar base
        int barLeft = this.width / 2 - PROGRESS_BAR_WIDTH / 2;
        guiGraphics.fill(barLeft, this.height - 16, barLeft + PROGRESS_BAR_WIDTH, this.height - 18, 0xFFA0A0A0);

        // Render progress fill
        if (currentTaskTotalWork != 0) {
            // Determinate progress
            currentTaskProgress += (currentTaskProgressTarget - currentTaskProgress) * f * 0.2f;
            if (currentTaskProgress > 1) currentTaskProgress = 1;
            int barPixels = (int) (PROGRESS_BAR_WIDTH * currentTaskProgress);
            guiGraphics.fill(barLeft, this.height - 16, barLeft + barPixels, this.height - 18, 0xFF80FF80);
        } else {
            // Indeterminate progress
            indeterminateTimer += f;

            // ranges from [-barwidth, barwidth]
            int l = (int) (Math.sin(indeterminateTimer / 10f) * PROGRESS_BAR_WIDTH);
            int r = l + PROGRESS_BAR_WIDTH;
            l = Mth.clamp(l, 0, PROGRESS_BAR_WIDTH);
            r = Mth.clamp(r, 0, PROGRESS_BAR_WIDTH);
            guiGraphics.fill(barLeft + l, this.height - 16, barLeft + r, this.height - 18, 0xFF80FF80);
        }

        // Draw message
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 70, 16777215);
    }

    @Override
    public void start(int totalTasks) {}

    @Override
    public void beginTask(String title, int totalWork) {
        if (currentTaskWidget != null) {
            minecraft.submit(() -> {
                Component message = Component.literal(title);
                currentTaskWidget.setMessage(message);
                currentTaskWidget.setWidth(font.width(message));
                repositionElements();
            });
        }
        if (currentTaskTotalWork != 0) indeterminateTimer = 0;
        currentTaskProgress = 0;
        currentTaskProgressTarget = 0;
        currentTaskTotalWork = totalWork;
    }

    @Override
    public void update(int completed) {
        currentTaskProgressTarget += (float) completed / currentTaskTotalWork;
    }

    @Override
    public void endTask() {}

    @Override
    public boolean isCancelled() {return false;}

    @Override
    public void showDuration(boolean enabled) {}
}
