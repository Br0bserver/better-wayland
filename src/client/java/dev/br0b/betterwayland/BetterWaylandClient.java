package dev.br0b.betterwayland;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BetterWaylandClient implements ClientModInitializer {
    public static final String MOD_ID = "betterwayland";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("BetterWayland client layer enabled");
    }
}
