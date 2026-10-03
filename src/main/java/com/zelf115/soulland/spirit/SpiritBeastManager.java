package com.zelf115.soulland.spirit;

import com.zelf115.soulland.Config;
import com.zelf115.soulland.DerivedStats;
import com.zelf115.soulland.StatBonus;
import com.zelf115.soulland.item.SoulRingItem;
import com.zelf115.soulland.item.SpiritBoneItem;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

    /** Share of a beast's strength carried by the soul ring it drops. */
    private static final double SOUL_RING_STRENGTH_SHARE = 0.25;
    /** Share of a beast's strength carried by a spirit bone: a minor boost, not a major one. */
    private static final double SPIRIT_BONE_STRENGTH_SHARE = 0.10;
    /** Soul rings quicken cultivation in proportion to their colour. */
    private static final double CULTIVATION_SPEED_PER_TIER = 5.0;
    /** Lowest tier that drops spirit bones at all, i.e. beasts of 10 000 years and up. */
    public static final int SPIRIT_BONE_MIN_TIER = 4;
    /** Chance a bone drops at all from a qualifying beast. */
    public static final double SPIRIT_BONE_DROP_CHANCE = 0.02;

    /** Fallbacks for a beast whose entity type declares no such attribute. */
    private static final double DEFAULT_BASE_MAX_HEALTH = 20.0;
    private static final double DEFAULT_BASE_DAMAGE = 2.0;
    private static final double DEFAULT_BASE_ARMOR = 0.0;
    private static final double DEFAULT_BASE_SPEED = 0.1;
    /** Spirit has no vanilla attribute to grow from, so every beast starts at the same footing. */
    private static final double BASE_SPIRIT_STAT = 10.0;

    private static final double SKULL_BONE_CHANCE = 0.12;
    /** External bones are the rarest of all, and only the four beasts below carry one. */
    private static final double EXTERNAL_BONE_CHANCE = 0.05;
    private static final List<String> BODY_BONE_SLOTS = List.of(
            "Torso Bone", "Left Arm Bone", "Right Arm Bone", "Left Leg Bone", "Right Leg Bone");
    private static final String SKULL_BONE_SLOT = "Skull Bone";
    /** Every bone slot a beast carries inside its body, as opposed to the external bones. */
    public static final List<String> INTERNAL_BONE_SLOTS = Stream.concat(
            BODY_BONE_SLOTS.stream(), Stream.of(SKULL_BONE_SLOT)).toList();
    /** Spread of a single ring stat around what the beast's age and colour earn it. */
    private static final double MIN_STAT_ROLL = 0.60;
    private static final double MAX_STAT_ROLL = 1.60;
    /** Age lifts a ring at most this far above the floor of its colour, so the top end stays finite. */
    private static final double MIN_AGE_STRENGTH = 1.0;
    private static final double MAX_AGE_STRENGTH = 4.0;
    /** Youngest age each tier starts at, in tier order; the inverse of the year roll table. */
    private static final int[] TIER_YEAR_FLOORS = {1, 100, 1_000, 10_000, 100_000, 200_000, 1_000_000};
    private static final int LOWEST_TIER = 1;
    /** Beasts whose bones are boss loot, so no other source may hand them out. */
    public static final Set<String> BOSS_BEAST_PATHS = Set.of(
            "ice_jade_scorpion", "ice_bear", "evil_spirit_orca", "three_eyed_golden_lion");
    /** The external spirit bone each of these named beasts carries, keyed by entity path. */
    private static final Map<String, String> EXTERNAL_BONE_BY_BEAST = Map.of(
            "ice_jade_scorpion", "Ice Jade Tail",
            "manfaced_demon_spider", "Spider Lance",
            "dark_gold_terror_claw_bear", "Terror Claws",
            "evileye_tyrant", "Third Eye");

    private SpiritBeastManager() {
    }

    public static void ensureSpiritBeast(final SpiritBeastEntity monster) {
        final CompoundTag data = monster.getPersistentData();
        if (data.getBoolean(INITIALIZED_KEY)) {
            monster.syncTier(data.getInt(TIER_KEY));
            applyStats(monster);
            return;
        }

        rollBeast(monster, data);
        monster.syncTier(data.getInt(TIER_KEY));
        applyStatsAtFullHealth(monster);
    }

    /** Rolls a beast's age, tier and starting stats, recording the vanilla values it started from. */
    private static void rollBeast(final SpiritBeastEntity monster, final CompoundTag data) {
        final int tier = rollTier(monster.getRandom().nextDouble());
        final int years = randomYearsForTier(monster, tier);
        final int level = effectiveLevelForYears(years);
        final double baseMaxHealth = getBaseValue(monster, Attributes.MAX_HEALTH, DEFAULT_BASE_MAX_HEALTH);
        final double baseDamage = getBaseValue(monster, Attributes.ATTACK_DAMAGE, DEFAULT_BASE_DAMAGE);
        final double baseArmor = getBaseValue(monster, Attributes.ARMOR, DEFAULT_BASE_ARMOR);
        final double baseSpeed = getBaseValue(monster, Attributes.MOVEMENT_SPEED, DEFAULT_BASE_SPEED);

        data.putBoolean(INITIALIZED_KEY, true);
        data.putInt(TIER_KEY, tier);
        data.putInt(YEARS_KEY, years);
        data.putDouble(BASE_MAX_HEALTH_KEY, baseMaxHealth);
        data.putDouble(BASE_DAMAGE_KEY, baseDamage);
        data.putDouble(BASE_ARMOR_KEY, baseArmor);
        data.putDouble(HEALTH_STAT_KEY, grownStat(DerivedStats.healthStatFor(baseMaxHealth), level, tier));
        data.putDouble(DAMAGE_STAT_KEY, grownStat(DerivedStats.damageStatFor(baseDamage), level, tier));
        data.putDouble(DEFENSE_STAT_KEY, grownStat(DerivedStats.defenseStatFor(baseArmor), level, tier));
        data.putDouble(SPEED_STAT_KEY, grownStat(DerivedStats.speedStatFor(baseSpeed), level, tier));
        data.putDouble(SPIRIT_STAT_KEY, grownStat(BASE_SPIRIT_STAT, level, tier));
    }

    /** Stat growth formula: {@code (base stat + level) * tier}. */
    private static double grownStat(final double baseStat, final int level, final int tier) {
        return (baseStat + level) * tier;
    }

    /**
     * Age drives a beast's level, but only up to a configured ceiling: the raw year counts run to
     * the billions, which would otherwise put million-hit-point beasts in the overworld.
     */
    private static int effectiveLevelForYears(final int years) {
        final int level = years / Config.SPIRIT_BEAST_YEARS_PER_LEVEL.getAsInt();
        return Math.min(Config.SPIRIT_BEAST_MAX_EFFECTIVE_LEVEL.getAsInt(), Math.max(1, level));
    }

    /** Recomputes the beast's vanilla attributes, leaving its current health where it stands. */
    public static void applyStats(final SpiritBeastEntity monster) {
        final CompoundTag data = monster.getPersistentData();
        setBaseValue(monster, Attributes.MAX_HEALTH,
                DerivedStats.maxHealth(data.getDouble(BASE_MAX_HEALTH_KEY), data.getDouble(HEALTH_STAT_KEY)));
        setBaseValue(monster, Attributes.ATTACK_DAMAGE,
                DerivedStats.attackDamage(data.getDouble(BASE_DAMAGE_KEY), data.getDouble(DAMAGE_STAT_KEY)));
        setBaseValue(monster, Attributes.ARMOR,
                DerivedStats.armor(data.getDouble(BASE_ARMOR_KEY), data.getDouble(DEFENSE_STAT_KEY)));
        clampHealthToMaximum(monster);
    }

    /** Applies the stats a newly rolled beast was born with, so it enters the world at full health. */
    public static void applyStatsAtFullHealth(final SpiritBeastEntity monster) {
        applyStats(monster);
        if (monster.getHealth() < monster.getMaxHealth()) {
            monster.heal(monster.getMaxHealth() - monster.getHealth());
        }
    }

    private static void clampHealthToMaximum(final SpiritBeastEntity monster) {
        if (monster.getHealth() > monster.getMaxHealth()) {
            monster.setHealth(monster.getMaxHealth());
        }
    }

    public static ItemStack createSoulRing(final SpiritBeastEntity monster) {
        final CompoundTag data = monster.getPersistentData();
        final int tier = data.getInt(TIER_KEY);
        final int years = data.getInt(YEARS_KEY);
        final StatBonus bonus = rolledPerStat(
                statsOf(data).scaled(SOUL_RING_STRENGTH_SHARE * ageStrength(years, tier)), monster.getRandom())
                .withCultivationSpeed(tier * CULTIVATION_SPEED_PER_TIER);
        return SoulRingItem.create(beastName(monster), tier, years, bonus,
            AffinitySystem.affinitiesOf(monster));
    }

    /**
     * How much the beast's own age lifts the ring above the floor of its colour.
     *
     * <p>Combat stats stop climbing at the configured level ceiling, which would otherwise make the
     * ring off a million-year beast identical to one off a beast a tenth its age. The ring reads the
     * raw years instead, so within a colour the elder beast always yields the better ring.
     */
    private static double ageStrength(final int years, final int tier) {
        final int band = Math.max(LOWEST_TIER, Math.min(TIER_YEAR_FLOORS.length, tier));
        final double floor = TIER_YEAR_FLOORS[band - 1];
        final double span = Math.max(1.0, oldestYearsOfTier(band) - floor);
        final double throughBand = Math.max(0.0, Math.min(1.0, (years - floor) / span));
        return MIN_AGE_STRENGTH + (MAX_AGE_STRENGTH - MIN_AGE_STRENGTH) * throughBand;
    }

    /**
     * Rolls each stat of the ring separately, so two rings of one colour and age are still worth
     * comparing rather than being the same ring twice.
     */
    private static StatBonus rolledPerStat(final StatBonus bonus, final RandomSource random) {
        return new StatBonus(
                roll(bonus.damage(), random),
                roll(bonus.health(), random),
                roll(bonus.defense(), random),
                roll(bonus.speed(), random),
                roll(bonus.spirit(), random),
                bonus.cultivationSpeed());
    }

    private static double roll(final double statValue, final RandomSource random) {
        return statValue * (MIN_STAT_ROLL + random.nextDouble() * (MAX_STAT_ROLL - MIN_STAT_ROLL));
    }

    /**
     * The strength of a ring that was never cut from a living beast, priced as if it had been taken
     * from one of that colour and age.
     */
    public static StatBonus soulRingBonusForAge(final int tier, final int years) {
        final double stat = grownStat(BASE_SPIRIT_STAT, effectiveLevelForYears(years), tier);
        return new StatBonus(stat, stat, stat, stat, stat, 0.0)
                .scaled(SOUL_RING_STRENGTH_SHARE * ageStrength(years, tier))
                .withCultivationSpeed(tier * CULTIVATION_SPEED_PER_TIER);
    }

    /** The strength of a bone that came from no particular beast, priced by colour and age. */
    public static StatBonus spiritBoneBonusForAge(final int tier, final int years) {
        final double stat = grownStat(BASE_SPIRIT_STAT, effectiveLevelForYears(years), tier);
        return new StatBonus(stat, stat, stat, stat, stat, 0.0).scaled(SPIRIT_BONE_STRENGTH_SHARE);
    }

    /** The greatest age a beast of this colour reaches; the top colour is open-ended, so it returns its floor. */
    public static int oldestYearsOfTier(final int tier) {
        final int index = Math.max(LOWEST_TIER, Math.min(TIER_YEAR_FLOORS.length, tier)) - 1;
        if (index + 1 >= TIER_YEAR_FLOORS.length) {
            return TIER_YEAR_FLOORS[index];
        }
        return TIER_YEAR_FLOORS[index + 1] - 1;
    }

    public static ItemStack createSpiritBone(final SpiritBeastEntity monster) {
        final CompoundTag data = monster.getPersistentData();
        final String slot = rollBoneSlot(monster.getRandom(), monster.getType());
        return SpiritBoneItem.create(beastName(monster), slot, data.getInt(TIER_KEY), data.getInt(YEARS_KEY),
                statsOf(data).scaled(SPIRIT_BONE_STRENGTH_SHARE));
    }

    public static int getTier(final SpiritBeastEntity monster) {
        return monster.getPersistentData().getInt(TIER_KEY);
    }

    public static int getYears(final SpiritBeastEntity monster) {
        return monster.getPersistentData().getInt(YEARS_KEY);
    }

    /** The colour a beast of this age carries, the inverse of the tier year bands. */
    public static int tierForYears(final int years) {
        int tier = LOWEST_TIER;
        for (int index = 0; index < TIER_YEAR_FLOORS.length; index++) {
            if (years >= TIER_YEAR_FLOORS[index]) {
                tier = index + 1;
            }
        }
        return tier;
    }

    public static double getDamageStat(final SpiritBeastEntity monster) {
        return monster.getPersistentData().getDouble(DAMAGE_STAT_KEY);
    }

    public static double getDefenseStat(final SpiritBeastEntity monster) {
        return monster.getPersistentData().getDouble(DEFENSE_STAT_KEY);
    }

    /**
     * The colour of the soul ring each age band drops, as exact text colours.
     *
     * <p>Chat formatting has no orange and only one gold, which would have left the two oldest
     * bands indistinguishable, so the bands carry their own colours instead. Black is lightened
     * enough to stay readable against a name tag's dark backing.
     */
    private static final int[] TIER_TEXT_COLORS = {
            0xFFFFFF, 0xFFE14F, 0xB44BFF, 0x70707E, 0xFF4B4B, 0xFF9A2E, 0xFFD24A};

    /** The text colour for an age band, matching the ring a beast of that band drops. */
    public static int tierTextColor(final int tier) {
        final int index = Math.max(LOWEST_TIER, Math.min(TIER_TEXT_COLORS.length, tier)) - 1;
        return TIER_TEXT_COLORS[index];
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

    private static String beastName(final SpiritBeastEntity monster) {
        return monster.getType().getDescription().getString();
    }

    private static StatBonus statsOf(final CompoundTag data) {
        return new StatBonus(
                data.getDouble(DAMAGE_STAT_KEY),
                data.getDouble(HEALTH_STAT_KEY),
                data.getDouble(DEFENSE_STAT_KEY),
                data.getDouble(SPEED_STAT_KEY),
                data.getDouble(SPIRIT_STAT_KEY),
                0.0);
    }

    /**
     * Picks which bone a beast yields: its external bone if it has one and the rarest roll lands,
     * otherwise a skull on an uncommon roll, otherwise one of the body slots.
     */
    private static String rollBoneSlot(final RandomSource random, final EntityType<?> entityType) {
        final String externalBone = EXTERNAL_BONE_BY_BEAST.get(entityPath(entityType));
        if (externalBone != null && random.nextDouble() < EXTERNAL_BONE_CHANCE) {
            return externalBone;
        }
        if (random.nextDouble() < SKULL_BONE_CHANCE) {
            return SKULL_BONE_SLOT;
        }
        return BODY_BONE_SLOTS.get(random.nextInt(BODY_BONE_SLOTS.size()));
    }

    /** Whether this bone slot is one of the external bones that can be shown or hidden at will. */
    public static boolean isExternalBoneSlot(final String slot) {
        return EXTERNAL_BONE_BY_BEAST.containsValue(slot);
    }

    private static String entityPath(final EntityType<?> entityType) {
        final ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return id == null ? "" : id.getPath();
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

    private static int randomYearsForTier(final SpiritBeastEntity monster, final int tier) {
        return switch (tier) {
            case 1 -> 1 + monster.getRandom().nextInt(99);
            case 2 -> 100 + monster.getRandom().nextInt(900);
            case 3 -> 1_000 + monster.getRandom().nextInt(9_000);
            case 4 -> 10_000 + monster.getRandom().nextInt(90_000);
            case 5 -> 100_000 + monster.getRandom().nextInt(100_000);
            case 6 -> 200_000 + monster.getRandom().nextInt(800_000);
            default -> 1_000_000 + monster.getRandom().nextInt(Integer.MAX_VALUE - 1_000_000);
        };
    }

    private static double getBaseValue(final SpiritBeastEntity monster, final net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, final double fallback) {
        final var instance = monster.getAttribute(attribute);
        return instance != null ? instance.getBaseValue() : fallback;
    }

    private static void setBaseValue(final SpiritBeastEntity monster, final net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, final double value) {
        final var instance = monster.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }
}
