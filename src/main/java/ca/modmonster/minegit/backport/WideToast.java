package ca.modmonster.minegit.backport;

import ca.modmonster.minegit.mixin.SystemToastAccessor;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

public class WideToast {
    public static SystemToast get(Font font, Component component) {
        return SystemToastAccessor.create(SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE, component, ImmutableList.of(), Math.max(200, font.width(component) + 30));
    }
}
