package com.zelf115.soulland.spirit;

import com.zelf115.soulland.Config;
import com.zelf115.soulland.DerivedStats;
import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.StatBand;
import com.zelf115.soulland.StatBonus;
import com.zelf115.soulland.cultivation.skill.SkillPools;
import com.zelf115.soulland.cultivation.skill.SkillTag;
import com.zelf115.soulland.item.SoulRingItem;
import com.zelf115.soulland.item.SpiritBoneItem;
import com.zelf115.soulland.spirit.SpiritBosses.BossBone;
import com.zelf115.soulland.spirit.SpiritBosses.BossProfile;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

    /** Soul rings quicken cultivation in proportion to their colour. */
    private static final double CULTIVATION_SPEED_PER_TIER = 5.0;

    /** Fallbacks for a beast whose entity type declares no such attribute. */
    private static final double DEFAULT_BASE_MAX_HEALTH = 20.0;
    private static final double DEFAULT_BASE_DAMAGE = 2.0;
    private static final double DEFAULT_BASE_ARMOR = 0.0;
    private static final double DEFAULT_BASE_SPEED = 0.1;
    /** Spirit has no vanilla attribute to grow from, so every beast starts at the same footing. */
    private static final double BASE_SPIRIT_STAT = 10.0;

    private static final List<String> BODY_BONE_SLOTS = List.of(
            "Torso Bone", "Left Arm Bone", "Right Arm Bone", "Left Leg Bone", "Right Leg Bone");
    private static final String SKULL_BONE_SLOT = "Skull Bone";
    /** Every bone slot a beast carries inside its body, as opposed to the external bones. */
    public static final List<String> INTERNAL_BONE_SLOTS = Stream.concat(
            BODY_BONE_SLOTS.stream(), Stream.of(SKULL_BONE_SLOT)).toList();
    /** Youngest age each tier starts at, in tier order; the inverse of the year roll table. */
    private static final int[] TIER_YEAR_FLOORS = {1, 100, 1_000, 10_000, 100_000, 200_000, 1_000_000};
    private static final int LOWEST_TIER = 1;
    /**
     * The tier roll where 1 000-year beasts begin: wild lands roll below it, capping beasts at 999
     * years, and the mod's own biomes roll above it.
     */
    private static final double HOMELAND_ROLL_FLOOR = 0.55D;
    /** Bosses must last long enough for a real fight, so they carry this many times the health of their age. */
    private static final double BOSS_HEALTH_MULTIPLIER = 5.0;
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

    /**
     * Rolls a beast's age, tier and starting stats, recording the vanilla values it started from.
     * A boss is always the age its profile names.
     */
    private static void rollBeast(final SpiritBeastEntity monster, final CompoundTag data) {
        final Optional<BossProfile> boss = SpiritBosses.of(monster.getType());
        final int years = boss.map(BossProfile::years)
                .orElseGet(() -> randomYearsForTier(monster, rollTier(tierRollFor(monster))));
        final int tier = tierForYears(years);
        final int level = effectiveLevelForYears(years);
        final double healthMultiplier = boss.isPresent() ? BOSS_HEALTH_MULTIPLIER : 1.0;
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
        data.putDouble(HEALTH_STAT_KEY,
                grownStat(DerivedStats.healthStatFor(baseMaxHealth), level, tier) * healthMultiplier);
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
        final Set<Affinity> affinities = AffinitySystem.affinitiesOf(monster);
        final ItemStack ring = SoulRingItem.create(beastName(monster), tier, data.getInt(YEARS_KEY),
                rollSoulRingBonus(tier, monster.getRandom()), affinities);
        SpiritBosses.of(monster.getType()).map(BossProfile::ringSkill)
                .or(() -> SkillPools.roll(affinities, monster.getRandom()))
                .ifPresent(skill -> SkillTag.attach(ring, skill));
        return ring;
    }

    /** The stats of a soul ring of this colour, with the cultivation speed every ring of it carries. */
    public static StatBonus rollSoulRingBonus(final int tier, final RandomSource random) {
        return StatBand.roll(tier, random).withCultivationSpeed(tier * CULTIVATION_SPEED_PER_TIER);
    }

    /** The greatest age a beast of this colour reaches; the top colour is open-ended, so it returns its floor. */
    public static int oldestYearsOfTier(final int tier) {
        final int index = Math.max(LOWEST_TIER, Math.min(TIER_YEAR_FLOORS.length, tier)) - 1;
        if (index + 1 >= TIER_YEAR_FLOORS.length) {
            return TIER_YEAR_FLOORS[index];
        }
        return TIER_YEAR_FLOORS[index + 1] - 1;
    }

    /** A bone from this beast, carrying a skill from the pools its affinities reach. */
    public static ItemStack createSpiritBone(final SpiritBeastEntity monster) {
        final CompoundTag data = monster.getPersistentData();
        final int tier = data.getInt(TIER_KEY);
        final String slot = rollBoneSlot(monster.getRandom(), monster.getType());
        final ItemStack bone = SpiritBoneItem.create(beastName(monster), slot, tier, data.getInt(YEARS_KEY),
                StatBand.roll(tier, monster.getRandom()));
        SkillPools.rollForBone(AffinitySystem.affinitiesOf(monster), monster.getRandom())
                .ifPresent(skill -> SkillTag.attach(bone, skill));
        return bone;
    }

    /** The bones a boss always leaves behind, each carrying the skill its profile names. */
    public static List<ItemStack> createBossBones(final SpiritBeastEntity monster) {
        final CompoundTag data = monster.getPersistentData();
        final int tier = data.getInt(TIER_KEY);
        final List<BossBone> bones = SpiritBosses.of(monster.getType()).map(BossProfile::bones).orElse(List.of());
        return bones.stream().map(bone -> {
            final ItemStack stack = SpiritBoneItem.create(beastName(monster), bone.slot(), tier,
                    data.getInt(YEARS_KEY), StatBand.roll(tier, monster.getRandom()));
            SkillTag.attach(stack, bone.skill());
            return stack;
        }).toList();
    }

    /** Whether a dying beast leaves a bone behind: old enough, and lucky enough. */
    public static boolean rollsSpiritBone(final SpiritBeastEntity monster) {
        return getTier(monster) >= Config.SPIRIT_BONE_MIN_TIER.getAsInt()
                && monster.getRandom().nextDouble() < Config.SPIRIT_BONE_DROP_CHANCE.getAsDouble();
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

    public static double getSpiritStat(final SpiritBeastEntity monster) {
        return monster.getPersistentData().getDouble(SPIRIT_STAT_KEY);
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

    /** A beast's name in the colour of its age band, so chat reads the same as the beast's name tag. */
    public static Component nameInTierColor(final String name, final int tier) {
        return Component.literal(name).withStyle(style -> style.withColor(tierTextColor(tier)));
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

    /**
     * Picks which bone a beast yields: its external bone if it has one and the rarest roll lands,
     * otherwise a skull on an uncommon roll, otherwise one of the body slots.
     */
    public static String rollBoneSlot(final RandomSource random, final EntityType<?> entityType) {
        final String externalBone = EXTERNAL_BONE_BY_BEAST.get(entityPath(entityType));
        if (externalBone != null && random.nextDouble() < Config.EXTERNAL_BONE_CHANCE.getAsDouble()) {
            return externalBone;
        }
        if (random.nextDouble() < Config.SKULL_BONE_CHANCE.getAsDouble()) {
            return SKULL_BONE_SLOT;
        }
        return BODY_BONE_SLOTS.get(random.nextInt(BODY_BONE_SLOTS.size()));
    }

    /** Every bone slot there is: the internal ones, then each external bone. */
    public static List<String> allBoneSlots() {
        return Stream.concat(INTERNAL_BONE_SLOTS.stream(), EXTERNAL_BONE_BY_BEAST.values().stream().sorted()).toList();
    }

    /** Whether this bone slot is one of the external bones that can be shown or hidden at will. */
    public static boolean isExternalBoneSlot(final String slot) {
        return EXTERNAL_BONE_BY_BEAST.containsValue(slot);
    }

    private static String entityPath(final EntityType<?> entityType) {
        final ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return id == null ? "" : id.getPath();
    }

    private static double tierRollFor(final SpiritBeastEntity monster) {
        final double roll = monster.getRandom().nextDouble();
        if (isInSoulLandBiome(monster)) {
            return HOMELAND_ROLL_FLOOR + roll * (1.0D - HOMELAND_ROLL_FLOOR);
        }
        return roll * HOMELAND_ROLL_FLOOR;
    }

    private static boolean isInSoulLandBiome(final SpiritBeastEntity monster) {
        return monster.level().getBiome(monster.blockPosition()).unwrapKey()
                .map(key -> key.location().getNamespace().equals(SoulLand.MODID))
                .orElse(false);
    }

    private static int rollTier(final double roll) {
        if (roll < 0.30D) {
            return 1;
        }
        if (roll < HOMELAND_ROLL_FLOOR) {
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
