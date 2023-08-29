package com.lx.takeaseat.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lx.takeaseat.TakeASeat;
import com.lx.takeaseat.Util;
import com.lx.takeaseat.data.BlockTagKeyWrapper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Config {
    private static final Path CONFIG_PATH = Paths.get(FabricLoader.getInstance().getConfigDir().toString(), "takeaseat.json");
    private static final List<Identifier> allowedBlockId = new ArrayList<>();
    private static final List<BlockTagKeyWrapper> allowedBlockTag = new ArrayList<>(Arrays.asList(BlockTagKeyWrapper.from(new Identifier("stairs")), BlockTagKeyWrapper.from(new Identifier("slabs"))));
    private static boolean ensurePlayerWontSuffocate = true;
    private static boolean mustBeEmptyHandToSit = true;
    private static boolean blockMustBeLowerThanPlayer = true;
    private static boolean mustNotBeObstructed = false;
    private static boolean stairs025Offset = false;
    private static double maxDistance = 0;

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                allowedBlockId.clear();
                allowedBlockTag.clear();

                final JsonObject jsonConfig = new JsonParser().parse(String.join("", Files.readAllLines(CONFIG_PATH))).getAsJsonObject();

                if (jsonConfig.has("allowedBlockId")) {
                    jsonConfig.getAsJsonArray("allowedBlockId").forEach(e -> {
                        allowedBlockId.add(new Identifier(e.getAsString()));
                    });
                }

                if (jsonConfig.has("allowedBlockTag")) {
                    jsonConfig.getAsJsonArray("allowedBlockTag").forEach(e -> {
                        allowedBlockTag.add(BlockTagKeyWrapper.from(new Identifier(e.getAsString())));
                    });
                }

                if (jsonConfig.has("ensurePlayerWontSuffocate")) {
                    ensurePlayerWontSuffocate = jsonConfig.get("ensurePlayerWontSuffocate").getAsBoolean();
                }

                if(jsonConfig.has("stairsOffset")) {
                    stairs025Offset = jsonConfig.get("stairsOffset").getAsBoolean();
                }

                if (jsonConfig.has("mustBeEmptyHandToSit")) {
                    mustBeEmptyHandToSit = jsonConfig.get("mustBeEmptyHandToSit").getAsBoolean();
                }

                if (jsonConfig.has("blockMustBeLowerThanPlayer")) {
                    blockMustBeLowerThanPlayer = jsonConfig.get("blockMustBeLowerThanPlayer").getAsBoolean();
                }

                if (jsonConfig.has("mustNotBeObstructed")) {
                    mustNotBeObstructed = jsonConfig.get("mustNotBeObstructed").getAsBoolean();
                }

                if (jsonConfig.has("maxDistance")) {
                    maxDistance = jsonConfig.get("maxDistance").getAsDouble();
                }
            } catch (Exception e) {
                TakeASeat.LOGGER.warn("[TakeASeat] Unable to read config file! Regenerating one...");
                e.printStackTrace();
                write();
            }
        } else {
            write();
        }
    }

    public static void write() {
        try {
            TakeASeat.LOGGER.info("[TakeASeat] Writing Config...");
            final JsonObject jsonConfig = new JsonObject();
            jsonConfig.add("allowedBlockId", Util.toJsonArray(allowedBlockId));
            jsonConfig.add("allowedBlockTag", Util.toJsonArray(allowedBlockTag));
            jsonConfig.addProperty("stairsOffset", stairs025Offset);
            jsonConfig.addProperty("ensurePlayerWontSuffocate", ensurePlayerWontSuffocate);
            jsonConfig.addProperty("mustBeEmptyHandToSit", mustBeEmptyHandToSit);
            jsonConfig.addProperty("blockMustBeLowerThanPlayer", blockMustBeLowerThanPlayer);
            jsonConfig.addProperty("mustNotBeObstructed", mustNotBeObstructed);
            jsonConfig.addProperty("maxDistance", maxDistance);

            Files.write(CONFIG_PATH, Collections.singleton(new GsonBuilder().setPrettyPrinting().create().toJson(jsonConfig)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean blockIdIsAllowed(Identifier identifier) {
        return allowedBlockId.contains(identifier);
    }

    public static boolean ensurePlayerWontSuffocate() {
        return ensurePlayerWontSuffocate;
    }

    public static boolean mustBeEmptyHandToSit() {
        return mustBeEmptyHandToSit;
    }

    public static boolean blockMustBeLowerThanPlayer() {
        return blockMustBeLowerThanPlayer;
    }

    public static boolean mustNotBeObstructed() {
        return mustNotBeObstructed;
    }

    public static double maxDistance() {
        return maxDistance;
    }
    public static boolean stairs025Offset() {
        return stairs025Offset;
    }

    public static List<BlockTagKeyWrapper> getAllowedBlockTag() {
        return allowedBlockTag;
    }
}
