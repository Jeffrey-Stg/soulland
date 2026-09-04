package com.zelf115.soulland.cultivation;

/** How a player wants their absorbed soul rings shown. */
public enum RingDisplayMode {
    NONE,
    CURRENT_MARTIAL_SOUL,
    ALL;

    private static final RingDisplayMode[] VALUES = values();

    public static RingDisplayMode byOrdinal(final int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) {
            return NONE;
        }
        return VALUES[ordinal];
    }

    /** The mode after this one, skipping {@link #ALL} for players without a second martial soul. */
    public RingDisplayMode next(final boolean hasSecondMartialSoul) {
        final RingDisplayMode candidate = VALUES[(ordinal() + 1) % VALUES.length];
        if (candidate == ALL && !hasSecondMartialSoul) {
            return NONE;
        }
        return candidate;
    }

    public String translationKey() {
        return "soulland.soul_ring.display." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
