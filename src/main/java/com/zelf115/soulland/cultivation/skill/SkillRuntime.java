package com.zelf115.soulland.cultivation.skill;

import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.effect.SkillBuffEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * A player's skills in motion: the channel or toggle being sustained, and the skill buffs running.
 *
 * <p>Lives only in memory, so a relog or death ends any sustained skill.
 */
public final class SkillRuntime {

    /** A channel or toggle fires four times a second. */
    private static final int PULSE_TICKS = 5;

    private Skill sustainedSkill;
    private long nextPulseTick;
    private long runningBuffCount;

    public void cast(final Player caster, final CultivationData data, final Skill skill) {
        switch (skill.kind()) {
            case INSTANT -> fireOnce(caster, data, skill);
            case CHANNEL -> sustain(caster, data, skill);
            case TOGGLE -> toggle(caster, data, skill);
            case PASSIVE -> caster.sendSystemMessage(Component.translatable("soulland.skill.passive", skill.displayName()));
        }
    }

    /** Ends a channel when its key is let go; toggles ignore the release. */
    public void releaseChannel() {
        if (sustainedSkill != null && sustainedSkill.kind() == SkillKind.CHANNEL) {
            sustainedSkill = null;
        }
    }

    /** Ends whatever channel or toggle is running. */
    public void stopSustainedSkill() {
        sustainedSkill = null;
    }

    public void tick(final Player player, final CultivationData data, final long gameTick) {
        resyncWhenBuffsWearOff(player, data);
        if (sustainedSkill != null && gameTick >= nextPulseTick) {
            pulse(player, data, gameTick);
        }
    }

    private void fireOnce(final Player caster, final CultivationData data, final Skill skill) {
        if (!canAfford(caster, data, skill)) return;
        fire(caster, data, skill);
    }

    /** A held key repeats its press, so a skill already being sustained is left running. */
    private void sustain(final Player caster, final CultivationData data, final Skill skill) {
        if (skill == sustainedSkill) return;
        sustainedSkill = skill;
        pulse(caster, data, caster.level().getGameTime());
    }

    private void toggle(final Player caster, final CultivationData data, final Skill skill) {
        if (skill == sustainedSkill) {
            sustainedSkill = null;
            return;
        }
        sustain(caster, data, skill);
    }

    private void pulse(final Player caster, final CultivationData data, final long gameTick) {
        nextPulseTick = gameTick + PULSE_TICKS;
        if (!canAfford(caster, data, sustainedSkill)) {
            sustainedSkill = null;
            return;
        }
        fire(caster, data, sustainedSkill);
    }

    private static boolean canAfford(final Player caster, final CultivationData data, final Skill skill) {
        if (data.getSpiritEnergy() >= skill.spiritCost()) return true;
        caster.sendSystemMessage(Component.translatable("soulland.skill.no_energy", skill.displayName()));
        return false;
    }

    private static void fire(final Player caster, final CultivationData data, final Skill skill) {
        if (skill.effect().tryApply(new SkillCast(caster, skill))) {
            data.setSpiritEnergy(data.getSpiritEnergy() - skill.spiritCost());
        }
    }

    /** The vanilla stats derived from mod stats only refresh on a sync, so an expired buff must trigger one. */
    private void resyncWhenBuffsWearOff(final Player player, final CultivationData data) {
        final long running = player.getActiveEffects().stream()
                .filter(effect -> effect.getEffect().value() instanceof SkillBuffEffect)
                .count();
        if (running < runningBuffCount) {
            Stats.syncDerivedPlayerStats(player, data);
        }
        runningBuffCount = running;
    }
}
