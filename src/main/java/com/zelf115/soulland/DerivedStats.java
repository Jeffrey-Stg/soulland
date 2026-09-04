package com.zelf115.soulland;

/**
 * Converts the five cultivation stats into the vanilla attribute values they drive.
 *
 * <p>This is the conversion table, kept in one place so players, spirit beasts
 * and anything else that gains stats stay on identical maths.
 */
public final class DerivedStats {

    private static final double HEALTH_POINTS_PER_HITPOINT = 20.0;
    private static final double DAMAGE_POINTS_PER_DAMAGE = 20.0;
    private static final double DEFENSE_POINTS_PER_ARMOR = 50.0;
    private static final double SPEED_POINTS_PER_MOVE_PERCENT = 25.0;
    private static final double SPIRIT_POINTS_PER_ENERGY = 10.0;
    private static final double POINTS_PER_BONUS_PERCENT = 100.0;
    private static final double PERCENT = 100.0;
    /** Defense would otherwise reach total immunity; the cap keeps high-tier fights winnable. */
    private static final double MAX_DAMAGE_REDUCTION = 0.90;

    private DerivedStats() {
    }

    public static double maxHealth(final double baseMaxHealth, final double healthStat) {
        return (baseMaxHealth + healthStat / HEALTH_POINTS_PER_HITPOINT) * (1.0 + bonusFraction(healthStat));
    }

    public static double attackDamage(final double baseAttackDamage, final double damageStat) {
        return baseAttackDamage + damageStat / DAMAGE_POINTS_PER_DAMAGE;
    }

    public static double armor(final double baseArmor, final double defenseStat) {
        return baseArmor + defenseStat / DEFENSE_POINTS_PER_ARMOR;
    }

    public static double movementSpeed(final double baseMovementSpeed, final double speedStat) {
        return baseMovementSpeed * (1.0 + speedStat / (SPEED_POINTS_PER_MOVE_PERCENT * PERCENT));
    }

    public static double attackSpeed(final double baseAttackSpeed, final double speedStat) {
        return baseAttackSpeed * (1.0 + bonusFraction(speedStat));
    }

    // ---- Inverses, for seeding an entity whose vanilla attributes came first ----

    public static double healthStatFor(final double hitpoints) {
        return hitpoints * HEALTH_POINTS_PER_HITPOINT;
    }

    public static double damageStatFor(final double damage) {
        return damage * DAMAGE_POINTS_PER_DAMAGE;
    }

    public static double defenseStatFor(final double armor) {
        return armor * DEFENSE_POINTS_PER_ARMOR;
    }

    public static double speedStatFor(final double movementSpeed) {
        return movementSpeed * SPEED_POINTS_PER_MOVE_PERCENT * PERCENT;
    }

    public static double outgoingDamage(final double baseDamage, final double damageStat) {
        return baseDamage * (1.0 + bonusFraction(damageStat));
    }

    public static float reducedIncomingDamage(final float incomingDamage, final double defenseStat) {
        final double reduction = Math.min(MAX_DAMAGE_REDUCTION, bonusFraction(defenseStat));
        return (float) (incomingDamage * (1.0 - reduction));
    }

    public static double maxSpiritEnergy(final double spiritStat) {
        return Math.max(0.0, spiritStat / SPIRIT_POINTS_PER_ENERGY);
    }

    public static double spiritEnergyRegenPerSecond(final double spiritStat) {
        return maxSpiritEnergy(spiritStat) * bonusFraction(spiritStat);
    }

    private static double bonusFraction(final double statValue) {
        return statValue / (POINTS_PER_BONUS_PERCENT * PERCENT);
    }
}
