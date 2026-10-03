package com.zelf115.soulland.tournament;

import com.zelf115.soulland.cultivation.CultivationManager;
import net.minecraft.util.RandomSource;

/** How strong a tournament opponent is, and how close to the challenger it is drawn. */
public final class SoulMasterStats {

    /** The bracket runs ten levels either side of the challenger. */
    private static final int LEVEL_SPREAD = 10;
    private static final int MIN_OPPONENT_LEVEL = 10;
    private static final int MAX_OPPONENT_LEVEL = 99;
    /** A cultivator gains half a stat point per level, so the total to a level is the triangle of that. */
    private static final double STAT_POINTS_PER_LEVEL_STEP = 0.5;

    private SoulMasterStats() {
    }

    public static int rollOpponentLevel(final int challengerLevel, final RandomSource random) {
        final int offset = random.nextInt(LEVEL_SPREAD * 2 + 1) - LEVEL_SPREAD;
        return clampToBracket(challengerLevel + offset);
    }

    public static int clampToBracket(final int level) {
        return Math.max(MIN_OPPONENT_LEVEL, Math.min(MAX_OPPONENT_LEVEL, level));
    }

    /**
     * The stat total a cultivator of that level would carry, summing the same per-level gain the
     * player earns in {@link CultivationManager#regularStatIncrease(int)}.
     */
    public static double statPointsForLevel(final int level) {
        return STAT_POINTS_PER_LEVEL_STEP * level * (level + 1) / 2.0;
    }
}
