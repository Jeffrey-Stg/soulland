package com.zelf115.soulland.item;

import net.minecraft.world.item.Item;

public final class PillFurnaceItem extends Item {
    public enum Tier {
        REGULAR,
        ENCHANTED,
        NETHER,
        STAR,
        DIVINE
    }

    private final Tier tier;

    public PillFurnaceItem(final Tier tier, final Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public Tier tier() {
        return tier;
    }
}
