package com.zelf115.soulland.block;

import com.mojang.serialization.MapCodec;
import com.zelf115.soulland.SoulLand;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

/** The Common Spirit Herb planted on farmland; the herb itself is its seed. */
public final class SpiritHerbCropBlock extends CropBlock {

    public static final MapCodec<SpiritHerbCropBlock> CODEC = simpleCodec(SpiritHerbCropBlock::new);

    public SpiritHerbCropBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<SpiritHerbCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return SoulLand.COMMON_SPIRIT_HERB.get();
    }
}
