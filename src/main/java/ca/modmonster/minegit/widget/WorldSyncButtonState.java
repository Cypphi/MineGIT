package ca.modmonster.minegit.widget;

import net.minecraft.client.resource.language.I18n;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public enum WorldSyncButtonState {
    SETUP("☁", () -> Collections.singletonList(I18n.translate("minegit.link.setup"))),
    ENABLE("☁", () -> Arrays.asList(I18n.translate("minegit.sync.enable"), I18n.translate("minegit.sync.alt_configure"))),
    WORLD_CONFIGURE("✔", () -> Arrays.asList(I18n.translate("minegit.sync.enabled"), I18n.translate("minegit.sync.alt_configure")));

    public final String message;
    private final Supplier<List<String>> tooltip;

    WorldSyncButtonState(String message, Supplier<List<String>> tooltip) {
        this.message = message;
        this.tooltip = tooltip;
    }

    public final List<String> getTooltip() {
        return tooltip.get();
    }
}
