package com.zelf115.soulland.tournament;

/**
 * When a tournament run may be taken again.
 *
 * <p>The countdown is wall-clock time stamped into the player's saved data, not a timer held in
 * memory, so a server restart or a logged-out player does not reset or delay it. Every reset
 * decision in the tournament goes through this class, so the period can be redefined in one place.
 */
public final class TournamentClock {

    private static final long REAL_DAY_MILLIS = 86_400_000L;
    private static final long NEVER = 0L;

    private TournamentClock() {
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static boolean isNewPeriod(final long runStartedAt) {
        return runStartedAt == NEVER || now() - runStartedAt >= REAL_DAY_MILLIS;
    }

    public static long millisUntilNextPeriod(final long runStartedAt) {
        return Math.max(0L, runStartedAt + REAL_DAY_MILLIS - now());
    }
}
