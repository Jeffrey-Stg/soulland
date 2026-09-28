package com.zelf115.soulland.cultivation.technique;

import com.zelf115.soulland.cultivation.CultivationManager;
import java.util.Locale;
import net.minecraft.network.chat.Component;

/**
 * A technique taught by a technique book. It levels up as its progress passes each cumulative
 * threshold: ticks of use for the Demon Eye, uses for Shadow Step, ticks meditated for the Haven.
 */
public enum Technique {
    PURPLE_DEMON_EYE(1, minutes(5), minutes(10), minutes(30)),
    GHOSTLY_SHADOW_STEP(1, 50, 200),
    MYSTERIOUS_HAVEN(0, minutes(5), minutes(15), minutes(30), minutes(50), minutes(75),
            minutes(105), minutes(140), minutes(180), minutes(225));

    private static final int PERCENT = 100;

    private final int startingLevel;
    private final long[] cumulativeThresholds;

    Technique(final int startingLevel, final long... cumulativeThresholds) {
        this.startingLevel = startingLevel;
        this.cumulativeThresholds = cumulativeThresholds;
    }

    private static long minutes(final long count) {
        return count * CultivationManager.TICKS_PER_MINUTE;
    }

    public int levelFor(final long progress) {
        int level = startingLevel;
        for (final long threshold : cumulativeThresholds) {
            if (progress >= threshold) level++;
        }
        return level;
    }

    public int minLevel() {
        return startingLevel;
    }

    public int maxLevel() {
        return startingLevel + cumulativeThresholds.length;
    }

    public long progressForLevel(final int level) {
        final int thresholdIndex = level - startingLevel - 1;
        if (thresholdIndex < 0) return 0L;
        return cumulativeThresholds[Math.min(thresholdIndex, cumulativeThresholds.length - 1)];
    }

    public int percentToNextLevel(final long progress) {
        final int level = levelFor(progress);
        if (level >= maxLevel()) return PERCENT;

        final long levelStart = progressForLevel(level);
        final long nextLevelStart = progressForLevel(level + 1);
        return (int) (PERCENT * (progress - levelStart) / (nextLevelStart - levelStart));
    }

    public Component displayName() {
        return Component.translatable("soulland.technique." + name().toLowerCase(Locale.ROOT));
    }
}
