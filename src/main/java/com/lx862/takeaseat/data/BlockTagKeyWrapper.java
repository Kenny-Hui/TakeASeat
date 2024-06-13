package com.lx862.takeaseat.data;

import net.minecraft.block.Block;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;


import net.minecraft.util.Identifier;

/**
 * Wrapper to accomodate Mojang's refactoring across different versions
 * The joy of cross version development... (None)
 */
public class BlockTagKeyWrapper {
    private final TagKey<Block> tagKey;
    public BlockTagKeyWrapper(TagKey<Block> tagKey) {
        this.tagKey = tagKey;
    }

    public TagKey<Block> get() {
        return tagKey;
    }

    public static BlockTagKeyWrapper from(Identifier identifier) {
        return new BlockTagKeyWrapper(TagKey.of(RegistryKeys.BLOCK, identifier));
    }

    @Override
    public String toString() {
        return tagKey.id().toString();
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) return true;

        if(!(o instanceof BlockTagKeyWrapper)) {
            return false;
        }

        BlockTagKeyWrapper blockTagKeyWrapper = ((BlockTagKeyWrapper)o);
        return blockTagKeyWrapper.get().id().equals(this.tagKey.id());
    }
}
