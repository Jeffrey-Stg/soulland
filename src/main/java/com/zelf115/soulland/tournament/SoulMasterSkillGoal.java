package com.zelf115.soulland.tournament;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Lets a soul master fight with its rings: whenever a skill is off cooldown and its target sits in
 * that skill's range, it casts. It claims no movement or look control, so melee chasing carries on.
 */
final class SoulMasterSkillGoal extends Goal {

    /** A pause after every cast, so skills come one at a time rather than in a burst. */
    private static final int GLOBAL_COOLDOWN_TICKS = 40;

    private final SoulMasterEntity master;
    private final Map<SoulMasterSkill, Long> readyAt = new EnumMap<>(SoulMasterSkill.class);
    private long nextCastTick;

    SoulMasterSkillGoal(final SoulMasterEntity master) {
        this.master = master;
        setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        return master.level().getGameTime() >= nextCastTick && readySkill().isPresent();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        final LivingEntity target = master.getTarget();
        readySkill().ifPresent(skill -> cast(skill, target));
    }

    private void cast(final SoulMasterSkill skill, final LivingEntity target) {
        final long now = master.level().getGameTime();
        master.getLookControl().setLookAt(target);
        skill.cast(master, target);
        readyAt.put(skill, now + skill.cooldownTicks());
        nextCastTick = now + GLOBAL_COOLDOWN_TICKS;
        if (target instanceof ServerPlayer challenger) {
            challenger.displayClientMessage(Component.translatable("soulland.tournament.skill.cast",
                    skill.displayName()), true);
        }
    }

    private Optional<SoulMasterSkill> readySkill() {
        final LivingEntity target = master.getTarget();
        if (target == null || !target.isAlive() || !master.hasLineOfSight(target)) {
            return Optional.empty();
        }
        final long now = master.level().getGameTime();
        return Arrays.stream(SoulMasterSkill.values())
                .filter(skill -> now >= readyAt.getOrDefault(skill, 0L))
                .filter(skill -> skill.suits(master, target))
                .findFirst();
    }
}
