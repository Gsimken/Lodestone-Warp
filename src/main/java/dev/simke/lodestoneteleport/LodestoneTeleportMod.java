package dev.simke.lodestoneteleport;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LodestoneTeleportMod implements ModInitializer {
	public static final String MOD_ID = "lodestone_teleport";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LodestoneConfig.load();
		LodestoneNetworking.register();
		LodestoneEvents.register();
		LodestoneCommands.register();
		LOGGER.info("Lodestone Warps v0 initialized.");
	}
}
