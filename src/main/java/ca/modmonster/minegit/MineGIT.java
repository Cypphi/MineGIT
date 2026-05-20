package ca.modmonster.minegit;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: explanation for api url vs web url (do we need both?)
// TODO: hide entire web url section for GitHub
// TODO: remove need for scrolling, i don't like it
// TODO: allow typing just a repo name "minegit_testing-world" to default to "<username>/minegit_testing-world" like what currently happens
// TODO: replace text that says "pulling from remote" with "pulling from <service>" (custom can stay "pulling from remote") - do this with all occurances like force quit, pushing, etc
// TODO: finish adding support for github orgs as a service option (#57)

public class MineGIT implements ClientModInitializer {
	public static final String MOD_ID = "minegit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {

    }
}