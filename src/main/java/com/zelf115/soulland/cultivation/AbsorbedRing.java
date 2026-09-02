package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.StatBonus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/** A soul ring the player has absorbed, kept so it can be rendered, re-applied and inspected. */
public record AbsorbedRing(String sourceName, int tier, int years, StatBonus bonus) {

    private static final String SOURCE_NAME_KEY = "SourceName";
    private static final String TIER_KEY = "Tier";
    private static final String YEARS_KEY = "Years";

    public static AbsorbedRing readFrom(final CompoundTag tag) {
        return new AbsorbedRing(
                tag.getString(SOURCE_NAME_KEY),
                tag.getInt(TIER_KEY),
                tag.getInt(YEARS_KEY),
                StatBonus.readFrom(tag));
    }

    public CompoundTag toNbt() {
        final CompoundTag tag = new CompoundTag();
        tag.putString(SOURCE_NAME_KEY, sourceName);
        tag.putInt(TIER_KEY, tier);
        tag.putInt(YEARS_KEY, years);
        bonus.writeTo(tag);
        return tag;
    }

    /** Stable modifier id for the ring in the given slot of the player ring list. */
    public static ResourceLocation modifierId(final int ringIndex) {
        return ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "soul_ring_" + ringIndex);
    }
}
