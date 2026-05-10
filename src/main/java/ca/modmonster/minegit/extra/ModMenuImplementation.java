package ca.modmonster.minegit.extra;

import ca.modmonster.minegit.MineGIT;
import ca.modmonster.minegit.gui.AccountLinkScreen;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.Screen;

import java.util.function.Function;

public class ModMenuImplementation implements ModMenuApi {
    @Override
    public String getModId() {
        return MineGIT.MOD_ID;
    }

    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return AccountLinkScreen::new;
    }
}