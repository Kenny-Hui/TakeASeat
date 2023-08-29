package com.lx862.takeaseat;

import com.lx862.takeaseat.commands.takeaseat;
import com.lx862.takeaseat.config.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TakeASeat implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("TakeASeat");

    @Override
    public void onInitialize() {
        LOGGER.info("[TakeASeat] Take a seat!");
        Config.load();

        UseBlockCallback.EVENT.register(SittingManager::onBlockRightClick);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            takeaseat.register("takeaseat", dispatcher);
        });
    }
}
