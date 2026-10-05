package com.zelf115.soulland;

/**
 * Converts the five cultivation stats into the vanilla attribute values they drive.
 *
 * <p>This is the conversion table, kept in one place so players, spirit beasts
 * and anything else that gains stats stay on identical maths.
 */
public final class DerivedStats {

    private static final double HEALTH_POINTS_PER_HITPOINT = 100.0;
    private static final double DAMAGE_POINTS_PER_DAMAGE = 100.0;
    private static final double DEFENSE_POINTS_PER_ARMOR = 250.0;
    private static final double SPEED_POINTS_PER_MOVE_PERCENT = 125.0;
    private static final double SPIRIT_POINTS_PER_ENERGY = 10.0;
    private static final double SPIRIT_POINTS_PER_SKILL_POWER = 200.0;
    private static final double SPIRIT_ENERGY_REGEN_FRACTION_PER_SECOND = 0.02;
    private static final double PERCENT = 100.0;

    private DerivedStats() {
    }

    public static double maxHealth(final double baseMaxHealth, final double healthStat) {
        return baseMaxHealth + healthStat / HEALTH_POINTS_PER_HITPOINT;
    }

    public static double attackDamage(final double baseAttackDamage, final double damageStat) {
        return baseAttackDamage + damageStat / DAMAGE_POINTS_PER_DAMAGE;
    }

    public static double armor(final double baseArmor, final double defenseStat) {
        return baseArmor + armorFor(defenseStat);
    }

    public static double armorFor(final double defenseStat) {
        return defenseStat / DEFENSE_POINTS_PER_ARMOR;
    }

    public static double movementSpeed(final double baseMovementSpeed, final double speedStat) {
        return baseMovementSpeed * (1.0 + speedStat / (SPEED_POINTS_PER_MOVE_PERCENT * PERCENT));
    }

    public static double swimSpeed(final double baseSwimSpeed, final double speedStat) {
        return baseSwimSpeed * (1.0 + speedStat / (SPEED_POINTS_PER_MOVE_PERCENT * PERCENT));
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

    /** Skill damage, heals and buff sizes grow linearly with spirit, the way melee damage grows with the damage stat. */
    public static double scaledBySpirit(final double value, final double spiritStat) {
        return value * (1.0 + Math.max(0.0, spiritStat) / SPIRIT_POINTS_PER_SKILL_POWER);
    }

    public static double maxSpiritEnergy(final double spiritStat) {
        return Math.max(0.0, spiritStat / SPIRIT_POINTS_PER_ENERGY);
    }

    public static double spiritEnergyRegenPerSecond(final double spiritStat) {
        return maxSpiritEnergy(spiritStat) * SPIRIT_ENERGY_REGEN_FRACTION_PER_SECOND;
    }
}
