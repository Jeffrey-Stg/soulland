package com.zelf115.soulland.cultivation.skill;

import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationManager;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** The building blocks skills are made of. */
public final class SkillEffects {

    private static final double TARGET_RANGE = 30.0;
    private static final double BEAM_RANGE = 20.0;
    private static final double BEAM_PARTICLE_SPACING = 0.4;
    private static final double BEAM_DROP_BELOW_EYE = 0.2;
    private static final int AREA_EDGE_PARTICLES = 48;
    private static final double AREA_EDGE_HEIGHT = 0.2;
    /** Slowness this strong roots a creature in place, which is what freezing and binding mean here. */
    private static final int FREEZE_AMPLIFIER = 19;
    /** Effect levels are saved as an unsigned byte; a higher amplifier makes the holder's save fail. */
    private static final int MAX_AMPLIFIER = 255;

    private SkillEffects() {
    }

    /** The living entity under the player's crosshair, if one is within range and not behind a wall. */
    public static Optional<LivingEntity> lookTarget(final Player player, final double range) {
        final Vec3 eye = player.getEyePosition();
        final Vec3 sightEnd = sightEnd(player, range);
        final AABB searchBox = player.getBoundingBox().expandTowards(player.getViewVector(1.0F).scale(range)).inflate(1.0);
        final EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, sightEnd, searchBox,
                SkillEffects::isTargetable, eye.distanceToSqr(sightEnd));
        if (hit == null) return Optional.empty();
        return Optional.of((LivingEntity) hit.getEntity());
    }

    public static void freeze(final LivingEntity target, final int ticks) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, FREEZE_AMPLIFIER));
    }

    /** Acts on the creature under the crosshair; with nothing in sight the skill does not fire. */
    static boolean atLookTarget(final SkillCast cast, final Consumer<LivingEntity> action) {
        final Optional<LivingEntity> target = lookTargetOrWarn(cast);
        target.ifPresent(action);
        return target.isPresent();
    }

    /** The creature under the crosshair, telling the caster when there is none. */
    static Optional<LivingEntity> lookTargetOrWarn(final SkillCast cast) {
        final Optional<LivingEntity> target = lookTarget(cast.caster(), TARGET_RANGE);
        if (target.isEmpty()) {
            cast.caster().displayClientMessage(Component.translatable("soulland.skill.no_target"), true);
        }
        return target;
    }

    /** A beam from the caster's eyes to whatever they aim at, acting on the first creature in its path. */
    static boolean beam(final SkillCast cast, final ParticleOptions particle, final Consumer<LivingEntity> onHit) {
        final Player caster = cast.caster();
        final Optional<LivingEntity> target = lookTarget(caster, BEAM_RANGE);
        final Vec3 end = target.map(hit -> hit.getBoundingBox().getCenter())
                .orElseGet(() -> sightEnd(caster, BEAM_RANGE));
        drawLine(caster, end, particle);
        target.ifPresent(onHit);
        return true;
    }

    /** Acts on every creature within the radius except the caster, and rings the edge with particles. */
    static boolean area(final SkillCast cast, final double radius, final ParticleOptions edge,
                        final Consumer<LivingEntity> action) {
        final Player caster = cast.caster();
        caster.level().getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(radius),
                        entity -> entity != caster && entity.distanceTo(caster) <= radius)
                .forEach(action);
        ring(cast, radius, edge);
        return true;
    }

    static boolean projectile(final SkillCast cast, final ParticleOptions trail, final Consumer<LivingEntity> onHit) {
        SkillProjectileEntity.launch(cast.caster(), 0.0F, trail, onHit);
        return true;
    }

    /** Several projectiles fanned evenly across the given spread, centred on where the caster looks. */
    static boolean projectileFan(final SkillCast cast, final int count, final float spreadDegrees,
                                 final ParticleOptions trail, final Consumer<LivingEntity> onHit) {
        final float step = count > 1 ? spreadDegrees / (count - 1) : 0.0F;
        final float firstOffset = -spreadDegrees / 2.0F;
        for (int shot = 0; shot < count; shot++) {
            SkillProjectileEntity.launch(cast.caster(), firstOffset + step * shot, trail, onHit);
        }
        return true;
    }

    /** A timed flat stat bonus: one effect level is one stat point, and spirit raises its size. */
    static boolean timedBuff(final SkillCast cast, final Holder<MobEffect> effect, final double amount,
                             final int seconds) {
        final Player caster = cast.caster();
        final int points = (int) Math.round(cast.scaledBySpirit(amount));
        caster.addEffect(new MobEffectInstance(effect, ticks(seconds), amplifierForLevels(points)));
        Stats.syncDerivedPlayerStats(caster, caster.getData(CultivationAttachment.CULTIVATION_DATA.get()));
        return true;
    }

    static void applyStatus(final SkillCast cast, final LivingEntity target, final Holder<MobEffect> effect,
                            final int seconds) {
        target.addEffect(new MobEffectInstance(effect, ticks(seconds)), cast.caster());
    }

    static void healPlayersNear(final SkillCast cast, final double radius, final double amount) {
        final Player caster = cast.caster();
        final float healed = (float) cast.scaledBySpirit(amount);
        caster.level().getEntitiesOfClass(Player.class, caster.getBoundingBox().inflate(radius),
                        player -> player.distanceTo(caster) <= radius)
                .forEach(player -> player.heal(healed));
    }

    static void magicDamage(final SkillCast cast, final LivingEntity target) {
        magicDamage(cast, target, cast.scaledBySpirit(cast.skill().baseDamage()));
    }

    /** Invulnerability frames are cleared so a channel pulse is not swallowed by the last one. */

    static void magicDamage(final SkillCast cast, final LivingEntity target, final double amount) {
        final Player caster = cast.caster();
        target.invulnerableTime = 0;
        target.hurt(caster.damageSources().indirectMagic(caster, caster), (float) amount);
    }

    static void summonHarmlessLightning(final Player caster, final LivingEntity target) {
        final LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(caster.level());
        if (bolt == null) return;
        bolt.moveTo(target.position());
        bolt.setVisualOnly(true);
        caster.level().addFreshEntity(bolt);
    }

    /** The amplifier that gives an effect this many levels, kept within what an effect can save. */
    static int amplifierForLevels(final int levels) {
        return Math.clamp(levels - 1L, 0, MAX_AMPLIFIER);
    }

    static int ticks(final int seconds) {
        return seconds * CultivationManager.TPS;
    }

    static void drawLine(final Player caster, final Vec3 end, final ParticleOptions particle) {
        if (!(caster.level() instanceof ServerLevel level)) return;
        final Vec3 start = caster.getEyePosition().subtract(0.0, BEAM_DROP_BELOW_EYE, 0.0);
        final Vec3 path = end.subtract(start);
        final int steps = Math.max(1, (int) Math.ceil(path.length() / BEAM_PARTICLE_SPACING));
        for (int step = 0; step <= steps; step++) {
            final Vec3 point = start.add(path.scale((double) step / steps));
            level.sendParticles(particle, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    static void ring(final SkillCast cast, final double radius, final ParticleOptions particle) {
        final Player caster = cast.caster();
        if (!(caster.level() instanceof ServerLevel level)) return;
        for (int point = 0; point < AREA_EDGE_PARTICLES; point++) {
            final double angle = 2.0 * Math.PI * point / AREA_EDGE_PARTICLES;
            level.sendParticles(particle,
                    caster.getX() + radius * Math.cos(angle),
                    caster.getY() + AREA_EDGE_HEIGHT,
                    caster.getZ() + radius * Math.sin(angle),
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static Vec3 sightEnd(final Player player, final double range) {
        final Vec3 eye = player.getEyePosition();
        final Vec3 reach = player.getViewVector(1.0F).scale(range);
        return player.level().clip(new ClipContext(eye, eye.add(reach),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
    }

    private static boolean isTargetable(final Entity entity) {
        return entity instanceof LivingEntity && entity.isPickable() && !entity.isSpectator();
    }
}
