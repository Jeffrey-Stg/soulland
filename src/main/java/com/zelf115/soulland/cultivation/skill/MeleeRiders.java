package com.zelf115.soulland.cultivation.skill;

import com.zelf115.soulland.SoulLand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Skills that ride on the caster's next melee blows rather than acting at once.
 *
 * <p>Only a plain melee hit counts, so a skill's own damage never sets them off.
 */
public final class MeleeRiders {

    private static final int VENOM_POISON_SECONDS = 5;
    private static final int FROST_FREEZE_SECONDS = 5;
    private static final float TITAN_SMASH_BONUS = 75.0F;

    private MeleeRiders() {
    }

    /** Poison from Venom Coating and a freeze from Frost Coating, when the attacker carries them. */
    public static void applyOnHitEffects(final Player attacker, final LivingEntity victim, final DamageSource source) {
        if (!source.is(DamageTypes.PLAYER_ATTACK)) return;
        final MobEffectInstance venom = attacker.getEffect(SoulLand.SKILL_VENOM_EFFECT);
        if (venom != null) {
            victim.addEffect(new MobEffectInstance(MobEffects.POISON,
                    SkillEffects.ticks(VENOM_POISON_SECONDS), venom.getAmplifier()), attacker);
        }
        if (attacker.hasEffect(SoulLand.SKILL_FROST_EFFECT)) {
            SkillEffects.freeze(victim, SkillEffects.ticks(FROST_FREEZE_SECONDS));
        }
    }

    /** Titan Smash's extra damage, spent on the first melee hit after the cast. */
    public static float consumeSmashBonus(final Player attacker, final DamageSource source) {
        if (!source.is(DamageTypes.PLAYER_ATTACK) || !attacker.hasEffect(SoulLand.SKILL_TITAN_FIST_EFFECT)) return 0.0F;
        attacker.removeEffect(SoulLand.SKILL_TITAN_FIST_EFFECT);
        return TITAN_SMASH_BONUS;
    }
}
