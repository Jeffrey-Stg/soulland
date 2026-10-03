package com.zelf115.soulland.tournament;

import com.zelf115.soulland.cultivation.skill.SkillProjectileEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.joml.Vector3f;

/**
 * What a tournament soul master can do with its rings. Each skill unlocks at a ring count, suits a
 * range band, and recovers on its own cooldown.
 */
enum SoulMasterSkill {

    SPIRIT_BOLT("spirit_bolt", 1, 80, 4.0, 16.0) {
        @Override
        void cast(final SoulMasterEntity master, final LivingEntity target) {
            final double damage = attackDamage(master) * SPIRIT_BOLT_DAMAGE_FACTOR;
            SkillProjectileEntity.launchAt(master, target, SPIRIT_BOLT_TRAIL,
                    hit -> hit.hurt(master.damageSources().mobAttack(master), (float) damage));
        }
    },
    RING_SURGE("ring_surge", 3, 300, 0.0, 16.0) {
        @Override
        void cast(final SoulMasterEntity master, final LivingEntity target) {
            master.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SURGE_TICKS));
            master.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, SURGE_TICKS));
            burst(master, ParticleTypes.ENCHANT, SURGE_PARTICLES);
        }
    },
    SHOCKWAVE("shockwave", 5, 160, 0.0, 4.0) {
        @Override
        void cast(final SoulMasterEntity master, final LivingEntity target) {
            final float damage = (float) attackDamage(master);
            for (final LivingEntity hit : master.level().getEntitiesOfClass(LivingEntity.class,
                    master.getBoundingBox().inflate(SHOCKWAVE_RADIUS), entity -> entity != master)) {
                hit.hurt(master.damageSources().mobAttack(master), damage);
                hit.knockback(SHOCKWAVE_KNOCKBACK, master.getX() - hit.getX(), master.getZ() - hit.getZ());
            }
            burst(master, ParticleTypes.EXPLOSION, SHOCKWAVE_PARTICLES);
        }
    };

    private static final ParticleOptions SPIRIT_BOLT_TRAIL = new DustParticleOptions(new Vector3f(0.55F, 0.8F, 1.0F), 1.2F);
    private static final double SPIRIT_BOLT_DAMAGE_FACTOR = 1.5;
    private static final int SURGE_TICKS = 120;
    private static final int SURGE_PARTICLES = 30;
    private static final double SHOCKWAVE_RADIUS = 4.0;
    private static final double SHOCKWAVE_KNOCKBACK = 1.2;
    private static final int SHOCKWAVE_PARTICLES = 6;

    private final String name;
    private final int ringsNeeded;
    private final int cooldownTicks;
    private final double minRange;
    private final double maxRange;

    SoulMasterSkill(final String name, final int ringsNeeded, final int cooldownTicks,
                    final double minRange, final double maxRange) {
        this.name = name;
        this.ringsNeeded = ringsNeeded;
        this.cooldownTicks = cooldownTicks;
        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    abstract void cast(SoulMasterEntity master, LivingEntity target);

    boolean suits(final SoulMasterEntity master, final LivingEntity target) {
        final double distance = master.distanceTo(target);
        return master.getRingCount() >= ringsNeeded && distance >= minRange && distance <= maxRange;
    }

    int cooldownTicks() {
        return cooldownTicks;
    }

    Component displayName() {
        return Component.translatable("soulland.tournament.skill." + name);
    }

    private static double attackDamage(final SoulMasterEntity master) {
        return master.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private static void burst(final SoulMasterEntity master, final ParticleOptions particle, final int count) {
        if (master.level() instanceof ServerLevel level) {
            level.sendParticles(particle, master.getX(), master.getY(0.5), master.getZ(), count, 0.6, 0.6, 0.6, 0.1);
        }
    }
}
