package ca.modmonster.minegit.extra;

import ca.modmonster.minegit.gui.AccountLinkScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuImplementation implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return AccountLinkScreen::new;
    }
}
