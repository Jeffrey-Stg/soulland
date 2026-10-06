package com.zelf115.soulland.cultivation.technique;

import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Ghostly Trace Shadow Steps: a forward dash that never passes through blocks. */
public final class ShadowStep {

    private static final Technique STEP = Technique.GHOSTLY_SHADOW_STEP;
    private static final int FAR_STEP_LEVEL = 2;
    private static final int VANISHING_STEP_LEVEL = 3;
    private static final double SHORT_DISTANCE = 5.0;
    private static final double LONG_DISTANCE = 10.0;
    private static final double PATH_CHECK_INCREMENT = 0.5;
    private static final int TPS = CultivationManager.TPS;
    private static final int BASE_COOLDOWN_TICKS = 5 * TPS;
    private static final int VANISHING_COOLDOWN_TICKS = 2 * TPS;
    private static final int INVISIBILITY_TICKS = 3 * TPS;

    private ShadowStep() {
    }

    public static void use(final Player player, final CultivationData data) {
        final LearnedTechniques techniques = data.getTechniques();
        if (!techniques.isLearned(STEP)) {
            player.displayClientMessage(Component.translatable("soulland.technique.not_learned", STEP.displayName()), true);
            return;
        }
        final long now = player.level().getGameTime();
        if (now < techniques.getShadowStepReadyAt()) return;

        final int level = techniques.level(STEP);
        player.stopRiding();
        final Vec3 destination = farthestClearPoint(player, level >= FAR_STEP_LEVEL ? LONG_DISTANCE : SHORT_DISTANCE);
        player.teleportTo(destination.x, destination.y, destination.z);
        player.resetFallDistance();
        if (level >= VANISHING_STEP_LEVEL) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, INVISIBILITY_TICKS, 0, false, false));
        }
        techniques.setShadowStepReadyAt(now + (level >= VANISHING_STEP_LEVEL ? VANISHING_COOLDOWN_TICKS : BASE_COOLDOWN_TICKS));
        TechniqueTraining.train(player, techniques, STEP, 1);
    }

    private static Vec3 farthestClearPoint(final Player player, final double distance) {
        final Vec3 look = player.getLookAngle();
        final Vec3 heading = new Vec3(look.x, 0.0, look.z);
        final Vec3 origin = player.position();
        if (heading.lengthSqr() == 0.0) return origin;

        final Vec3 direction = heading.normalize();
        Vec3 clear = origin;
        for (double travelled = PATH_CHECK_INCREMENT; travelled <= distance; travelled += PATH_CHECK_INCREMENT) {
            final Vec3 offset = direction.scale(travelled);
            if (!player.level().noCollision(player, player.getBoundingBox().move(offset))) break;
            clear = origin.add(offset);
        }
        return clear;
    }
}
