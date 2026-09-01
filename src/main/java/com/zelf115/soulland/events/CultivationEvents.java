package com.zelf115.soulland.events;

import com.zelf115.soulland.Stats;
import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.cultivation.BreakthroughManager;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Handles all cultivation-related game events:
 * <ul>
 *   <li>Passive meditation XP gain (player tick)</li>
 *   <li>Spirit-stat boost during bottleneck meditation</li>
 *   <li>Flight ability reapplication on tick</li>
 *   <li>Elytra-like gliding (level 70–89)</li>
 *   <li>Spirit-beast kill XP rewards</li>
 *   <li>Pending special-breakthrough survival resolution</li>
 *   <li>Special-breakthrough cancellation on player death</li>
 *   <li>Ability restoration on player respawn and login</li>
 * </ul>
 */
@EventBusSubscriber(modid = SoulLand.MODID)
public class CultivationEvents {

    /**
     * Called every server tick for each player.
     *
     * <p>Responsibilities:
     * <ul>
     *   <li>Meditation XP gain (when sneaking and stationary)</li>
     *   <li>Spirit boost during bottleneck</li>
     *   <li>Flight ability maintenance</li>
     *   <li>Elytra-glide activation at level 70–89</li>
     *   <li>Resolving a pending special breakthrough (player survived the lightning)</li>
     * </ul>
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA);
        int level = data.getLevel();
        long gameTick = player.level().getGameTime();

        // -- Resolve pending special breakthrough (player survived the lightning) --
        if (BreakthroughManager.hasPendingSpecialBreakthrough(player) && player.isAlive()) {
            // We wait one full second after the bolt before confirming survival to let
            // damage processing complete.
            long strikeTime = player.getPersistentData().getLong("soulland_special_bt_strike_tick");
            if (strikeTime > 0 && gameTick - strikeTime >= CultivationManager.TPS) {
                BreakthroughManager.resolveSpecialBreakthrough(player, data);
                player.getPersistentData().remove("soulland_special_bt_strike_tick");
            }
        }

        // -- Flight abilities --
        CultivationManager.applyFlightAbilities(player, level);

        // -- Elytra-like glide at level 70–89 --
        CultivationManager.tickElytraGlide(player, level);

        // -- Meditation XP (when sneaking and not moving) --
        if (player.isCrouching() && isStationary(player)) {
            tickMeditation(player, data, gameTick);
        }
    }

    /**
     * Awards XP when the player kills a spirit beast (any hostile mob for now).
     * The reward scales with the beast's tier relative to the player's tier.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();

        // Only reward XP for spirit beasts (hostile mobs)
        if (!(victim instanceof Monster)) return;

        // Find the player responsible for the kill
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA);
        int playerLevel = data.getLevel();
        int playerTier = data.getPlayerTier();
        int beastTier = CultivationManager.beastTierFromHealth(victim.getMaxHealth());

        double xpReward = CultivationManager.spiritBeastXpReward(playerLevel, playerTier, beastTier);

        // Apply innate-stat and region-qi multipliers
        double spiritValue = Stats.getSpirit(player);
        double innateMultiplier = CultivationManager.innateStatXpMultiplier(spiritValue);
        int regionQi = getRegionQi(player);
        double qiMultiplier = CultivationManager.regionQiMultiplier(regionQi);
        double cultivationSpeedMultiplier = getCultivationSpeedMultiplier(player);

        double finalXp = xpReward * innateMultiplier * qiMultiplier * cultivationSpeedMultiplier;
        addXpAndCheckLevelUp(player, data, finalXp);
    }

    /**
     * Re-applies flight abilities when a player logs in.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA);
        CultivationManager.applyFlightAbilities(player, data.getLevel());
    }

    /**
     * Re-applies flight abilities when a player respawns.
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA);
        CultivationManager.applyFlightAbilities(player, data.getLevel());

        // Cancel any pending special breakthrough (player died during it)
        if (BreakthroughManager.hasPendingSpecialBreakthrough(player)) {
            BreakthroughManager.cancelSpecialBreakthroughOnDeath(player);
        }
    }

    // ---- Helper Methods ----

    /**
     * Handles passive meditation XP accumulation and level-up checks.
     * Also applies the spirit-only bottleneck boost once per minute.
     */
    private static void tickMeditation(Player player, CultivationData data, long gameTick) {
        // Use a simple counter stored in persistent data to throttle ticks
        long lastMeditationTick = player.getPersistentData().getLong("soulland_last_meditation_tick");

        // -- Regular meditation XP every MEDITATION_TICK_INTERVAL --
        if (gameTick - lastMeditationTick >= CultivationManager.MEDITATION_TICK_INTERVAL) {
            player.getPersistentData().putLong("soulland_last_meditation_tick", gameTick);

            double spiritValue = Stats.getSpirit(player);
            double innateMultiplier = CultivationManager.innateStatXpMultiplier(spiritValue);
            int regionQi = getRegionQi(player);
            double qiMultiplier = CultivationManager.regionQiMultiplier(regionQi);
            double cultivationSpeedMultiplier = getCultivationSpeedMultiplier(player);

            double xpGain = CultivationManager.MEDITATION_XP_PER_TICK
                    * innateMultiplier * qiMultiplier * cultivationSpeedMultiplier;

            if (data.isInBottleneck()) {
                // During a bottleneck, XP still stacks (stored in data.xp for future use),
                // but only the Spirit stat increases (once per minute).
                data.addXp(xpGain);

                long lastSpiritTick = player.getPersistentData().getLong("soulland_last_spirit_tick");
                if (gameTick - lastSpiritTick >= CultivationManager.TICKS_PER_MINUTE) {
                    player.getPersistentData().putLong("soulland_last_spirit_tick", gameTick);
                    Stats.addSpirit(player, CultivationManager.SPIRIT_BOTTLENECK_INCREASE_PER_MINUTE);
                }
            } else {
                addXpAndCheckLevelUp(player, data, xpGain);
            }
        }
    }

