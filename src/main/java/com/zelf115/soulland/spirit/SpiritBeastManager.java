package com.zelf115.soulland.spirit;

import com.zelf115.soulland.item.SoulRingItem;
import com.zelf115.soulland.item.SpiritBoneItem;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;

public final class SpiritBeastManager {
    public static final String INITIALIZED_KEY = "soulland_spirit_beast_initialized";
    public static final String YEARS_KEY = "soulland_spirit_beast_years";
    public static final String TIER_KEY = "soulland_spirit_beast_tier";
    public static final String DAMAGE_STAT_KEY = "soulland_spirit_beast_damage_stat";
    public static final String HEALTH_STAT_KEY = "soulland_spirit_beast_health_stat";
    public static final String DEFENSE_STAT_KEY = "soulland_spirit_beast_defense_stat";
    public static final String SPEED_STAT_KEY = "soulland_spirit_beast_speed_stat";
    public static final String SPIRIT_STAT_KEY = "soulland_spirit_beast_spirit_stat";
    public static final String BASE_MAX_HEALTH_KEY = "soulland_spirit_beast_base_max_health";
    public static final String BASE_DAMAGE_KEY = "soulland_spirit_beast_base_damage";
    public static final String BASE_ARMOR_KEY = "soulland_spirit_beast_base_armor";
    public static final String BASE_SPEED_KEY = "soulland_spirit_beast_base_speed";

    private SpiritBeastManager() {
    }

    public static void ensureSpiritBeast(final Monster monster) {
        final CompoundTag data = monster.getPersistentData();
        boolean initializedNow = false;
        if (!data.getBoolean(INITIALIZED_KEY)) {
            final int tier = rollTier(monster.getRandom().nextDouble());
            final int years = randomYearsForTier(monster.getRandom().nextInt(), tier);
            final double baseMaxHealth = getBaseValue(monster, Attributes.MAX_HEALTH, 20.0D);
            final double baseDamage = getBaseValue(monster, Attributes.ATTACK_DAMAGE, 2.0D);
            final double baseArmor = getBaseValue(monster, Attributes.ARMOR, 0.0D);
            final double baseSpeed = getBaseValue(monster, Attributes.MOVEMENT_SPEED, 0.1D);
            final int effectiveLevel = Math.max(1, years / 100);
            data.putBoolean(INITIALIZED_KEY, true);
            data.putInt(TIER_KEY, tier);
            data.putInt(YEARS_KEY, years);
            data.putDouble(BASE_MAX_HEALTH_KEY, baseMaxHealth);
            data.putDouble(BASE_DAMAGE_KEY, baseDamage);
            data.putDouble(BASE_ARMOR_KEY, baseArmor);
            data.putDouble(BASE_SPEED_KEY, baseSpeed);
            data.putDouble(HEALTH_STAT_KEY, (baseMaxHealth * 20.0D + effectiveLevel) * tier);
            data.putDouble(DAMAGE_STAT_KEY, (baseDamage * 20.0D + effectiveLevel) * tier);
            data.putDouble(DEFENSE_STAT_KEY, (baseArmor * 50.0D + effectiveLevel) * tier);
            data.putDouble(SPEED_STAT_KEY, (baseSpeed * 2500.0D + effectiveLevel) * tier);
            data.putDouble(SPIRIT_STAT_KEY, (10.0D + effectiveLevel) * tier);
            initializedNow = true;
        }

        applyStats(monster, initializedNow);
    }

    public static void applyStats(final Monster monster, final boolean healToFull) {
        final CompoundTag data = monster.getPersistentData();
        final double healthStat = data.getDouble(HEALTH_STAT_KEY);
        final double damageStat = data.getDouble(DAMAGE_STAT_KEY);
        final double defenseStat = data.getDouble(DEFENSE_STAT_KEY);
        final double speedStat = data.getDouble(SPEED_STAT_KEY);
        final double maxHealth = (data.getDouble(BASE_MAX_HEALTH_KEY) + healthStat / 20.0D) * (1.0D + healthStat / 10000.0D);
        setBaseValue(monster, Attributes.MAX_HEALTH, maxHealth);
        setBaseValue(monster, Attributes.ATTACK_DAMAGE, data.getDouble(BASE_DAMAGE_KEY) + damageStat / 20.0D);
        setBaseValue(monster, Attributes.ARMOR, data.getDouble(BASE_ARMOR_KEY) + defenseStat / 50.0D);
        setBaseValue(monster, Attributes.MOVEMENT_SPEED, data.getDouble(BASE_SPEED_KEY) * (1.0D + speedStat / 2500.0D));
        if (monster.getHealth() > monster.getMaxHealth()) {
            monster.setHealth(monster.getMaxHealth());
        } else if (healToFull && monster.getHealth() < monster.getMaxHealth()) {
            monster.heal(monster.getMaxHealth() - monster.getHealth());
        }
    }

