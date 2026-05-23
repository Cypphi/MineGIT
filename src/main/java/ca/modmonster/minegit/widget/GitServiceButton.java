package ca.modmonster.minegit.widget;

import com.mojang.blaze3d.vertex.PoseStack;

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
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(getX() + 14, getY() + 6, 0);
        pose.scale(2, 2, 1);
        graphics.blit(
                sprite,
                0, 0,
                0, 0,
                16, 16,
                16, 16
        );
        pose.popPose();
    }

    @Override
    public void renderString(GuiGraphics graphics, Font font, int i) {
        graphics.drawCenteredString(font, getMessage(), getX() + 30, getY() + 46, i);
    }
}
