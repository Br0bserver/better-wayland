package dev.br0b.waylandfix;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WaylandFixClient implements ClientModInitializer {
    public static final String MOD_ID = "waylandfix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("WaylandFix client layer enabled for Minecraft 26.1.2");
    }
}
