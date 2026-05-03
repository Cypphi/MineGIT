package ca.modmonster.minegit;

import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "minegit", guiFactory = "ca.modmonster.minegit.extra.MineGITGuiFactory")
public class MineGIT {
	public static final String MOD_ID = "minegit";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
}