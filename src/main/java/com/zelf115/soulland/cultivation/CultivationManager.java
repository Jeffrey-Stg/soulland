package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.Stats;
import net.minecraft.world.entity.player.Player;

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
    public static final int MEDITATION_DURATION_TICKS = TPS * 60 * 5;
    /** Passive XP awarded per meditation tick (before multipliers). */
    public static final double MEDITATION_XP_PER_TICK = 2.0;
    /** Spirit stat increase per minute (60 s) while bottlenecked and meditating. */
    public static final double SPIRIT_BOTTLENECK_INCREASE_PER_MINUTE = 1.0;
    /** Ticks in one minute. */
    public static final int TICKS_PER_MINUTE = TPS * 60;

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
        double required = xpRequiredForLevel(playerLevel);
        return required * 5.0 * beastTier / Math.max(1, playerTier) / 100.0;
    }

    // ---- Stat Formulas ----

    /**
     * Regular level-up stat increase.
     *
     * <p>Formula: {@code y + x * level} where y is the current stat value.
     * Returns the <em>new</em> stat value after the increase.
     */
    public static double regularStatIncrease(double currentStat, int level) {
        return currentStat + STAT_FLAT_INCREASE_PER_LEVEL * level;
    }

    /**
     * Breakthrough (milestone) stat multiplier applied on levels 11, 21, 31 … 91.
     *
     * <p>Formula: {@code currentStat * 2^round(level / 10)}
     * Returns the <em>new</em> stat value after the multiplier.
     */
    public static double breakthroughStatValue(double currentStat, int level) {
        long exponent = Math.round((double) level / 10.0);
        return currentStat * Math.pow(2.0, exponent);
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
        return currentLevel >= 10 && currentLevel <= LAST_SOUL_RING_GATE && currentLevel % SOUL_RING_GATE_INTERVAL == 0;
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

    public static int maxSoulRingCountForLevel(final int level) {
        if (level >= LAST_SOUL_RING_GATE) {
            return LAST_SOUL_RING_GATE / SOUL_RING_GATE_INTERVAL;
        }
        return Math.max(1, level / SOUL_RING_GATE_INTERVAL + 1);
    }

    public static int maxAbsorbableTier(final double spiritValue) {
        if (spiritValue < 100.0D) {
            return 2;
        }
        if (spiritValue < 500.0D) {
            return 3;
        }
        if (spiritValue < 5_000.0D) {
            return 4;
        }
        if (spiritValue < 10_000.0D) {
            return 5;
        }
        if (spiritValue < 20_000.0D) {
            return 6;
        }
        return 7;
    }

    public static double overreachSuccessChance(final int allowedTier, final int actualTier, final int rebirthCount) {
        final int tiersAboveLimit = Math.max(0, actualTier - allowedTier);
        return Math.max(0.05D, Math.min(0.95D, 0.50D - tiersAboveLimit * 0.05D + rebirthCount * 0.05D));
    }

    // ---- XP Multipliers ----

    /**
     * Returns the XP gain multiplier from innate stats.
     *
     * <p>Each stat point above 10 adds 5 %; each stat point below 10 removes 5 %.
     * Capped at a minimum of 0 (can't give negative XP).
     *
     * @param spiritValue the player's current Spirit stat value
     */
    public static double innateStatXpMultiplier(double spiritValue) {
        double delta = spiritValue - 10.0;
        return Math.max(0.0, 1.0 + delta * 0.05);
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
        double damage  = Stats.getDamage(player);
        double health  = Stats.getHealth(player);
        double defense = Stats.getDefense(player);
        double speed   = Stats.getSpeed(player);
        double spirit  = Stats.getSpirit(player);

        Stats.addDamage(player,  regularStatIncrease(damage,  newLevel) - damage);
        Stats.addHealth(player,  regularStatIncrease(health,  newLevel) - health);
        Stats.addDefense(player, regularStatIncrease(defense, newLevel) - defense);
        Stats.addSpeed(player,   regularStatIncrease(speed,   newLevel) - speed);
        Stats.addSpirit(player,  regularStatIncrease(spirit,  newLevel) - spirit);
    }

    /**
     * Applies the breakthrough multiplier stat boost to the player.
     *
     * @param player the player
     * @param breakthroughLevel the level that triggered the breakthrough (11, 21, …, 91)
     */
    public static void applyBreakthroughStats(Player player, int breakthroughLevel) {
        double damage  = Stats.getDamage(player);
        double health  = Stats.getHealth(player);
        double defense = Stats.getDefense(player);
        double speed   = Stats.getSpeed(player);
        double spirit  = Stats.getSpirit(player);

        Stats.addDamage(player,  breakthroughStatValue(damage,  breakthroughLevel) - damage);
        Stats.addHealth(player,  breakthroughStatValue(health,  breakthroughLevel) - health);
        Stats.addDefense(player, breakthroughStatValue(defense, breakthroughLevel) - defense);
        Stats.addSpeed(player,   breakthroughStatValue(speed,   breakthroughLevel) - speed);
        Stats.addSpirit(player,  breakthroughStatValue(spirit,  breakthroughLevel) - spirit);
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
        var abilities = player.getAbilities();
        if (level >= 90) {
            if (!abilities.mayfly) {
                abilities.mayfly = true;
                player.onUpdateAbilities();
            }
        } else if (level >= 70) {
            // Elytra-like flight: player can initiate glide from the air.
            // We keep mayfly off to prevent hovering, but allow fall-flying.
            if (abilities.mayfly) {
                abilities.mayfly = false;
                abilities.flying = false;
                player.onUpdateAbilities();
            }
        } else {
            // Remove cultivation-granted flight if the player somehow lost levels
            if (abilities.mayfly && !player.isCreative() && !player.isSpectator()) {
                abilities.mayfly = false;
                abilities.flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    // ---- Elytra Glide Helper ----

    /**
     * At level 70–89, allow the player to start fall-flying (gliding) when they jump
     * while already airborne and not in water/lava. Called from the player tick.
     */
    public static void tickElytraGlide(Player player, int level) {
        if (player.level().isClientSide()) return;
        if (level < 70 || level >= 90) return;
        // Only try to start gliding if the player is falling (negative Y velocity)
        if (!player.onGround() && !player.isInWater() && !player.isFallFlying()) {
            if (player.getDeltaMovement().y < -0.1) {
                player.startFallFlying();
            }
        }
    }
}
