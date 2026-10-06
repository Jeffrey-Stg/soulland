package com.zelf115.soulland.cultivation.skill;

import com.zelf115.soulland.spirit.Affinity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.RandomSource;

/** The skills a soul ring can be born with, by the affinities of the beast it came from. */
public final class SkillPools {

    private static final Map<Affinity, List<Skill>> POOLS = Map.of(
            Affinity.LIGHTNING, List.of(Skill.LIGHTNING_FIELD, Skill.LIGHTNING_SPEAR, Skill.LIGHTNING_STRIKE),
            Affinity.LIGHT, List.of(Skill.LIGHT_RAY, Skill.LIGHT_SHIELD, Skill.LIGHT_EMPOWER),
            Affinity.DARK, List.of(Skill.DARK_RAY, Skill.DARK_SHIELD, Skill.DARK_EMPOWER),
            Affinity.ICE, List.of(Skill.ICE_RAY, Skill.ICE_FIELD),
            Affinity.FIRE, List.of(Skill.FIRE_FIELD, Skill.FLAMETHROWER),
            Affinity.DRAGON, List.of(Skill.DRAGON_CLAW, Skill.DRAGON_SCALE),
            Affinity.POISON, List.of(Skill.POISON_STING, Skill.POISON_FIELD),
            Affinity.SPIRIT, List.of(Skill.SPIRIT_REGEN, Skill.SPIRIT_THORN),
            Affinity.HOLY, List.of(Skill.HEAL),
            Affinity.DEMONIC, List.of(Skill.WITHER));

    /** Affinities that also draw on another affinity's whole pool. */
    private static final Map<Affinity, Affinity> INHERITED_POOLS = Map.of(
            Affinity.HOLY, Affinity.LIGHT,
            Affinity.DEMONIC, Affinity.DARK);

    private SkillPools() {
    }

    /** Whether any skill pool covers these affinities, so a ring from them can carry a skill. */
    public static boolean hasPoolFor(final Set<Affinity> affinities) {
        return !candidatesFor(affinities).isEmpty();
    }

    /** A random skill from every pool the affinities reach, or none when no pool covers them. */
    public static Optional<Skill> roll(final Set<Affinity> affinities, final RandomSource random) {
        return pick(candidatesFor(affinities), random);
    }

    /**
     * A bone always carries a skill: from the pools its beast's affinities reach, or from every pool
     * when none of them has one of its own.
     */
    public static Optional<Skill> rollForBone(final Set<Affinity> affinities, final RandomSource random) {
        final List<Skill> candidates = candidatesFor(affinities);
        return pick(candidates.isEmpty() ? candidatesFor(POOLS.keySet()) : candidates, random);
    }

    private static List<Skill> candidatesFor(final Set<Affinity> affinities) {
        return affinities.stream()
                .flatMap(SkillPools::poolsReachedBy)
                .flatMap(affinity -> POOLS.getOrDefault(affinity, List.of()).stream())
                .distinct()
                .toList();
    }

    private static Optional<Skill> pick(final List<Skill> candidates, final RandomSource random) {
        if (candidates.isEmpty()) return Optional.empty();
        return Optional.of(candidates.get(random.nextInt(candidates.size())));
    }

    private static Stream<Affinity> poolsReachedBy(final Affinity affinity) {
        final Affinity inherited = INHERITED_POOLS.get(affinity);
        return inherited == null ? Stream.of(affinity) : Stream.of(affinity, inherited);
    }
}
