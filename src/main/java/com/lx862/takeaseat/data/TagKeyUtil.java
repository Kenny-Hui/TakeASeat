package com.lx862.takeaseat.data;

import net.minecraft.block.Block;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

import net.minecraft.util.Identifier;

public class TagKeyUtil {
    public static TagKey<Block> fromBlock(Identifier identifier) {
        return TagKey.of(RegistryKeys.BLOCK, identifier);
    }
}
