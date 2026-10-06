package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.StatBonus;
import com.zelf115.soulland.cultivation.skill.Skill;
import com.zelf115.soulland.cultivation.skill.SkillTag;
import com.zelf115.soulland.spirit.Affinity;
import com.zelf115.soulland.spirit.AffinitySystem;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import java.util.Optional;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A soul ring the player has absorbed, kept so it can be rendered, re-applied and inspected.
 *
 * <p>Affinities are empty for rings absorbed before they were recorded.
 */
public record AbsorbedRing(String sourceName, int tier, int years, StatBonus bonus, SoulSlot slot,
                           Optional<Skill> skill, Optional<Set<Affinity>> affinities) {

    public static final String SOURCE_NAME_KEY = "SourceName";
    public static final String TIER_KEY = "Tier";
    public static final String YEARS_KEY = "Years";
    private static final String SLOT_KEY = "Slot";

    public static AbsorbedRing readFrom(final CompoundTag tag) {
        return new AbsorbedRing(
                tag.getString(SOURCE_NAME_KEY),
                tag.getInt(TIER_KEY),
                tag.getInt(YEARS_KEY),
                StatBonus.readFrom(tag),
                readSlot(tag),
                SkillTag.read(tag),
                readAffinities(tag));
    }

    private static Optional<Set<Affinity>> readAffinities(final CompoundTag tag) {
        if (!tag.contains(AffinitySystem.RING_AFFINITIES_KEY)) {
            return Optional.empty();
        }
        return Optional.of(AffinitySystem.readRingAffinities(tag));
    }

    private static SoulSlot readSlot(final CompoundTag tag) {
        if (!tag.contains(SLOT_KEY)) {
            return SoulSlot.PRIMARY;
        }
        try {
            return SoulSlot.valueOf(tag.getString(SLOT_KEY));
        } catch (IllegalArgumentException ignored) {
            return SoulSlot.PRIMARY;
        }
    }

    public CompoundTag toNbt() {
        final CompoundTag tag = new CompoundTag();
        tag.putString(SOURCE_NAME_KEY, sourceName);
        tag.putInt(TIER_KEY, tier);
        tag.putInt(YEARS_KEY, years);
        tag.putString(SLOT_KEY, slot.name());
        bonus.writeTo(tag);
        SkillTag.write(tag, skill);
        affinities.ifPresent(ringAffinities -> AffinitySystem.writeRingAffinities(tag, ringAffinities));
        return tag;
    }

    public AbsorbedRing withBonus(final StatBonus newBonus) {
        return new AbsorbedRing(sourceName, tier, years, newBonus, slot, skill, affinities);
    }

    public Component coloredSourceName() {
        return SpiritBeastManager.nameInTierColor(sourceName, tier);
    }

    /** Stable modifier id for the ring in the given slot of the player ring list. */
    public static ResourceLocation modifierId(final int ringIndex) {
        return ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "soul_ring_" + ringIndex);
    }
}
