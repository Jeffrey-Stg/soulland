package com.zelf115.soulland;

import net.minecraft.nbt.CompoundTag;

/**
 * A block of stat points granted by an absorbed soul ring or spirit bone.
 *
 * <p>Stored on the item stack and, once absorbed, replayed as attribute modifiers so it can be
 * removed again exactly.
 */
public record StatBonus(double damage, double health, double defense, double speed, double spirit,
                        double cultivationSpeed) {

    private static final String DAMAGE_KEY = "DamageBonus";
    private static final String HEALTH_KEY = "HealthBonus";
    private static final String DEFENSE_KEY = "DefenseBonus";
    private static final String SPEED_KEY = "SpeedBonus";
    private static final String SPIRIT_KEY = "SpiritBonus";
    private static final String CULTIVATION_SPEED_KEY = "CultivationSpeedBonus";

    public static StatBonus readFrom(final CompoundTag tag) {
        return new StatBonus(
                tag.getDouble(DAMAGE_KEY),
                tag.getDouble(HEALTH_KEY),
                tag.getDouble(DEFENSE_KEY),
                tag.getDouble(SPEED_KEY),
                tag.getDouble(SPIRIT_KEY),
                tag.getDouble(CULTIVATION_SPEED_KEY));
    }

    public void writeTo(final CompoundTag tag) {
        tag.putDouble(DAMAGE_KEY, damage);
        tag.putDouble(HEALTH_KEY, health);
        tag.putDouble(DEFENSE_KEY, defense);
        tag.putDouble(SPEED_KEY, speed);
        tag.putDouble(SPIRIT_KEY, spirit);
        tag.putDouble(CULTIVATION_SPEED_KEY, cultivationSpeed);
    }

    public StatBonus scaled(final double factor) {
        return new StatBonus(damage * factor, health * factor, defense * factor, speed * factor,
                spirit * factor, cultivationSpeed * factor);
    }

    public StatBonus withCultivationSpeed(final double value) {
        return new StatBonus(damage, health, defense, speed, spirit, value);
    }
}
