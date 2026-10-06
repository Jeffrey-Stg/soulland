package com.zelf115.soulland.cultivation.skill;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.network.chat.Component;

/**
 * Every skill a soul ring or spirit bone can carry.
 *
 * <p>Costs and damage are per firing; a channel or toggle fires once every pulse.
 */
public enum Skill {
    LIGHTNING_FIELD(SkillKind.INSTANT, 20.0, 10.0, AffinitySkills::lightningField),
    LIGHTNING_SPEAR(SkillKind.INSTANT, 15.0, 10.0, AffinitySkills::lightningSpear),
    LIGHTNING_STRIKE(SkillKind.INSTANT, 15.0, 15.0, AffinitySkills::lightningStrike),
    LIGHT_RAY(SkillKind.CHANNEL, 10.0, 2.0, AffinitySkills::lightRay),
    LIGHT_SHIELD(SkillKind.INSTANT, Costs.UNLISTED, 0.0, AffinitySkills::shield),
    LIGHT_EMPOWER(SkillKind.INSTANT, Costs.UNLISTED, 0.0, AffinitySkills::empower),
    DARK_RAY(SkillKind.CHANNEL, 10.0, 2.0, AffinitySkills::darkRay),
    DARK_SHIELD(SkillKind.INSTANT, Costs.UNLISTED, 0.0, AffinitySkills::shield),
    DARK_EMPOWER(SkillKind.INSTANT, Costs.UNLISTED, 0.0, AffinitySkills::empower),
    ICE_RAY(SkillKind.CHANNEL, 12.0, 2.0, AffinitySkills::iceRay),
    ICE_FIELD(SkillKind.INSTANT, 20.0, 5.0, AffinitySkills::iceField),
    FIRE_FIELD(SkillKind.INSTANT, 20.0, 5.0, AffinitySkills::fireField),
    FLAMETHROWER(SkillKind.CHANNEL, 12.0, 5.0, AffinitySkills::flamethrower),
    DRAGON_CLAW(SkillKind.INSTANT, Costs.UNLISTED, 0.0, AffinitySkills::dragonClaw),
    DRAGON_SCALE(SkillKind.INSTANT, Costs.UNLISTED, 0.0, AffinitySkills::dragonScale),
    POISON_STING(SkillKind.INSTANT, Costs.UNLISTED, 0.0, AffinitySkills::poisonSting),
    POISON_FIELD(SkillKind.INSTANT, Costs.UNLISTED, 0.0, AffinitySkills::poisonField),
    SPIRIT_REGEN(SkillKind.PASSIVE, 0.0, 0.0, SkillEffect.NONE),
    SPIRIT_THORN(SkillKind.INSTANT, 20.0, 5.0, AffinitySkills::spiritThorn),
    HEAL(SkillKind.INSTANT, 20.0, 0.0, AffinitySkills::heal),
    WITHER(SkillKind.INSTANT, Costs.UNLISTED, 0.0, AffinitySkills::wither),
    ICE_EMPRESS_PINCER(SkillKind.INSTANT, Costs.UNLISTED, 0.0, BossSkills::iceEmpressPincer),
    PERMAFROST_DOMAIN(SkillKind.INSTANT, Costs.UNLISTED, 20.0, BossSkills::permafrostDomain),
    ICE_EXPLOSION(SkillKind.INSTANT, Costs.UNLISTED, 0.0, BossSkills::iceExplosion),
    ICE_BEAR_KING_BLIZZARD(SkillKind.CHANNEL, 25.0, 10.0, BossSkills::iceBearKingBlizzard),
    SPIRITUAL_SHOCK(SkillKind.INSTANT, 20.0, 20.0, BossSkills::spiritualShock),
    SPIRITUAL_DISPOSSESSION(SkillKind.INSTANT, 40.0, 20.0, BossSkills::spiritualDispossession),
    DRAGON_COIL(SkillKind.INSTANT, 30.0, 50.0, BossSkills::dragonCoil),
    SKY_AZURE_THUNDERCLAP(SkillKind.INSTANT, 50.0, 75.0, BossSkills::skyAzureThunderclap),
    TITAN_SMASH(SkillKind.INSTANT, 50.0, 0.0, BossSkills::titanSmash),
    GRAVITY_CONTROL(SkillKind.TOGGLE, 1.0, 0.0, BossSkills::gravityControl),
    ORCA_DEVILS_ABSORPTION(SkillKind.INSTANT, 20.0, 5.0, BossSkills::orcaDevilsAbsorption);

    /** Held apart from the enum body, which may not name its own constants while building its values. */
    private static final class Costs {
        /** Stand-in for skills whose design gives no spirit cost yet. */
        private static final double UNLISTED = 15.0;
    }

    private final SkillKind kind;
    private final double spiritCost;
    private final double baseDamage;
    private final SkillEffect effect;

    Skill(final SkillKind kind, final double spiritCost, final double baseDamage, final SkillEffect effect) {
        this.kind = kind;
        this.spiritCost = spiritCost;
        this.baseDamage = baseDamage;
        this.effect = effect;
    }

    public static Optional<Skill> byName(final String name) {
        return Arrays.stream(values()).filter(skill -> skill.name().equals(name)).findFirst();
    }

    public SkillKind kind() {
        return kind;
    }

    public boolean isPassive() {
        return kind == SkillKind.PASSIVE;
    }

    public double spiritCost() {
        return spiritCost;
    }

    public double baseDamage() {
        return baseDamage;
    }

    public SkillEffect effect() {
        return effect;
    }

    public Component displayName() {
        return Component.translatable(translationKey());
    }

    public String translationKey() {
        return "soulland.skill." + name().toLowerCase(Locale.ROOT);
    }
}
