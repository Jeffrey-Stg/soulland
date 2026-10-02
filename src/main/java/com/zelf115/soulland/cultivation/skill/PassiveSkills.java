package com.zelf115.soulland.cultivation.skill;

import com.zelf115.soulland.cultivation.AbsorbedBone;
import com.zelf115.soulland.cultivation.AbsorbedRing;
import com.zelf115.soulland.cultivation.CultivationData;
import java.util.Optional;
import java.util.stream.Stream;

/** Passive skills work on their own, from the active soul's rings and from every absorbed bone. */
public final class PassiveSkills {

    private static final double SPIRIT_REGEN_BONUS = 0.25;

    private PassiveSkills() {
    }

    public static double spiritRegenMultiplier(final CultivationData data) {
        return owns(data, Skill.SPIRIT_REGEN) ? 1.0 + SPIRIT_REGEN_BONUS : 1.0;
    }

    private static boolean owns(final CultivationData data, final Skill skill) {
        final Stream<Optional<Skill>> ringSkills = data.getRings(data.getActiveSoulSlot()).stream().map(AbsorbedRing::skill);
        final Stream<Optional<Skill>> boneSkills = data.getSpiritBones().values().stream().map(AbsorbedBone::skill);
        return Stream.concat(ringSkills, boneSkills).anyMatch(owned -> owned.equals(Optional.of(skill)));
    }
}
