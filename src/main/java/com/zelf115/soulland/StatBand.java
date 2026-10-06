package com.zelf115.soulland;

import net.minecraft.util.RandomSource;

/**
 * The range each stat of a soul ring or spirit bone falls in, by the colour of the beast it came from:
 * at least {@code 1 + 50 x tier}, at most {@code 100 x tier}.
 */
public final class StatBand {

    private static final double MIN_POINTS_PER_TIER = 50.0;
    private static final double MIN_FLOOR = 1.0;
    private static final double MAX_POINTS_PER_TIER = 100.0;

    private StatBand() {
    }

    public static double min(final int tier) {
        return MIN_FLOOR + MIN_POINTS_PER_TIER * tier;
    }

    public static double max(final int tier) {
        return MAX_POINTS_PER_TIER * tier;
    }

    /** Rolls each stat separately, so two drops of one colour are still worth comparing. */
    public static StatBonus roll(final int tier, final RandomSource random) {
        return new StatBonus(
                rollStat(tier, random),
                rollStat(tier, random),
                rollStat(tier, random),
                rollStat(tier, random),
                rollStat(tier, random),
                0.0);
    }

    /** Pulls every stat into the band of its tier, leaving cultivation speed alone. */
    public static StatBonus clamp(final int tier, final StatBonus bonus) {
        return new StatBonus(
                clampStat(tier, bonus.damage()),
                clampStat(tier, bonus.health()),
                clampStat(tier, bonus.defense()),
                clampStat(tier, bonus.speed()),
                clampStat(tier, bonus.spirit()),
                bonus.cultivationSpeed());
    }

    private static double rollStat(final int tier, final RandomSource random) {
        return min(tier) + random.nextDouble() * (max(tier) - min(tier));
    }

    private static double clampStat(final int tier, final double value) {
        return Math.max(min(tier), Math.min(max(tier), value));
    }
}
