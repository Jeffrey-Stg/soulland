package com.zelf115.soulland.cultivation.skill;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.joml.Vector3f;

/** The skills a soul ring can draw from its beast's affinity pools. */
public final class AffinitySkills {

    private static final float BEAM_PARTICLE_SCALE = 1.2F;
    private static final ParticleOptions LIGHT_BEAM = new DustParticleOptions(new Vector3f(1.0F, 0.9F, 0.25F), BEAM_PARTICLE_SCALE);
    private static final ParticleOptions DARK_BEAM = new DustParticleOptions(new Vector3f(0.45F, 0.1F, 0.6F), BEAM_PARTICLE_SCALE);
    private static final ParticleOptions ICE_BEAM = new DustParticleOptions(new Vector3f(0.55F, 0.85F, 1.0F), BEAM_PARTICLE_SCALE);
    private static final ParticleOptions POISON_MIST = new DustParticleOptions(new Vector3f(0.3F, 0.8F, 0.1F), BEAM_PARTICLE_SCALE);

    private static final double FIELD_RADIUS = 5.0;
    private static final int BUFF_SECONDS = 10;
    private static final double SHIELD_POINTS = 20.0;
    private static final double EMPOWER_POINTS = 20.0;
    private static final double DRAGON_POINTS = 30.0;
    private static final int ICE_RAY_SLOW_SECONDS = 1;
    private static final int ICE_RAY_SLOW_AMPLIFIER = 1;
    private static final int FREEZE_SECONDS = 10;
    private static final int BURN_SECONDS = 5;
    private static final int VENOM_SECONDS = 10;
    private static final int POISON_FIELD_SECONDS = 10;
    private static final int WITHER_SECONDS = 10;
    private static final double HEAL_AMOUNT = 2.0;

    private AffinitySkills() {
    }

    static boolean lightningStrike(final SkillCast cast) {
        return SkillEffects.atLookTarget(cast, target -> {
            SkillEffects.summonHarmlessLightning(cast.caster(), target);
            SkillEffects.magicDamage(cast, target);
        });
    }

    static boolean lightningField(final SkillCast cast) {
        return SkillEffects.area(cast, FIELD_RADIUS, ParticleTypes.ELECTRIC_SPARK, target -> SkillEffects.magicDamage(cast, target));
    }

    static boolean lightningSpear(final SkillCast cast) {
        return SkillEffects.projectile(cast, ParticleTypes.ELECTRIC_SPARK, target -> SkillEffects.magicDamage(cast, target));
    }

    static boolean lightRay(final SkillCast cast) {
        return SkillEffects.beam(cast, LIGHT_BEAM, target -> SkillEffects.magicDamage(cast, target));
    }

    static boolean darkRay(final SkillCast cast) {
        return SkillEffects.beam(cast, DARK_BEAM, target -> SkillEffects.magicDamage(cast, target));
    }

    static boolean iceRay(final SkillCast cast) {
        return SkillEffects.beam(cast, ICE_BEAM, target -> {
            SkillEffects.magicDamage(cast, target);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    SkillEffects.ticks(ICE_RAY_SLOW_SECONDS), ICE_RAY_SLOW_AMPLIFIER));
        });
    }

    static boolean flamethrower(final SkillCast cast) {
        return SkillEffects.beam(cast, ParticleTypes.FLAME, target -> {
            SkillEffects.magicDamage(cast, target);
            target.igniteForSeconds(BURN_SECONDS);
        });
    }

    static boolean shield(final SkillCast cast) {
        return SkillEffects.timedBuff(cast, SoulLand.SKILL_DEFENSE_EFFECT, SHIELD_POINTS, BUFF_SECONDS);
    }

    static boolean empower(final SkillCast cast) {
        return SkillEffects.timedBuff(cast, SoulLand.SKILL_DAMAGE_EFFECT, EMPOWER_POINTS, BUFF_SECONDS);
    }

    static boolean dragonClaw(final SkillCast cast) {
        return SkillEffects.timedBuff(cast, SoulLand.SKILL_DAMAGE_EFFECT, DRAGON_POINTS, BUFF_SECONDS);
    }

    static boolean dragonScale(final SkillCast cast) {
        return SkillEffects.timedBuff(cast, SoulLand.SKILL_DEFENSE_EFFECT, DRAGON_POINTS, BUFF_SECONDS);
    }

    static boolean iceField(final SkillCast cast) {
        return SkillEffects.area(cast, FIELD_RADIUS, ParticleTypes.SNOWFLAKE, target -> {
            SkillEffects.magicDamage(cast, target);
            SkillEffects.freeze(target, SkillEffects.ticks(FREEZE_SECONDS));
        });
    }

    static boolean fireField(final SkillCast cast) {
        return SkillEffects.area(cast, FIELD_RADIUS, ParticleTypes.FLAME, target -> {
            SkillEffects.magicDamage(cast, target);
            target.igniteForSeconds(BURN_SECONDS);
        });
    }

    static boolean poisonSting(final SkillCast cast) {
        SkillEffects.applyStatus(cast, cast.caster(), SoulLand.SKILL_VENOM_EFFECT, VENOM_SECONDS);
        return true;
    }

    static boolean poisonField(final SkillCast cast) {
        return SkillEffects.area(cast, FIELD_RADIUS, POISON_MIST,
                target -> SkillEffects.applyStatus(cast, target, MobEffects.POISON, POISON_FIELD_SECONDS));
    }

    static boolean spiritThorn(final SkillCast cast) {
        return SkillEffects.atLookTarget(cast, target -> SkillEffects.magicDamage(cast, target));
    }

    static boolean heal(final SkillCast cast) {
        SkillEffects.healPlayersNear(cast, FIELD_RADIUS, HEAL_AMOUNT);
        SkillEffects.ring(cast, FIELD_RADIUS, ParticleTypes.HEART);
        return true;
    }

    static boolean wither(final SkillCast cast) {
        return SkillEffects.area(cast, FIELD_RADIUS, ParticleTypes.SMOKE,
                target -> SkillEffects.applyStatus(cast, target, MobEffects.WITHER, WITHER_SECONDS));
    }
}