    /**
     * Adds XP to the player and processes as many level-ups as the accumulated XP allows.
     * If a bottleneck is encountered the XP continues to accumulate but leveling halts.
     */
    public static void addXpAndCheckLevelUp(Player player, CultivationData data, double amount) {
        data.addXp(amount);

        while (true) {
            int level = data.getLevel();
            if (level >= CultivationManager.MAX_LEVEL) break;
            if (data.isInBottleneck()) break;

            double required = CultivationManager.xpRequiredForLevel(level);
            if (data.getXp() < required) break;

            // Check if next level hits a bottleneck gate
            if (CultivationManager.isBottleneckLevel(level)) {
                // Enter bottleneck — stop natural leveling until breakthrough
                data.setInBottleneck(true);
                player.sendSystemMessage(Component.translatable(
                        "soulland.cultivation.bottleneck", level));
                break;
            }

            // Check level-100 gate
            if (CultivationManager.requiresLevel100Gate(level)) {
                if (!data.hasGodInheritance() && data.getRebirthCount() < 1) {
                    data.setInBottleneck(true);
                    player.sendSystemMessage(Component.translatable(
                            "soulland.cultivation.bottleneck_100"));
                    break;
                }
            }

            // Consume XP and advance level
            data.setXp(data.getXp() - required);
            int newLevel = level + 1;
            data.setLevel(newLevel);

            // Apply stats for the new level
            boolean isBreakthroughLevel = (newLevel % 10 == 1) && newLevel >= 11 && newLevel <= 91;
            if (isBreakthroughLevel) {
                CultivationManager.applyBreakthroughStats(player, newLevel);
            } else {
                CultivationManager.applyRegularLevelStats(player, newLevel);
            }

            CultivationManager.applyFlightAbilities(player, newLevel);

            // Notify player
            player.sendSystemMessage(Component.translatable(
                    "soulland.cultivation.level_up", newLevel));

            // Check if the NEW level is a bottleneck gate (so we stop before trying the next)
            if (CultivationManager.isBottleneckLevel(newLevel)) {
                data.setInBottleneck(true);
                player.sendSystemMessage(Component.translatable(
                        "soulland.cultivation.bottleneck", newLevel));
                break;
            }
        }
    }

    /** Returns {@code true} if the player's horizontal movement is negligible. */
    private static boolean isStationary(Player player) {
        var motion = player.getDeltaMovement();
        return Math.abs(motion.x) < 0.01 && Math.abs(motion.z) < 0.01;
    }

    /**
     * Returns the region qi value for the player's current biome.
     * Returns 1 (minimum) as a default until the biome qi system is integrated.
     */
    private static int getRegionQi(Player player) {
        // TODO: integrate with the BiomeQi system from the biome task (PR #19).
        // For now, return default qi level of 1.
        return 1;
    }

    /**
     * Returns a cultivation-speed multiplier derived from the player's CultivationSpeed stat.
     * The base value of 0 means no bonus; positive values add a proportional bonus.
     */
    private static double getCultivationSpeedMultiplier(Player player) {
        double cultivationSpeed = Stats.getCultivationSpeed(player);
        return 1.0 + Math.max(0.0, cultivationSpeed / 100.0);
    }
}
