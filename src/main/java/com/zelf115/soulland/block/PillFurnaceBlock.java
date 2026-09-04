package com.zelf115.soulland.block;

import com.zelf115.soulland.item.PillFurnaceItem;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class PillFurnaceBlock extends FurnaceBlock {
    private final PillFurnaceItem.Tier tier;

    public PillFurnaceBlock(final PillFurnaceItem.Tier tier, final BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public PillFurnaceItem.Tier tier() {
        return tier;
    }

    public int bonusPillCount() {
        return tier == PillFurnaceItem.Tier.REGULAR ? 0 : tier.ordinal();
    }

    public float processingMultiplier() {
        return switch (tier) {
            case REGULAR -> 1.0F;
            case ENCHANTED -> 1.25F;
            case NETHER -> 1.5F;
            case STAR -> 1.75F;
            case DIVINE -> 2.0F;
        };
    }
}
