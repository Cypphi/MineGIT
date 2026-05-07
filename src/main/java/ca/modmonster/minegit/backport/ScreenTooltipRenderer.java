package ca.modmonster.minegit.backport;

import java.util.Collections;
import java.util.List;

public interface ScreenTooltipRenderer {
    default void renderTooltip(String tooltip, int x, int y) {
        renderTooltip(Collections.singletonList(tooltip), x, y);
    }
    void renderTooltip(List<String> tooltip, int x, int y);
}
