package ca.modmonster.minegit.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GitProgressScreen extends Screen implements ProgressMonitor {
    public static final int PROGRESS_BAR_WIDTH = 128;

    @Nullable
    private StringWidget currentTaskWidget;

    private int currentTaskWork = 0;
    private int currentTaskTotalWork = 1;

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
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        renderDirtBackground(guiGraphics);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);

        // Render progress bar
        int barLeft = this.width / 2 - PROGRESS_BAR_WIDTH / 2;
        guiGraphics.fill(barLeft, this.height - 16, barLeft + PROGRESS_BAR_WIDTH, this.height - 18, 0xFFA0A0A0);

        float progress = (float) currentTaskWork / currentTaskTotalWork;
        if (progress > 1) progress = 1;
        int barPixels = (int) (PROGRESS_BAR_WIDTH * progress);
        guiGraphics.fill(barLeft, this.height - 16, barLeft + barPixels, this.height - 18, 0xFF80FF80);

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
