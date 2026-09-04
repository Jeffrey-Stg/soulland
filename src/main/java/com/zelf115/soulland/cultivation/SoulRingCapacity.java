package com.zelf115.soulland.cultivation;

import java.util.List;

/**
 * The spirit-stat gate on soul ring absorption.
 *
 * <p>The design states the limit as combinations rather than a single number:
 * one yellow below 100 spirit; two yellow or one purple to 499; five yellow, three purple or one
 * black to 4 999; one red plus one black (or three purple, or five yellow), or five of anything
 * but red, to 19 999; anything at all from 20 000.
 *
 * <p>Every one of those reads as a budget: each ring costs a weight, each spirit band affords a
 * total. Weights are scaled by three so that "three purple equals five yellow" stays exact in
 * integer maths. Red and orange are treated as the same colour, so they share a weight.
 */
public final class SoulRingCapacity {

    private static final int UNLIMITED = Integer.MAX_VALUE;
    /** Ring weight by tier, index 0 unused. White is priced as yellow; gold only exists above the last band. */
    private static final int[] WEIGHT_BY_TIER = {0, 3, 3, 5, 15, 60, 60, 300};

    private static final double BAND_1_MAX_SPIRIT = 100.0;
    private static final double BAND_2_MAX_SPIRIT = 500.0;
    private static final double BAND_3_MAX_SPIRIT = 5_000.0;
    private static final double BAND_4_MAX_SPIRIT = 20_000.0;

    private SoulRingCapacity() {
    }

    /** Total ring weight a player with this spirit stat can carry. */
    public static int capacityFor(final double spiritValue) {
        if (spiritValue < BAND_1_MAX_SPIRIT) {
            return 3;
        }
        if (spiritValue < BAND_2_MAX_SPIRIT) {
            return 6;
        }
        if (spiritValue < BAND_3_MAX_SPIRIT) {
            return 15;
        }
        if (spiritValue < BAND_4_MAX_SPIRIT) {
            return 75;
        }
        return UNLIMITED;
    }

    public static int weightOf(final int tier) {
        if (tier < 1 || tier >= WEIGHT_BY_TIER.length) {
            return WEIGHT_BY_TIER[WEIGHT_BY_TIER.length - 1];
        }
        return WEIGHT_BY_TIER[tier];
    }

    public static int usedWeight(final List<AbsorbedRing> rings) {
        int used = 0;
        for (final AbsorbedRing ring : rings) {
            used += weightOf(ring.tier());
        }
        return used;
    }

    /** The strongest ring colour this spirit stat can hold on its own. */
    public static int maxAbsorbableTier(final double spiritValue) {
        final int capacity = capacityFor(spiritValue);
        int highest = 1;
        for (int tier = 1; tier < WEIGHT_BY_TIER.length; tier++) {
            if (weightOf(tier) <= capacity) {
                highest = tier;
            }
        }
        return highest;
    }

    /** Whether the rings already absorbed leave room for one more of {@code tier}. */
    public static boolean hasRoomFor(final double spiritValue, final List<AbsorbedRing> rings, final int tier) {
        final int capacity = capacityFor(spiritValue);
        if (capacity == UNLIMITED) {
            return true;
        }
        return usedWeight(rings) + weightOf(tier) <= capacity;
    }
}
