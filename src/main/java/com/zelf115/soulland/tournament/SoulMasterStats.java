package com.zelf115.soulland.tournament;

import com.zelf115.soulland.cultivation.CultivationManager;

/** How strong a tournament opponent is: each round of the ladder fights a stronger soul master. */
public final class SoulMasterStats {

    private static final int LEVELS_PER_ROUND = 10;
    private static final int MAX_OPPONENT_LEVEL = 99;
    /** A cultivator gains half a stat point per level, so the total to a level is the triangle of that. */
    private static final double STAT_POINTS_PER_LEVEL_STEP = 0.5;

    private SoulMasterStats() {
    }

    /** Round one fights level 10, each round ten levels more, and the final round level 99. */
    public static int opponentLevelForRound(final int round) {
        return Math.min(MAX_OPPONENT_LEVEL, round * LEVELS_PER_ROUND);
    }

    /**
     * The stat total a cultivator of that level would carry, summing the same per-level gain the
     * player earns in {@link CultivationManager#regularStatIncrease(int)}.
     */
    public static double statPointsForLevel(final int level) {
        return STAT_POINTS_PER_LEVEL_STEP * level * (level + 1) / 2.0;
    }
}
