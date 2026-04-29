package io.github.ryn.fungus;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fungus implements ModInitializer {
    public static final String MOD_ID = "fungus";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOG.info("[Fungus] common init");
    }
}
