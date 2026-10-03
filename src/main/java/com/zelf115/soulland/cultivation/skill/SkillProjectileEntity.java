package com.zelf115.soulland.cultivation.skill;

import java.util.function.Consumer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A skill shot that flies straight and does its skill's work on the first creature it hits.
 *
 * <p>Its on-hit work lives only in memory, so it is never saved: a reload simply ends the flight.
 */
public class SkillProjectileEntity extends ThrowableProjectile {

    private static final float SPEED = 2.0F;
    private static final int LIFETIME_TICKS = 60;
    private static final double EYE_OFFSET = 0.1;

    private ParticleOptions trail = ParticleTypes.CRIT;
    private Consumer<LivingEntity> onHit = target -> { };

    public SkillProjectileEntity(final EntityType<? extends SkillProjectileEntity> type, final Level level) {
        super(type, level);
    }

    /** Fires from the caster's eyes along their look, turned sideways by the given yaw offset. */
    public static void launch(final Player caster, final float yawOffset, final ParticleOptions trail,
                              final Consumer<LivingEntity> onHit) {
        final SkillProjectileEntity projectile = prepare(caster, trail, onHit);
        projectile.shootFromRotation(caster, caster.getXRot(), caster.getYRot() + yawOffset, 0.0F, SPEED, 0.0F);
        caster.level().addFreshEntity(projectile);
    }

    /** Fires from the caster's eyes straight at the target's chest, for casters that aim rather than look. */
    public static void launchAt(final LivingEntity caster, final LivingEntity target, final ParticleOptions trail,
                                final Consumer<LivingEntity> onHit) {
        final SkillProjectileEntity projectile = prepare(caster, trail, onHit);
        projectile.shoot(target.getX() - projectile.getX(), target.getY(0.5) - projectile.getY(),
                target.getZ() - projectile.getZ(), SPEED, 0.0F);
        caster.level().addFreshEntity(projectile);
    }

    private static SkillProjectileEntity prepare(final LivingEntity caster, final ParticleOptions trail,
                                                 final Consumer<LivingEntity> onHit) {
        final SkillProjectileEntity projectile = new SkillProjectileEntity(SkillEntities.SKILL_PROJECTILE.get(), caster.level());
        projectile.trail = trail;
        projectile.onHit = onHit;
        projectile.setOwner(caster);
        projectile.setPos(caster.getX(), caster.getEyeY() - EYE_OFFSET, caster.getZ());
        return projectile;
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(trail, getX(), getY(), getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        if (tickCount > LIFETIME_TICKS) discard();
    }

    @Override
    protected void onHitEntity(final EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            onHit.accept(target);
        }
    }

    @Override
    protected void onHit(final HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) discard();
    }
}
