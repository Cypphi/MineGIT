package ca.modmonster.minegit;

import ca.modmonster.minegit.gui.AccountLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MineGIT.MOD_ID)
public class MineGIT {
	public static final String MOD_ID = "minegit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public MineGIT(ModContainer container) {
		container.registerExtensionPoint(IConfigScreenFactory.class, (final ModContainer mod, final Screen parent) -> new AccountLinkScreen(parent));
	}
}