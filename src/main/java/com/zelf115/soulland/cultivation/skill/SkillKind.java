package com.zelf115.soulland.cultivation.skill;

/** How a skill is driven once its key is pressed. */
public enum SkillKind {
    /** Fires once per press. */
    INSTANT,
    /** Fires every pulse for as long as the cast key is held. */
    CHANNEL,
    /** Fires every pulse from one press until the next. */
    TOGGLE,
    /** Always in effect while owned; never cast and skipped when cycling. */
    PASSIVE
}
