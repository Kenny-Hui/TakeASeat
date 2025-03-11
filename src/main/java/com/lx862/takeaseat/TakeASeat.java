package com.lx862.takeaseat;

import com.lx862.takeaseat.commands.TakeASeatCommand;
import com.lx862.takeaseat.config.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TakeASeat implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("TakeASeat");
    private static final Config config = new Config();

    @Override
    public void onInitialize() {
        LOGGER.info("[TakeASeat] Take a seat!");
        config.load();

        UseBlockCallback.EVENT.register(SittingManager::onBlockRightClick);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TakeASeatCommand.register("takeaseat", dispatcher);
        });
    }

    public static Config getConfig() {
        return config;
    }
}
