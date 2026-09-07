package com.zelf115.soulland.cultivation;

/** How a player wants their absorbed soul rings shown: hidden, one soul's track, or both. */
public enum RingDisplayMode {
    NONE,
    PRIMARY,
    SECONDARY,
    ALL;

    private static final RingDisplayMode[] VALUES = values();

    public static RingDisplayMode byOrdinal(final int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) {
            return NONE;
        }
        return VALUES[ordinal];
    }

    /** The mode after this one, skipping {@link #SECONDARY} and {@link #ALL} without a second soul. */
    public RingDisplayMode next(final boolean hasSecondMartialSoul) {
        final RingDisplayMode candidate = VALUES[(ordinal() + 1) % VALUES.length];
        if (!hasSecondMartialSoul && (candidate == SECONDARY || candidate == ALL)) {
            return NONE;
        }
        return candidate;
    }

    public String translationKey() {
        return "soulland.soul_ring.display." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