    public static ItemStack createSoulRing(final Monster monster) {
        final CompoundTag data = monster.getPersistentData();
        return SoulRingItem.create(
                monster.getType().getDescription().getString(),
                data.getInt(TIER_KEY),
                data.getInt(YEARS_KEY),
                data.getDouble(DAMAGE_STAT_KEY) * 0.25D,
                data.getDouble(HEALTH_STAT_KEY) * 0.25D,
                data.getDouble(DEFENSE_STAT_KEY) * 0.25D,
                data.getDouble(SPEED_STAT_KEY) * 0.25D,
                data.getDouble(SPIRIT_STAT_KEY) * 0.25D
        );
    }

    public static ItemStack createSpiritBone(final Monster monster) {
        final CompoundTag data = monster.getPersistentData();
        final String slot = randomBoneSlot(monster.getRandom().nextDouble(), monster.getType());
        return SpiritBoneItem.create(
                monster.getType().getDescription().getString(),
                slot,
                data.getInt(TIER_KEY),
                data.getInt(YEARS_KEY),
                data.getDouble(DAMAGE_STAT_KEY) * 0.10D,
                data.getDouble(HEALTH_STAT_KEY) * 0.10D,
                data.getDouble(DEFENSE_STAT_KEY) * 0.10D,
                data.getDouble(SPEED_STAT_KEY) * 0.10D,
                data.getDouble(SPIRIT_STAT_KEY) * 0.10D
        );
    }

    public static int getTier(final Monster monster) {
        return monster.getPersistentData().getInt(TIER_KEY);
    }

    public static double getDamageStat(final Monster monster) {
        return monster.getPersistentData().getDouble(DAMAGE_STAT_KEY);
    }

    public static double getDefenseStat(final Monster monster) {
        return monster.getPersistentData().getDouble(DEFENSE_STAT_KEY);
    }

    public static ChatFormatting tierColor(final int tier) {
        return switch (tier) {
            case 1 -> ChatFormatting.WHITE;
            case 2 -> ChatFormatting.YELLOW;
            case 3 -> ChatFormatting.DARK_PURPLE;
            case 4 -> ChatFormatting.DARK_GRAY;
            case 5 -> ChatFormatting.RED;
            case 6 -> ChatFormatting.GOLD;
            default -> ChatFormatting.AQUA;
        };
    }

    public static String describeTier(final int tier) {
        return switch (tier) {
            case 1 -> "White";
            case 2 -> "Yellow";
            case 3 -> "Purple";
            case 4 -> "Black";
            case 5 -> "Red";
            case 6 -> "Orange";
            default -> "Gold";
        };
    }

    private static String randomBoneSlot(final double roll, final EntityType<?> entityType) {
        final String entityPath = entityType.getDescriptionId().toLowerCase(Locale.ROOT);
        if (entityPath.contains("scorpion") || entityPath.contains("spider") || entityPath.contains("bear") || entityPath.contains("eye")) {
            return "External Bone";
        }
        if (roll < 0.12D) {
            return "Skull Bone";
        }
        final double normalizedRoll = (roll - 0.12D) / 0.88D;
        return switch (Math.min(4, (int) (normalizedRoll * 5.0D))) {
            case 0 -> "Torso Bone";
            case 1 -> "Left Arm Bone";
            case 2 -> "Right Arm Bone";
            case 3 -> "Left Leg Bone";
            default -> "Right Leg Bone";
        };
    }

    private static int rollTier(final double roll) {
        if (roll < 0.30D) {
            return 1;
        }
        if (roll < 0.55D) {
            return 2;
        }
        if (roll < 0.75D) {
            return 3;
        }
        if (roll < 0.88D) {
            return 4;
        }
        if (roll < 0.95D) {
            return 5;
        }
        if (roll < 0.99D) {
            return 6;
        }
        return 7;
    }

    private static int randomYearsForTier(final int randomSeed, final int tier) {
        final int positiveSeed = Math.abs(randomSeed == Integer.MIN_VALUE ? 0 : randomSeed);
        return switch (tier) {
            case 1 -> 1 + positiveSeed % 99;
            case 2 -> 100 + positiveSeed % 900;
            case 3 -> 1_000 + positiveSeed % 9_000;
            case 4 -> 10_000 + positiveSeed % 90_000;
            case 5 -> 100_000 + positiveSeed % 100_000;
            case 6 -> 200_000 + positiveSeed % 800_000;
            default -> 1_000_000 + positiveSeed % (Integer.MAX_VALUE - 1_000_000);
        };
    }

    private static double getBaseValue(final Monster monster, final net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, final double fallback) {
        final var instance = monster.getAttribute(attribute);
        return instance != null ? instance.getBaseValue() : fallback;
    }

    private static void setBaseValue(final Monster monster, final net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, final double value) {
        final var instance = monster.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }
}
