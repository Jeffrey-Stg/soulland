package com.zelf115.soulland.cultivation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Handles both regular breakthroughs (soul-ring gates at levels 10, 20, … 90)
 * and special lightning breakthroughs (levels 95–120).
 *
 * <h3>Regular breakthrough rules:</h3>
 * <ul>
 *   <li>Base success chance: {@value #REGULAR_BASE_CHANCE}</li>
 *   <li>+{@value #FAILURE_BONUS_CHANCE} per previous failure</li>
 *   <li>On failure: set a cooldown of {@value #FAILURE_COOLDOWN_TICKS} ticks before the next attempt</li>
 *   <li>On success: advance the player's level and reset the failure counter</li>
 *   <li>The player must have one soul ring per 10-level gate already absorbed</li>
 * </ul>
 *
 * <h3>Special breakthrough rules:</h3>
 * <ul>
 *   <li>Lightning strikes the player for a random amount between 1 and their max health, so both
 *       health and defense decide whether the attempt is survivable</li>
 *   <li>Surviving advances the level; dying does not</li>
 * </ul>
 */
public class BreakthroughManager {

    /** Base success probability for a regular breakthrough (0–1). */
    public static final double REGULAR_BASE_CHANCE = 0.50;
    /** Additional success probability per breakthrough failure. */
    public static final double FAILURE_BONUS_CHANCE = 0.05;
    /** Cooldown in ticks (5 min) imposed after a failed breakthrough attempt. */
    public static final int FAILURE_COOLDOWN_TICKS = 20 * 60 * 5;
    /** Lowest damage the heavenly lightning can roll. */
    private static final float MIN_LIGHTNING_DAMAGE = 1.0F;

    // ---- Shared Preconditions ----

    /**
     * Answers what stands between the player and a breakthrough out of their current level, as the
     * message key explaining it, or {@code null} when nothing does.
     *
     * <p>They must be bottlenecked, off cooldown, holding the XP the gate level costs, carrying the
     * soul rings the gate demands, and — at level 99 — have either a god inheritance or a rebirth
     * behind them.
     */
    public static String breakthroughBlocker(final CultivationData data, final long gameTick) {
        if (!data.isInBottleneck()) {
            return "soulland.cultivation.breakthrough.not_ready";
        }
        if (gameTick < data.getBreakthroughCooldownUntil()) {
            return "soulland.cultivation.breakthrough.cooling_down";
        }

        final int level = data.getLevel();
        if (data.getXp() < CultivationManager.xpRequiredForLevel(level)) {
            return "soulland.cultivation.breakthrough.need_xp";
        }
        if (CultivationManager.requiresSoulRing(level)
                && data.getSoulRingCount() < level / CultivationManager.SOUL_RING_GATE_INTERVAL) {
            return "soulland.cultivation.breakthrough.need_ring";
        }
        if (CultivationManager.requiresLevel100Gate(level) && !hasLevel100Path(data)) {
            return "soulland.cultivation.bottleneck_100";
        }
        return null;
    }

    /** Tells the player why they cannot break through yet, and whether that was the case. */
    private static boolean wasBlocked(final Player player, final CultivationData data, final long gameTick) {
        final String blocker = breakthroughBlocker(data, gameTick);
        if (blocker == null) {
            return false;
        }

        player.sendSystemMessage(Component.translatable(blocker));
        return true;
    }

    /** Level 100 opens to a god inheritance or to anyone who has reborn at least once. */
    private static boolean hasLevel100Path(final CultivationData data) {
        return data.hasGodInheritance() || data.getRebirthCount() >= 1;
    }

    /**
     * Attempts whichever breakthrough the player's level calls for.
     *
     * @return {@code true} if the player advanced
     */
    public static boolean attemptBreakthrough(final Player player, final CultivationData data, final long gameTick) {
        if (!CultivationManager.requiresSpecialBreakthrough(data.getLevel())) {
            return attemptRegularBreakthrough(player, data, gameTick);
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        return attemptSpecialBreakthrough(player, data, serverLevel, gameTick);
    }

    // ---- Regular Breakthrough ----

    /**
     * Attempts a regular breakthrough for the given player.
     *
     * @param player   the player attempting the breakthrough
     * @param data     the player's cultivation data (mutated in-place)
     * @param gameTick the current server game tick
     * @return {@code true} if the breakthrough succeeded
     */
    public static boolean attemptRegularBreakthrough(Player player, CultivationData data, long gameTick) {
        if (wasBlocked(player, data, gameTick)) {
            return false;
        }

        final double successChance =
                Math.min(1.0, REGULAR_BASE_CHANCE + data.getBreakthroughFailures() * FAILURE_BONUS_CHANCE);
        if (player.getRandom().nextDouble() >= successChance) {
            recordFailure(player, data, gameTick);
            return false;
        }

        data.setBreakthroughFailures(0);
        advancePastBottleneck(player, data);
        return true;
    }

    private static void recordFailure(final Player player, final CultivationData data, final long gameTick) {
        data.setBreakthroughFailures(data.getBreakthroughFailures() + 1);
        data.setBreakthroughCooldownUntil(gameTick + FAILURE_COOLDOWN_TICKS);
        final long totalBonusPercent = Math.round(data.getBreakthroughFailures() * FAILURE_BONUS_CHANCE * 100.0);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.breakthrough.failed", totalBonusPercent));
    }

    // ---- Special (Lightning) Breakthrough ----

    /**
     * Runs a special breakthrough for levels 95–120: lightning strikes the player for a random
     * share of their max health, and the level is granted only if they are still standing.
     *
     * @return {@code true} if the player survived and advanced
     */
    public static boolean attemptSpecialBreakthrough(final Player player, final CultivationData data,
                                                     final ServerLevel serverLevel, final long gameTick) {
        if (wasBlocked(player, data, gameTick)) {
            return false;
        }

        player.sendSystemMessage(Component.translatable("soulland.cultivation.breakthrough.lightning_start"));
        strikeVisually(player, serverLevel);
        player.hurt(serverLevel.damageSources().lightningBolt(), rollLightningDamage(player));

        if (!player.isAlive()) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.breakthrough.died"));
            return false;
        }

        advancePastBottleneck(player, data);
        return true;
    }

    /** Damage is uniform between 1 and the player's max health, so defense decides survival. */
    private static float rollLightningDamage(final Player player) {
        final float span = Math.max(0.0F, player.getMaxHealth() - MIN_LIGHTNING_DAMAGE);
        return MIN_LIGHTNING_DAMAGE + player.getRandom().nextFloat() * span;
    }

    private static void strikeVisually(final Player player, final ServerLevel serverLevel) {
        final LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (bolt == null) {
            return;
        }

        final BlockPos pos = player.blockPosition();
        bolt.moveTo(Vec3.atBottomCenterOf(pos));
        // The heavenly tribulation deals its own health-scaled damage, so the bolt is spectacle only.
        bolt.setVisualOnly(true);
        serverLevel.addFreshEntity(bolt);
    }

    // ---- Shared Advancement ----

    /**
     * Spends the gate level's XP, advances one level with the breakthrough stat multiplier, and
     * lets any XP banked during the bottleneck cascade into further levels.
     */
    private static void advancePastBottleneck(final Player player, final CultivationData data) {
        final int gateLevel = data.getLevel();
        data.setXp(data.getXp() - CultivationManager.xpRequiredForLevel(gateLevel));
        final int newLevel = gateLevel + 1;
        data.setLevel(newLevel);
        data.setInBottleneck(false);
        CultivationManager.applyBreakthroughStats(player, newLevel);
        CultivationManager.applyFlightAbilities(player, newLevel);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.breakthrough.success", newLevel));
        CultivationManager.grantXp(player, data, 0.0);
    }
}
