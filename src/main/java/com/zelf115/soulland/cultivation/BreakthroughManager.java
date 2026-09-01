package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.Cultivation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * Handles both regular breakthroughs (soul-ring gates at levels 10, 20, … 90)
 * and special lightning breakthroughs (levels 95–120).
 *
 * <h3>Regular breakthrough rules:</h3>
 * <ul>
 *   <li>Base success chance: {@value #REGULAR_BASE_CHANCE} %</li>
 *   <li>+{@value #FAILURE_BONUS_CHANCE} % per previous failure</li>
 *   <li>On failure: set a cooldown of {@value #FAILURE_COOLDOWN_TICKS} ticks before the next attempt</li>
 *   <li>On success: advance the player's level and reset failure counter</li>
 *   <li>The player must have at least one soul ring per 10-level gate</li>
 * </ul>
 *
 * <h3>Special breakthrough rules:</h3>
 * <ul>
 *   <li>Summons a lightning bolt on the player when the attempt begins</li>
 *   <li>Lightning damage is a random value between 1 and the player's current max HP (vanilla)</li>
 *   <li>If the player survives, the level increases; if they die the level does <em>not</em> increase</li>
 *   <li>A pending-special-breakthrough flag is set; the actual level-up check happens in the
 *       player-death event (cancel) or on survival confirmation (next tick alive)</li>
 * </ul>
 */
public class BreakthroughManager {

    /** Base success probability for a regular breakthrough (0–1). */
    public static final double REGULAR_BASE_CHANCE = 0.50;
    /** Additional success probability per breakthrough failure. */
    public static final double FAILURE_BONUS_CHANCE = 0.05;
    /** Cooldown in ticks (30 s) imposed after a failed breakthrough attempt. */
    public static final int FAILURE_COOLDOWN_TICKS = 20 * 30;

    private static final Random RANDOM = new Random();

    // ---- Regular Breakthrough ----

    /**
     * Returns whether the player meets the prerequisites to attempt a regular
     * breakthrough from {@code currentLevel} to the next stage.
     *
     * <p>Prerequisites:
     * <ul>
     *   <li>Player is in a bottleneck</li>
     *   <li>Cooldown has expired</li>
     *   <li>Player has enough soul rings (soulRingCount ≥ required count)</li>
     * </ul>
     *
     * @param data      the player's cultivation data
     * @param gameTick  the current server game tick
     * @return {@code true} if the attempt is allowed
     */
    public static boolean canAttemptRegularBreakthrough(CultivationData data, long gameTick) {
        if (!data.isInBottleneck()) return false;
        if (gameTick < data.getBreakthroughCooldownUntil()) return false;
        int level = data.getLevel();
        if (CultivationManager.requiresSoulRing(level)) {
            int requiredRings = level / CultivationManager.SOUL_RING_GATE_INTERVAL;
            if (data.getSoulRingCount() < requiredRings) return false;
        }
        return true;
    }

    /**
     * Attempts a regular breakthrough for the given player.
     *
     * @param player   the player attempting the breakthrough
     * @param data     the player's cultivation data (mutated in-place)
     * @param gameTick the current server game tick
     * @return {@code true} if the breakthrough succeeded
     */
    public static boolean attemptRegularBreakthrough(Player player, CultivationData data, long gameTick) {
        if (!canAttemptRegularBreakthrough(data, gameTick)) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.breakthrough.not_ready"));
            return false;
        }

        double successChance = REGULAR_BASE_CHANCE + data.getBreakthroughFailures() * FAILURE_BONUS_CHANCE;
        successChance = Math.min(1.0, successChance);

        if (RANDOM.nextDouble() < successChance) {
            // Success
            int newLevel = data.getLevel() + 1;
            data.setLevel(newLevel);
            data.setInBottleneck(false);
            data.setBreakthroughFailures(0);
            CultivationManager.applyBreakthroughStats(player, newLevel);
            CultivationManager.applyFlightAbilities(player, newLevel);
            player.sendSystemMessage(Component.translatable("soulland.cultivation.breakthrough.success", newLevel));
            return true;
        } else {
            // Failure
            data.setBreakthroughFailures(data.getBreakthroughFailures() + 1);
            data.setBreakthroughCooldownUntil(gameTick + FAILURE_COOLDOWN_TICKS);
            int totalBonus = data.getBreakthroughFailures() * 5;
            player.sendSystemMessage(Component.translatable(
                    "soulland.cultivation.breakthrough.failed",
                    totalBonus));
            return false;
        }
    }

    // ---- Special (Lightning) Breakthrough ----

    /**
     * Initiates a special breakthrough for levels 95–120 by summoning a lightning bolt
     * on the player. The level-up itself happens in {@link #resolveSpecialBreakthrough}
     * once it is confirmed the player survived.
     *
     * @param player    the player
     * @param data      the player's cultivation data
     * @param serverLevel the server level
     */
    public static void beginSpecialBreakthrough(Player player, CultivationData data, ServerLevel serverLevel) {
        if (!data.isInBottleneck()) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.breakthrough.not_ready"));
            return;
        }

        // Summon a lightning bolt at the player's position
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (bolt != null) {
            BlockPos pos = player.blockPosition();
            bolt.moveTo(Vec3.atBottomCenterOf(pos));
            bolt.setVisualOnly(false); // causes real damage
            serverLevel.addFreshEntity(bolt);
        }

        // The player is now "mid-breakthrough"; surviving (checked on next player tick)
        // will trigger resolveSpecialBreakthrough.
        player.getPersistentData().putBoolean(Cultivation.PENDING_SPECIAL_BREAKTHROUGH_KEY, true);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.breakthrough.lightning_start"));
    }

    /**
     * Called after the player survives a special breakthrough lightning strike.
     * Advances the level and applies breakthrough stats.
     *
     * @param player the surviving player
     * @param data   the player's cultivation data
     */
    public static void resolveSpecialBreakthrough(Player player, CultivationData data) {
        player.getPersistentData().remove(Cultivation.PENDING_SPECIAL_BREAKTHROUGH_KEY);
        int newLevel = data.getLevel() + 1;
        data.setLevel(newLevel);
        data.setInBottleneck(false);
        CultivationManager.applyBreakthroughStats(player, newLevel);
        CultivationManager.applyFlightAbilities(player, newLevel);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.breakthrough.success", newLevel));
    }

    /**
     * Called when a player with a pending special breakthrough dies. Cancels the
     * level-up and clears the pending flag.
     *
     * @param player the deceased player
     */
    public static void cancelSpecialBreakthroughOnDeath(Player player) {
        player.getPersistentData().remove(Cultivation.PENDING_SPECIAL_BREAKTHROUGH_KEY);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.breakthrough.died"));
    }

    /**
     * Returns {@code true} if the player has a pending special breakthrough waiting
     * for survival confirmation.
     */
    public static boolean hasPendingSpecialBreakthrough(Player player) {
        return player.getPersistentData().getBoolean(Cultivation.PENDING_SPECIAL_BREAKTHROUGH_KEY);
    }
}
