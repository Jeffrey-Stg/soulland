package com.zelf115.soulland.cultivation.skill;

/** What a skill does when it fires. */
@FunctionalInterface
public interface SkillEffect {

    SkillEffect NONE = cast -> false;

    /** Fires the skill; false when it found nothing to act on, so no spirit energy is spent. */
    boolean tryApply(SkillCast cast);
}
