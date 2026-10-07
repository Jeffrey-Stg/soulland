package com.zelf115.soulland.spirit;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/** Vanilla melee chasing, with a beast's bites spaced 10% further apart than a vanilla mob's. */
public class SpiritBeastMeleeGoal extends MeleeAttackGoal {

    private static final int ATTACK_INTERVAL_TICKS = 30;

    private long nextAttackTick;

    public SpiritBeastMeleeGoal(final PathfinderMob mob) {
        super(mob, 1.0D, false);
    }

    @Override
    protected void resetAttackCooldown() {
        super.resetAttackCooldown();
        nextAttackTick = mob.level().getGameTime() + getAttackInterval();
    }

    @Override
    protected boolean isTimeToAttack() {
        return mob.level().getGameTime() >= nextAttackTick;
    }

    @Override
    protected int getAttackInterval() {
        return adjustedTickDelay(ATTACK_INTERVAL_TICKS);
    }
}
