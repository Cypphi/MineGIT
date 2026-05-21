package ca.modmonster.minegit;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: explanation for api url vs web url (do we need both?)

public class MineGIT implements ClientModInitializer {
	public static final String MOD_ID = "minegit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {

    }
}