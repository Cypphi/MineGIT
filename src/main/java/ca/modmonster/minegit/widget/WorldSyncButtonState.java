package ca.modmonster.minegit.widget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum WorldSyncButtonState {
    SETUP(new TextComponent("☁"), Collections.singletonList(new TranslatableComponent("minegit.link.setup"))),
    ENABLE(new TextComponent("☁"), Arrays.asList(new TranslatableComponent("minegit.sync.enable"), new TranslatableComponent("minegit.sync.alt_configure"))),
    WORLD_CONFIGURE(new TextComponent("✔"), Arrays.asList(new TranslatableComponent("minegit.sync.enabled"), new TranslatableComponent("minegit.sync.alt_configure")));

    public final Component message;
    public final List<Component> tooltip;

    WorldSyncButtonState(Component message, List<Component> tooltip) {
        this.message = message;
        this.tooltip = tooltip;
    }
}
