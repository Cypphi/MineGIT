package ca.modmonster.minegit.widget;

import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

public enum WorldSyncButtonState {
    SETUP(Component.literal("☁"), List.of(Component.translatable("minegit.link.setup"))),
    ENABLE(Component.literal("☁"), Arrays.asList(Component.translatable("minegit.sync.enable"), Component.translatable("minegit.sync.alt_configure"))),
    WORLD_CONFIGURE(Component.literal("✔"), Arrays.asList(Component.translatable("minegit.sync.enabled"), Component.translatable("minegit.sync.alt_configure")));

    public final Component message;
    public final List<Component> tooltip;

    WorldSyncButtonState(Component message, List<Component> tooltip) {
        this.message = message;
        this.tooltip = tooltip;
    }
}
