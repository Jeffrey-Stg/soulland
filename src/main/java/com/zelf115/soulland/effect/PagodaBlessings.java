package com.zelf115.soulland.effect;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * The timed blessings the Glazed Tile Pagoda's rings grant, as effects so the player sees them and
 * their time left. Each raises a share of the whole stat, rings and bones included.
 *
 * <p>Berserk's level is the ring that cast it: I, then Greater (II), then Greatest (III).
 */
public final class PagodaBlessings {

    private static final double STAT_BLESSING_PERCENT = 0.20;
    private static final double[] BERSERK_DAMAGE_PERCENT = {0.30, 0.60, 1.00};
    private static final double[] BERSERK_DEFENSE_PENALTY = {-0.20, -0.35, -0.50};

    private static final int DAMAGE_BLESSING_COLOR = 0xFFD24A;
    private static final int DEFENSE_BLESSING_COLOR = 0x7FD8E8;
    private static final int BERSERK_COLOR = 0xE0412E;

    private PagodaBlessings() {
    }

    public static MobEffect damageBlessing() {
        return new MarkerEffect(MobEffectCategory.BENEFICIAL, DAMAGE_BLESSING_COLOR)
                .addAttributeModifier(Stats.DAMAGE, modifierId("pagoda_damage"), STAT_BLESSING_PERCENT,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    public static MobEffect defenseBlessing() {
        return new MarkerEffect(MobEffectCategory.BENEFICIAL, DEFENSE_BLESSING_COLOR)
                .addAttributeModifier(Stats.DEFENSE, modifierId("pagoda_defense"), STAT_BLESSING_PERCENT,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    public static MobEffect berserk() {
        return new MarkerEffect(MobEffectCategory.NEUTRAL, BERSERK_COLOR)
                .addAttributeModifier(Stats.DAMAGE, modifierId("berserk_damage"),
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, level -> forLevel(BERSERK_DAMAGE_PERCENT, level))
                .addAttributeModifier(Stats.DEFENSE, modifierId("berserk_defense"),
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, level -> forLevel(BERSERK_DEFENSE_PENALTY, level));
    }

    private static double forLevel(final double[] byLevel, final int level) {
        return byLevel[Math.max(0, Math.min(byLevel.length - 1, level))];
    }

    /** Under the {@code effect.} prefix every effect-owned modifier uses, which stat rebuilds leave to the effect. */
    private static ResourceLocation modifierId(final String name) {
        return ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "effect." + name);
    }
}
