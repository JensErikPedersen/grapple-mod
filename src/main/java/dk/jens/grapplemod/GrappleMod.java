package dk.jens.grapplemod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class GrappleMod implements ModInitializer {
	public static final String MOD_ID = "grapplemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.initialize();

		// Flyt krogene og træk spillerne én gang hvert tick (20 gange i sekundet)
		ServerTickEvents.END_SERVER_TICK.register(GrappleManager::tick);
		// Glem alle kroge, når verdenen lukkes
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> GrappleManager.clear());

		LOGGER.info("Grapple Mod er indlæst!");
	}
}
