package com.lx862.takeaseat.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lx862.takeaseat.TakeASeat;
import com.lx862.takeaseat.Util;
import com.lx862.takeaseat.data.TagKeyUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Config {
    private static final Path CONFIG_PATH = Paths.get(FabricLoader.getInstance().getConfigDir().toString(), "takeaseat.json");
    private final List<Identifier> allowedBlockId = new ArrayList<>();
    private final List<TagKey<Block>> allowedBlockTag = new ArrayList<>(Arrays.asList(TagKeyUtil.fromBlock(Identifier.of("stairs")), TagKeyUtil.fromBlock(Identifier.of("slabs"))));
    private boolean ensurePlayerWontSuffocate = true;
    private boolean mustBeEmptyHandToSit = true;
    private boolean blockMustBeLowerThanPlayer = true;
    private boolean mustNotBeObstructed = false;
    private boolean stairs025Offset = false;
    private int requiredOpLevel = 0;
    private double maxDistance = 0;

    public void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                allowedBlockId.clear();
                allowedBlockTag.clear();

                final JsonObject jsonConfig = JsonParser.parseString(String.join("", Files.readAllLines(CONFIG_PATH))).getAsJsonObject();

                if (jsonConfig.has("allowedBlockId")) {
                    jsonConfig.getAsJsonArray("allowedBlockId").forEach(e -> {
                        allowedBlockId.add(Identifier.of(e.getAsString()));
                    });
                }

                if (jsonConfig.has("allowedBlockTag")) {
                    jsonConfig.getAsJsonArray("allowedBlockTag").forEach(e -> {
                        allowedBlockTag.add(TagKeyUtil.fromBlock(Identifier.of(e.getAsString())));
                    });
                }

                ensurePlayerWontSuffocate = JsonHelper.getBoolean(jsonConfig, "ensurePlayerWontSuffocate", ensurePlayerWontSuffocate);
                stairs025Offset = JsonHelper.getBoolean(jsonConfig, "stairsOffset", stairs025Offset);
                mustBeEmptyHandToSit = JsonHelper.getBoolean(jsonConfig, "mustBeEmptyHandToSit", mustBeEmptyHandToSit);
                blockMustBeLowerThanPlayer = JsonHelper.getBoolean(jsonConfig, "blockMustBeLowerThanPlayer", blockMustBeLowerThanPlayer);
                mustNotBeObstructed = JsonHelper.getBoolean(jsonConfig, "mustNotBeObstructed", mustNotBeObstructed);
                maxDistance = JsonHelper.getDouble(jsonConfig, "maxDistance", maxDistance);
                requiredOpLevel = JsonHelper.getInt(jsonConfig, "requiredOpLevel", requiredOpLevel);
            } catch (Exception e) {
                TakeASeat.LOGGER.warn("[TakeASeat] Unable to read config file! Regenerating one...");
                e.printStackTrace();
                write();
            }
        } else {
            write();
        }
    }

    public void write() {
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
            jsonConfig.addProperty("requiredOpLevel", requiredOpLevel);

            Files.write(CONFIG_PATH, Collections.singleton(new GsonBuilder().setPrettyPrinting().create().toJson(jsonConfig)));
        } catch (Exception e) {
            TakeASeat.LOGGER.error("", e);
        }
    }

    public boolean blockIdIsAllowed(Identifier identifier) {
        return allowedBlockId.contains(identifier);
    }

    public boolean ensurePlayerWontSuffocate() {
        return ensurePlayerWontSuffocate;
    }

    public boolean mustBeEmptyHandToSit() {
        return mustBeEmptyHandToSit;
    }

    public boolean blockMustBeLowerThanPlayer() {
        return blockMustBeLowerThanPlayer;
    }

    public boolean mustNotBeObstructed() {
        return mustNotBeObstructed;
    }

    public double maxDistance() {
        return maxDistance;
    }

    public int requiredOpLevel() {
        return requiredOpLevel;
    }

    public boolean stairs025Offset() {
        return stairs025Offset;
    }

    public List<TagKey<Block>> getAllowedBlockTag() {
        return allowedBlockTag;
    }
}
