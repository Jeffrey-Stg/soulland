package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Core cultivation logic: XP requirements, stat formulas, level-up processing,
 * soul-ring gates, bottleneck detection, and flight abilities.
 */
public class CultivationManager {

    // ---- Constants ----

    public static final int MAX_LEVEL = 120;
    /** Levels that require a soul ring + regular breakthrough to pass (10, 20, …, 90). */
    public static final int SOUL_RING_GATE_INTERVAL = 10;
    public static final int LAST_SOUL_RING_GATE = 90;
    /** Nine ten-level gates up to 90, so nine rings per martial soul. */
    public static final int MAX_SOUL_RINGS = LAST_SOUL_RING_GATE / SOUL_RING_GATE_INTERVAL;
    /** Levels that require a special (lightning) breakthrough every single level. */
    public static final int SPECIAL_BREAKTHROUGH_START = 95;
    /** Level 100 also requires god inheritance or at least one rebirth. */
    public static final int GOD_INHERITANCE_GATE = 100;
    /** Base XP for level 1 → 2. */
    private static final double BASE_XP = 100.0;
    /** Exponential growth factor per level. */
    private static final double XP_GROWTH = 1.15;
    /** Flat stat increase per level (x in the formula y + x * level). */
    private static final double STAT_FLAT_INCREASE_PER_LEVEL = 0.5;
    /** Ticks per second. */
    public static final int TPS = 20;
    /** Ticks between passive meditation XP ticks (1 second). */
    public static final int MEDITATION_TICK_INTERVAL = TPS;
    public static final int HUD_SYNC_INTERVAL_TICKS = TPS / 2;
    public static final int MEDITATION_DURATION_TICKS = TPS * 60 * 5;
    /** Passive XP awarded per meditation tick (before multipliers). */
    public static final double MEDITATION_XP_PER_TICK = 2.0;
    /** Spirit stat increase per minute (60 s) while bottlenecked and meditating. */
    public static final double SPIRIT_BOTTLENECK_INCREASE_PER_MINUTE = 1.0;
    /** Ticks in one minute. */
    public static final int TICKS_PER_MINUTE = TPS * 60;
    /** XP swing per innate stat point away from neutral. */
    private static final double INNATE_STAT_XP_STEP = 0.05;
    /** Level at which elytra-style gliding unlocks. */
    public static final int GLIDE_LEVEL = 70;
    /** Level at which creative-style flight unlocks. */
    public static final int CREATIVE_FLIGHT_LEVEL = 90;
    /** Level at which a player may name themselves a title. */
    public static final int TITLE_LEVEL = 90;
    /** Downward speed past which gliding kicks in, so a small hop does not trigger it. */
    private static final double GLIDE_START_FALL_SPEED = -0.1;
    /** Identifies this mod's flight grant, so revoking it never touches another source of flight. */
    private static final ResourceLocation CULTIVATION_FLIGHT_ID =
            ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "cultivation_flight");
    /** CREATIVE_FLIGHT is a boolean attribute: any value above zero grants flight. */
    private static final double FLIGHT_GRANTED = 1.0;
    private static final double OVERREACH_BASE_CHANCE = 0.50;
    private static final double OVERREACH_PENALTY_PER_TIER = 0.05;
    private static final double OVERREACH_REBIRTH_BONUS = 0.05;
    /** Overreaching is never certain in either direction, however many rebirths back it. */
    private static final double OVERREACH_MIN_CHANCE = 0.05;
    private static final double OVERREACH_MAX_CHANCE = 0.95;
    private static final int TWIN_SOUL_BASE_CHANCE_PERCENT = 20;
    private static final int TWIN_SOUL_CHANCE_PER_INNATE_POINT = 2;
    /** Percent of the next level a kill awards per beast tier, before the tier ratio. */
    private static final double KILL_XP_PERCENT_PER_TIER = 20.0;
    private static final double PERCENT = 100.0;

    // ---- XP Calculations ----

    /**
     * Returns the total XP required to go from {@code level} to {@code level + 1}.
     *
     * <p>Formula: {@code BASE_XP * XP_GROWTH^level}
     */
    public static double xpRequiredForLevel(int level) {
        return BASE_XP * Math.pow(XP_GROWTH, level);
    }

    /**
     * Returns the XP reward for killing a spirit beast.
     *
     * <p>Formula: {@code xpRequired(playerLevel) * 5 * beastTier / playerTier / 100}
     */
    public static double spiritBeastXpReward(int playerLevel, int playerTier, int beastTier) {
        final double rewardPercent = KILL_XP_PERCENT_PER_TIER * beastTier / Math.max(1, playerTier);
        return xpRequiredForLevel(playerLevel) * rewardPercent / PERCENT;
    }

    // ---- Stat Formulas ----

    /**
     * The stat gain of a regular level-up.
     *
     * <p>Formula: {@code y + x * level}, so every stat rises by the same {@code x * level}.
     */
    public static double regularStatIncrease(int level) {
        return STAT_FLAT_INCREASE_PER_LEVEL * level;
    }

    /**
     * The stat multiplier of a breakthrough, applied on levels 11, 21, 31 … 91.
     *
     * <p>Formula: {@code 2^round(level / 10)}.
     */
    public static double breakthroughStatMultiplier(int level) {
        final long exponent = Math.round((double) level / SOUL_RING_GATE_INTERVAL);
        return Math.pow(2.0, exponent);
    }

    // ---- Gate / Bottleneck Detection ----

    /**
     * Returns {@code true} when the player cannot advance past {@code currentLevel}
     * without completing a breakthrough (or fulfilling prerequisites).
     *
     * <p>Bottleneck gates:
     * <ul>
     *   <li>Level is a multiple of 10, between 10 and 90 (soul-ring + regular breakthrough required)</li>
     *   <li>Level is 94 (start of the special-breakthrough chain needed to reach level 95)</li>
     *   <li>Level is 99 (pre-lvl-100 gate; requires god inheritance or rebirth)</li>
     *   <li>Each level 95–119 (special breakthrough required to advance)</li>
     * </ul>
     */
    public static boolean isBottleneckLevel(int currentLevel) {
        if (currentLevel >= 1 && currentLevel <= LAST_SOUL_RING_GATE && currentLevel % SOUL_RING_GATE_INTERVAL == 0) {
            return true;
        }
        if (currentLevel >= SPECIAL_BREAKTHROUGH_START - 1 && currentLevel < MAX_LEVEL) {
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if advancing past {@code currentLevel} requires a soul ring.
     * (Soul ring gates occur at multiples of 10 up to 90.)
     */
    public static boolean requiresSoulRing(int currentLevel) {
        return currentLevel >= SOUL_RING_GATE_INTERVAL && currentLevel <= LAST_SOUL_RING_GATE && currentLevel % SOUL_RING_GATE_INTERVAL == 0;
    }

    /**
     * Returns {@code true} if advancing past {@code currentLevel} needs special
     * lightning breakthrough (applies at levels 94–119).
     */
    public static boolean requiresSpecialBreakthrough(int currentLevel) {
        return currentLevel >= SPECIAL_BREAKTHROUGH_START - 1 && currentLevel < MAX_LEVEL;
    }

    /**
     * Returns {@code true} if advancing past level 99 requires the level-100 gate
     * (god inheritance or at least one rebirth).
     */
    public static boolean requiresLevel100Gate(int currentLevel) {
        return currentLevel == GOD_INHERITANCE_GATE - 1;
    }

    /**
     * Rings are absorbed at each ten-level gate up to 90, so a player holds none before level 10
     * and at most {@link #MAX_SOUL_RINGS} in total.
     */
    public static int maxSoulRingCountForLevel(final int level) {
        return Math.min(MAX_SOUL_RINGS, level / SOUL_RING_GATE_INTERVAL);
    }

    public static int maxAbsorbableTier(final double spiritValue) {
        return SoulRingCapacity.maxAbsorbableTier(spiritValue);
    }

    /**
     * The odds of surviving an overreaching absorption.
     *
     * <p>Formula: {@code 50% - 5% per tier above the limit + the rebirth bonus}.
     */
    public static double overreachSuccessChance(final int allowedTier, final int actualTier, final int rebirthCount) {
        final int tiersAboveLimit = Math.max(0, actualTier - allowedTier);
        final double chance = OVERREACH_BASE_CHANCE
                - tiersAboveLimit * OVERREACH_PENALTY_PER_TIER
                + rebirthCount * OVERREACH_REBIRTH_BONUS;
        return Math.max(OVERREACH_MIN_CHANCE, Math.min(OVERREACH_MAX_CHANCE, chance));
    }

    /**
     * The odds (0-100) of rolling a second martial soul, checked at the first pick and after each
     * of the first two breakthroughs. Only innate stats of 10 or higher get a chance at all.
     *
     * <p>Formula: {@code 20% + 2% per innate stat point above 10}.
     */
    public static int twinMartialSoulChancePercent(final int innateStat) {
        if (innateStat < CultivationData.NEUTRAL_INNATE_STAT) {
            return 0;
        }
        final int aboveNeutral = innateStat - CultivationData.NEUTRAL_INNATE_STAT;
        return Math.min(100, TWIN_SOUL_BASE_CHANCE_PERCENT + TWIN_SOUL_CHANCE_PER_INNATE_POINT * aboveNeutral);
    }

    // ---- XP Multipliers ----

    /**
     * Returns the XP gain multiplier from the innate stat rolled with the martial soul (1–20).
     *
     * <p>Each point above 10 adds 5 %; each point below 10 removes 5 %, so 20 gives +50 % and
     * 1 gives −45 %.
     *
     * @param innateStat the player's innate stat, already including any rebirth bonus
     */
    public static double innateStatXpMultiplier(int innateStat) {
        final int delta = innateStat - CultivationData.NEUTRAL_INNATE_STAT;
        return Math.max(0.0, 1.0 + delta * INNATE_STAT_XP_STEP);
    }

    /**
     * Returns the XP gain multiplier from region qi (biome qi level 1–10).
     *
     * <p>Formula: {@code 1 + (qi - 1) * 0.1}
     */
    public static double regionQiMultiplier(int qi) {
        int clamped = Math.max(1, Math.min(10, qi));
        return 1.0 + (clamped - 1) * 0.1;
    }

    // ---- Level-Up Application ----

    /**
     * Applies stat increases for a regular (non-breakthrough) level-up to the player.
     *
     * @param player the player whose stats are updated
     * @param newLevel the level just reached
     */
    public static void applyRegularLevelStats(Player player, int newLevel) {
        Stats.addToCultivationStats(player, regularStatIncrease(newLevel));
    }

    /**
     * Applies the breakthrough multiplier stat boost to the player.
     *
     * @param player the player
     * @param breakthroughLevel the level that triggered the breakthrough (11, 21, …, 91)
     */
    public static void applyBreakthroughStats(Player player, int breakthroughLevel) {
        Stats.multiplyCultivationStats(player, breakthroughStatMultiplier(breakthroughLevel));
    }

    /**
     * Adds XP and resolves every level-up it pays for.
     *
     * <p>Stops at a bottleneck without spending the banked XP, so clearing the gate can cascade
     * through several levels at once. Pass {@code 0} to replay that cascade after a breakthrough.
     */
    public static void grantXp(final Player player, final CultivationData data, final double amount) {
        data.addXp(amount);

        while (data.getLevel() < MAX_LEVEL && !data.isInBottleneck()) {
            final int level = data.getLevel();
            if (data.getXp() < xpRequiredForLevel(level)) {
                return;
            }
            if (isBottleneckLevel(level)) {
                enterBottleneck(player, data, level);
                return;
            }

            advanceOneLevel(player, data, level);
            if (isBottleneckLevel(data.getLevel())) {
                enterBottleneck(player, data, data.getLevel());
                return;
            }
        }
    }

    private static void advanceOneLevel(final Player player, final CultivationData data, final int level) {
        data.setXp(data.getXp() - xpRequiredForLevel(level));
        final int newLevel = level + 1;
        data.setLevel(newLevel);
        applyRegularLevelStats(player, newLevel);
        Stats.syncDerivedPlayerStats(player, data);
        applyFlightAbilities(player, newLevel);
        MartialSoulEvolution.tryEvolve(player, data);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.level_up", newLevel));
    }

    private static void enterBottleneck(final Player player, final CultivationData data, final int level) {
        data.setInBottleneck(true);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.bottleneck", level));
    }

    // ---- Flight Abilities ----

    /**
     * Applies flight abilities based on the player's cultivation level.
     *
     * <ul>
     *   <li>Level ≥ 90: full creative flight (mayfly = true)</li>
     *   <li>Level 70–89: elytra-like gliding (toggled via slow falling + glide state tracking)</li>
     *   <li>Level &lt; 70: no cultivation-based flight</li>
     * </ul>
     *
     * <p>This must be called server-side and requires abilities to be synced afterwards.
     */
    public static void applyFlightAbilities(Player player, int level) {
        if (player.level().isClientSide()) return;

        final AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight == null) {
            return;
        }

        final boolean granted = flight.hasModifier(CULTIVATION_FLIGHT_ID);
        if (level >= CREATIVE_FLIGHT_LEVEL) {
            if (!granted) {
                flight.addOrReplacePermanentModifier(
                        new AttributeModifier(CULTIVATION_FLIGHT_ID, FLIGHT_GRANTED, AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (granted) {
            flight.removeModifier(CULTIVATION_FLIGHT_ID);
        }
    }

    // ---- Elytra Glide Helper ----

    /**
     * Between {@link #GLIDE_LEVEL} and {@link #CREATIVE_FLIGHT_LEVEL} the player glides instead of
     * falling. Sneaking opts out, so a cultivator can still drop straight down when they mean to.
     */
    public static void tickElytraGlide(Player player, int level) {
        if (player.level().isClientSide()) return;
        if (level < GLIDE_LEVEL || level >= CREATIVE_FLIGHT_LEVEL) return;
        if (player.onGround() || player.isInWater() || player.isFallFlying() || player.isCrouching()) return;
        if (player.getDeltaMovement().y >= GLIDE_START_FALL_SPEED) return;

        player.startFallFlying();
    }
}
