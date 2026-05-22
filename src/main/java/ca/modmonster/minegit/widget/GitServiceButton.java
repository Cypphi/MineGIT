package ca.modmonster.minegit.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

public class GitServiceButton extends Button {
    private final Identifier sprite;

    public GitServiceButton(Component message, Identifier sprite, OnPress onPress) {
        super(0, 0, 60, 60, message, onPress, Button.DEFAULT_NARRATION);
        this.sprite = sprite;
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractDefaultSprite(graphics);
        graphics.centeredText(Minecraft.getInstance().font, message, getX() + 30, getY() + 46, ARGB.white(1f));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX() + 14, getY() + 6, 32, 32);
    }
}
