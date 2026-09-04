package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.StatBonus;
import java.util.Locale;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/** A spirit bone the player has absorbed into one body slot. */
public record AbsorbedBone(String slot, String sourceName, int tier, int years, StatBonus bonus) {

    public static final String SLOT_KEY = "Slot";
    public static final String SOURCE_NAME_KEY = "SourceName";
    public static final String TIER_KEY = "Tier";
    public static final String YEARS_KEY = "Years";

    public static AbsorbedBone readFrom(final CompoundTag tag) {
        return new AbsorbedBone(
                tag.getString(SLOT_KEY),
                tag.getString(SOURCE_NAME_KEY),
                tag.getInt(TIER_KEY),
                tag.getInt(YEARS_KEY),
                StatBonus.readFrom(tag));
    }

    public CompoundTag toNbt() {
        final CompoundTag tag = new CompoundTag();
        tag.putString(SLOT_KEY, slot);
        tag.putString(SOURCE_NAME_KEY, sourceName);
        tag.putInt(TIER_KEY, tier);
        tag.putInt(YEARS_KEY, years);
        bonus.writeTo(tag);
        return tag;
    }

    /** Stable modifier id for whatever bone currently occupies the given slot. */
    public static ResourceLocation modifierId(final String slot) {
        return ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "spirit_bone_" + slotKey(slot));
    }

    public static String slotKey(final String slot) {
        return slot.toLowerCase(Locale.ROOT).replace(' ', '_');
    }
}
