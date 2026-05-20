package ca.modmonster.minegit.widget;

import ca.modmonster.minegit.backport.ImageButton;
import net.minecraft.client.resources.I18n;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public enum WorldSyncButtonState {
    SETUP(ImageButton.ImageButtonTex.CLOUD, () -> Collections.singletonList(I18n.format("minegit.link.setup"))),
    ENABLE(ImageButton.ImageButtonTex.CLOUD, () -> Arrays.asList(I18n.format("minegit.sync.enable"), I18n.format("minegit.sync.alt_configure"))),
    WORLD_CONFIGURE(ImageButton.ImageButtonTex.CHECK, () -> Arrays.asList(I18n.format("minegit.sync.enabled"), I18n.format("minegit.sync.alt_configure")));

    public final ImageButton.ImageButtonTex texture;
    private final Supplier<List<String>> tooltip;

    WorldSyncButtonState(ImageButton.ImageButtonTex texture, Supplier<List<String>> tooltip) {
        this.texture = texture;
        this.tooltip = tooltip;
    }

    public final List<String> getTooltip() {
        return tooltip.get();
    }
}
