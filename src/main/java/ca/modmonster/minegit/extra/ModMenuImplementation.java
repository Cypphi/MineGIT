package ca.modmonster.minegit.extra;

import ca.modmonster.minegit.gui.AccountLinkScreen;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

public class ModMenuImplementation implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return AccountLinkScreen::new;
    }
}
