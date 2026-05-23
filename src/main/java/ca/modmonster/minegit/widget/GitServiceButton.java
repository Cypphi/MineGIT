package ca.modmonster.minegit.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public class GitServiceButton extends Button {
    private final ResourceLocation sprite;

    public GitServiceButton(Component message, ResourceLocation sprite, OnPress onPress) {
        super(0, 0, 60, 60, message, onPress, Button.DEFAULT_NARRATION);
        this.sprite = sprite;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float a) {
        super.renderWidget(graphics, mouseX, mouseY, a);
        graphics.blitSprite(sprite, getX() + 14, getY() + 6, 32, 32);
    }

    @Override
    public void renderString(GuiGraphics graphics, Font font, int i) {
        graphics.drawCenteredString(font, getMessage(), getX() + 30, getY() + 46, i);
    }
}
