package com.zelf115.soulland.cultivation.technique;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Adds progress to a technique and tells the player when it reaches a new level. */
public final class TechniqueTraining {

    private TechniqueTraining() {
    }

    public static void train(final Player player, final LearnedTechniques techniques, final Technique technique,
                             final long amount) {
        final int levelBefore = techniques.level(technique);
        techniques.addProgress(technique, amount);
        final int levelAfter = techniques.level(technique);
        if (levelAfter > levelBefore) {
            player.sendSystemMessage(Component.translatable("soulland.technique.level_up",
                    technique.displayName(), levelAfter));
        }
    }
}
