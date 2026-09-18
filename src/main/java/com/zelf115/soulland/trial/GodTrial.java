package com.zelf115.soulland.trial;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.MartialSoul;
import com.zelf115.soulland.cultivation.SoulSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

/** A god whose altar grants a trial, and the relic that trial awards. */
public enum GodTrial {
    SERAPHIM("seraphim", MartialSoul.SERAPHIM),
    DEATH_GOD("death_god", null),
    SEA_GOD("sea_god", null),
    RAKSHASA("rakshasa", null);

    private final String name;
    private final MartialSoul requiredMartialSoul;

    GodTrial(final String name, final MartialSoul requiredMartialSoul) {
        this.name = name;
        this.requiredMartialSoul = requiredMartialSoul;
    }

    public Component displayName() {
        return Component.translatable("soulland.trial.name." + name);
    }

    /**
     * Resolved on call rather than held in a field: the altar blocks are registered before the
     * relic items, so a captured reference would read a field that is still null.
     */
    public Item relicItem() {
        return switch (this) {
            case SERAPHIM -> SoulLand.ANGEL_GOD_SWORD.get();
            case DEATH_GOD -> SoulLand.ASURA_SWORD.get();
            case SEA_GOD -> SoulLand.SEA_GOD_TRIDENT.get();
            case RAKSHASA -> SoulLand.RAKSHASA_DAGGER.get();
        };
    }

    public MartialSoul getRequiredMartialSoul() {
        return requiredMartialSoul;
    }

    /** Whether the player carries the martial soul this god demands, in either soul slot. */
    public boolean acceptsMartialSoulOf(final CultivationData data) {
        if (requiredMartialSoul == null) {
            return true;
        }
        return requiredMartialSoul == data.getMartialSoul(SoulSlot.PRIMARY)
                || requiredMartialSoul == data.getMartialSoul(SoulSlot.SECONDARY);
    }

    /** The god this item is the relic of, or null when the item is not a relic at all. */
    public static GodTrial forRelic(final Item item) {
        for (final GodTrial trial : values()) {
            if (trial.relicItem() == item) {
                return trial;
            }
        }
        return null;
    }
}
