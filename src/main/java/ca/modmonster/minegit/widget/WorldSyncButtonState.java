package ca.modmonster.minegit.widget;

import ca.modmonster.minegit.backport.ImageButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public enum WorldSyncButtonState {
    SETUP(ImageButton.ImageButtonTex.CLOUD, () -> Collections.singletonList("Setup Cloud Syncing")),
    ENABLE(ImageButton.ImageButtonTex.CLOUD, () -> Arrays.asList("Enable Cloud Sync", "(Alt+Click to configure)")),
    WORLD_CONFIGURE(ImageButton.ImageButtonTex.CHECK, () -> Arrays.asList("Cloud Sync Enabled!", "(Alt+Click to configure)"));

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
