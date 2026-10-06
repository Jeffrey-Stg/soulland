package com.zelf115.soulland.cultivation.technique;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.StatBonus;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.cultivation.skill.SkillEffects;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Purple Demon Eye: Survey (timed glow pulse), Detailed (toggle), Surreal (free, plus a paralysing
 * strike) and Boundless (+100 Spirit). The open eye sees in the dark, and time spent seeing at dawn
 * trains it twice as fast.
 */
public final class PurpleDemonEye {

    private static final Technique EYE = Technique.PURPLE_DEMON_EYE;
    private static final int DETAILED_LEVEL = 2;
    private static final int SURREAL_LEVEL = 3;
    private static final int BOUNDLESS_LEVEL = 4;
    private static final int TPS = CultivationManager.TPS;
    private static final int PULSE_DURATION_TICKS = 10 * TPS;
    private static final int GLOW_DURATION_TICKS = 30;
    /** Refreshed every second; under 200 ticks the vanilla night vision screen starts to flicker. */
    private static final int NIGHT_VISION_TICKS = 220;
    private static final double SURVEY_RADIUS = 20.0;
    private static final double ENERGY_COST_PER_SECOND = 0.5;
    private static final double STRIKE_RANGE = 20.0;
    private static final float STRIKE_DAMAGE = 5.0F;
    private static final int PARALYSIS_TICKS = 5 * TPS;
    private static final int STRIKE_COOLDOWN_TICKS = 3 * TPS;
    private static final long DAY_LENGTH_TICKS = 24000L;
    private static final long DAWN_START_TICK = 23000L;
    private static final long DAWN_END_TICK = 1000L;
    private static final int DAWN_TRAINING_MULTIPLIER = 2;
    private static final double BOUNDLESS_SPIRIT = 100.0;
    private static final ResourceLocation BOUNDLESS_BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "technique_boundless");

    private PurpleDemonEye() {
    }

    public static void use(final Player player, final CultivationData data) {
        final LearnedTechniques techniques = data.getTechniques();
        if (!techniques.isLearned(EYE)) {
            player.displayClientMessage(Component.translatable("soulland.technique.not_learned", EYE.displayName()), true);
            return;
        }
        if (techniques.isDemonEyeOpen()) {
            close(player, techniques);
            return;
        }
        if (isCostly(techniques) && data.getSpiritEnergy() < ENERGY_COST_PER_SECOND) {
            player.displayClientMessage(Component.translatable("soulland.technique.no_energy"), true);
            return;
        }
        open(player, techniques);
    }

    private static void open(final Player player, final LearnedTechniques techniques) {
        if (techniques.level(EYE) >= DETAILED_LEVEL) {
            techniques.setDemonEyeOpen(true);
            player.displayClientMessage(Component.translatable("soulland.technique.demon_eye.opened"), true);
            return;
        }
        techniques.setDemonEyePulseUntil(player.level().getGameTime() + PULSE_DURATION_TICKS);
        player.displayClientMessage(Component.translatable("soulland.technique.demon_eye.pulse"), true);
    }

    private static void close(final Player player, final LearnedTechniques techniques) {
        techniques.setDemonEyeOpen(false);
        techniques.setDemonEyePulseUntil(0L);
        endNightVision(player);
        player.displayClientMessage(Component.translatable("soulland.technique.demon_eye.closed"), true);
    }

    public static void tick(final Player player, final CultivationData data, final long gameTick) {
        if (gameTick % TPS != 0) return;
        final LearnedTechniques techniques = data.getTechniques();
        syncBoundlessSpirit(player, techniques);
        if (!isSeeing(techniques, gameTick)) return;
        if (isCostly(techniques) && !payUpkeep(data)) {
            close(player, techniques);
            return;
        }

        revealNearbyMobs(player);
        grantNightVision(player);
        TechniqueTraining.train(player, techniques, EYE, trainingPerSecond(player));
    }

    private static void grantNightVision(final Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_TICKS, 0, true, false));
    }

    /** Leaves alone a longer night vision the player drank, which outlasts anything the eye grants. */
    private static void endNightVision(final Player player) {
        final MobEffectInstance nightVision = player.getEffect(MobEffects.NIGHT_VISION);
        if (nightVision != null && nightVision.getDuration() <= NIGHT_VISION_TICKS) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    private static boolean isSeeing(final LearnedTechniques techniques, final long gameTick) {
        return techniques.isDemonEyeOpen() || gameTick < techniques.getDemonEyePulseUntil();
    }

    private static boolean isCostly(final LearnedTechniques techniques) {
        return techniques.level(EYE) < SURREAL_LEVEL;
    }

    private static boolean payUpkeep(final CultivationData data) {
        if (data.getSpiritEnergy() < ENERGY_COST_PER_SECOND) return false;
        data.setSpiritEnergy(data.getSpiritEnergy() - ENERGY_COST_PER_SECOND);
        return true;
    }

    private static void revealNearbyMobs(final Player player) {
        final double radiusSqr = SURVEY_RADIUS * SURVEY_RADIUS;
        player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(SURVEY_RADIUS),
                        mob -> mob.isAlive() && player.distanceToSqr(mob) <= radiusSqr)
                .forEach(mob -> mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION_TICKS, 0, false, false)));
    }

    private static long trainingPerSecond(final Player player) {
        return isDawn(player) ? (long) TPS * DAWN_TRAINING_MULTIPLIER : TPS;
    }

    private static boolean isDawn(final Player player) {
        final long timeOfDay = player.level().getDayTime() % DAY_LENGTH_TICKS;
        return timeOfDay >= DAWN_START_TICK || timeOfDay < DAWN_END_TICK;
    }

    /** Keeps the Boundless +Spirit modifier in step with the eye's level, whether or not it is open. */
    private static void syncBoundlessSpirit(final Player player, final LearnedTechniques techniques) {
        final AttributeInstance spirit = player.getAttribute(Stats.SPIRIT);
        if (spirit == null) return;

        final boolean earned = techniques.level(EYE) >= BOUNDLESS_LEVEL;
        final boolean granted = spirit.hasModifier(BOUNDLESS_BONUS_ID);
        if (earned && !granted) {
            Stats.applyBonus(player, BOUNDLESS_BONUS_ID, new StatBonus(0, 0, 0, 0, BOUNDLESS_SPIRIT, 0));
        } else if (!earned && granted) {
            spirit.removeModifier(BOUNDLESS_BONUS_ID);
        }
    }

    public static void strike(final Player player, final CultivationData data) {
        final LearnedTechniques techniques = data.getTechniques();
        if (!techniques.isLearned(EYE) || techniques.level(EYE) < SURREAL_LEVEL) {
            player.displayClientMessage(Component.translatable("soulland.technique.demon_eye.strike_locked"), true);
            return;
        }
        final long now = player.level().getGameTime();
        if (now < techniques.getDemonEyeStrikeReadyAt()) return;

        final Optional<LivingEntity> target = SkillEffects.lookTarget(player, STRIKE_RANGE);
        if (target.isEmpty()) {
            player.displayClientMessage(Component.translatable("soulland.technique.demon_eye.no_target"), true);
            return;
        }
        paralyse(player, target.get());
        techniques.setDemonEyeStrikeReadyAt(now + STRIKE_COOLDOWN_TICKS);
    }

    private static void paralyse(final Player player, final LivingEntity target) {
        target.hurt(player.damageSources().indirectMagic(player, player), STRIKE_DAMAGE);
        SkillEffects.freeze(target, PARALYSIS_TICKS);
    }
}
