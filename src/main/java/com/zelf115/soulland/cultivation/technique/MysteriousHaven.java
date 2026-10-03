package com.zelf115.soulland.cultivation.technique;

import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import net.minecraft.world.entity.player.Player;

/** Mysterious Haven: a passive technique that speeds up meditation and trains by meditating. */
public final class MysteriousHaven {

    private static final Technique HAVEN = Technique.MYSTERIOUS_HAVEN;
    private static final double BONUS_PER_LEVEL = 0.10;
    private static final double FINAL_LEVEL_BONUS = 0.20;

    private MysteriousHaven() {
    }

    public static double cultivationMultiplier(final CultivationData data) {
        final LearnedTechniques techniques = data.getTechniques();
        if (!techniques.isLearned(HAVEN)) return 1.0;
        return 1.0 + bonusAt(techniques.level(HAVEN));
    }

    private static double bonusAt(final int level) {
        if (level < HAVEN.maxLevel()) return level * BONUS_PER_LEVEL;
        return (level - 1) * BONUS_PER_LEVEL + FINAL_LEVEL_BONUS;
    }

    /** Called once per meditation XP grant. */
    public static void trainWhileMeditating(final Player player, final CultivationData data) {
        TechniqueTraining.train(player, data.getTechniques(), HAVEN, CultivationManager.MEDITATION_TICK_INTERVAL);
    }
}
