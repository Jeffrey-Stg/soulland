package com.zelf115.soulland.cultivation.skill;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.spirit.SpiritBeastEntity;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import java.util.Optional;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

/** The skills only a boss's own soul ring or spirit bones carry. */
public final class BossSkills {

    private static final ParticleOptions SPIRIT_BOLT = new DustParticleOptions(new Vector3f(0.75F, 0.55F, 1.0F), 1.2F);
    private static final ParticleOptions ORCA_BOLT = new DustParticleOptions(new Vector3f(0.6F, 0.05F, 0.1F), 1.2F);

    private static final double DOMAIN_RADIUS = 10.0;
    private static final double BLIZZARD_RADIUS = 5.0;
    private static final int PINCER_SECONDS = 30;
    private static final double PINCER_DAMAGE_POINTS = 50.0;
    private static final int PERMAFROST_FREEZE_SECONDS = 20;
    private static final int ICE_EXPLOSION_SPIRIT_DIVISOR = 5;
    private static final int BLIZZARD_SLOW_SECONDS = 1;
    private static final int BLIZZARD_SLOW_AMPLIFIER = 1;
    private static final int DISPOSSESSION_SHOTS = 3;
    private static final float DISPOSSESSION_SPREAD_DEGREES = 10.0F;
    private static final int SUNDER_SECONDS = 10;
    private static final double SUNDER_POINTS = 20.0;
    private static final int COIL_BIND_SECONDS = 10;
    private static final int TITAN_FIST_SECONDS = 30;
    private static final int GRAVITY_SLOW_SECONDS = 1;
    private static final int GRAVITY_SLOW_AMPLIFIER = 4;
    private static final float ORCA_HEAL = 5.0F;

    private BossSkills() {
    }

    static boolean iceEmpressPincer(final SkillCast cast) {
        SkillEffects.timedBuff(cast, SoulLand.SKILL_DAMAGE_EFFECT, PINCER_DAMAGE_POINTS, PINCER_SECONDS);
        cast.caster().addEffect(new MobEffectInstance(SoulLand.SKILL_FROST_EFFECT, SkillEffects.ticks(PINCER_SECONDS)));
        return true;
    }

    static boolean permafrostDomain(final SkillCast cast) {
        return SkillEffects.area(cast, DOMAIN_RADIUS, ParticleTypes.SNOWFLAKE, target -> {
            SkillEffects.magicDamage(cast, target);
            SkillEffects.freeze(target, SkillEffects.ticks(PERMAFROST_FREEZE_SECONDS));
        });
    }

    /** Turns a fifth of the target's own spirit energy against it; a creature without spirit is untouched. */
    static boolean iceExplosion(final SkillCast cast) {
        final Optional<LivingEntity> target = SkillEffects.lookTargetOrWarn(cast);
        if (target.isEmpty()) return false;
        final double spirit = spiritEnergyOf(target.get());
        if (spirit <= 0.0) {
            cast.caster().sendSystemMessage(Component.translatable("soulland.skill.no_spirit"));
            return false;
        }
        SkillEffects.magicDamage(cast, target.get(), spirit / ICE_EXPLOSION_SPIRIT_DIVISOR);
        return true;
    }

    static boolean iceBearKingBlizzard(final SkillCast cast) {
        return SkillEffects.area(cast, BLIZZARD_RADIUS, ParticleTypes.SNOWFLAKE, target -> {
            SkillEffects.magicDamage(cast, target);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    SkillEffects.ticks(BLIZZARD_SLOW_SECONDS), BLIZZARD_SLOW_AMPLIFIER));
        });
    }

    static boolean spiritualShock(final SkillCast cast) {
        return SkillEffects.atLookTarget(cast, target -> SkillEffects.magicDamage(cast, target));
    }

    static boolean spiritualDispossession(final SkillCast cast) {
        return SkillEffects.projectileFan(cast, DISPOSSESSION_SHOTS, DISPOSSESSION_SPREAD_DEGREES, SPIRIT_BOLT, target -> {
            SkillEffects.magicDamage(cast, target);
            sunder(cast, target);
        });
    }

    static boolean dragonCoil(final SkillCast cast) {
        return SkillEffects.atLookTarget(cast, target -> {
            SkillEffects.freeze(target, SkillEffects.ticks(COIL_BIND_SECONDS));
            SkillEffects.magicDamage(cast, target);
        });
    }

    static boolean skyAzureThunderclap(final SkillCast cast) {
        return SkillEffects.atLookTarget(cast, target -> {
            SkillEffects.summonHarmlessLightning(cast.caster(), target);
            SkillEffects.magicDamage(cast, target);
        });
    }

    static boolean titanSmash(final SkillCast cast) {
        cast.caster().addEffect(new MobEffectInstance(SoulLand.SKILL_TITAN_FIST_EFFECT, SkillEffects.ticks(TITAN_FIST_SECONDS)));
        return true;
    }

    static boolean gravityControl(final SkillCast cast) {
        return SkillEffects.area(cast, DOMAIN_RADIUS, ParticleTypes.PORTAL, target -> target.addEffect(
                new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SkillEffects.ticks(GRAVITY_SLOW_SECONDS), GRAVITY_SLOW_AMPLIFIER)));
    }

    static boolean orcaDevilsAbsorption(final SkillCast cast) {
        return SkillEffects.projectile(cast, ORCA_BOLT, target -> {
            SkillEffects.magicDamage(cast, target);
            cast.caster().heal(ORCA_HEAL);
        });
    }

    /** Lowers the target's defense; spirit raises how much. */
    private static void sunder(final SkillCast cast, final LivingEntity target) {
        final int points = (int) Math.round(cast.scaledBySpirit(SUNDER_POINTS));
        target.addEffect(new MobEffectInstance(SoulLand.SUNDERED_EFFECT, SkillEffects.ticks(SUNDER_SECONDS),
                SkillEffects.amplifierForLevels(points)), cast.caster());
    }

    private static double spiritEnergyOf(final LivingEntity target) {
        if (target instanceof Player player) {
            return player.getData(CultivationAttachment.CULTIVATION_DATA.get()).getSpiritEnergy();
        }
        if (target instanceof SpiritBeastEntity beast) {
            return SpiritBeastManager.getSpiritStat(beast);
        }
        return 0.0;
    }
}
