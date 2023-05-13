package com.lx.takeaseat.data;

import net.minecraft.block.Block;

#if MC_VERSION >= "11903"
    import net.minecraft.registry.RegistryKeys;
    import net.minecraft.registry.tag.TagKey;
#else
    import net.minecraft.tag.TagKey;
    import net.minecraft.util.registry.Registry;
#endif

import net.minecraft.util.Identifier;

public class BlockTagKeyWrapper {
    private final TagKey<Block> tagKey;
    public BlockTagKeyWrapper(TagKey<Block> tagKey) {
        this.tagKey = tagKey;
    }

    public TagKey<Block> get() {
        return tagKey;
    }

    public static BlockTagKeyWrapper from(Identifier identifier) {

        #if MC_VERSION >= "11903"
        return new BlockTagKeyWrapper(TagKey.of(RegistryKeys.BLOCK, identifier));
        #else
        return new BlockTagKeyWrapper(TagKey.of(Registry.BLOCK_KEY, identifier));
        #endif
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

        BlockTagKeyWrapper blockTagKeyWrapper2 = ((BlockTagKeyWrapper)o);
        return blockTagKeyWrapper2.get().id().equals(this.tagKey.id());
    }
}
