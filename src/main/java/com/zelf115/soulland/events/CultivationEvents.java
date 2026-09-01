package com.zelf115.soulland.events;

import com.zelf115.soulland.Cultivation;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.cultivation.BreakthroughManager;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
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
        final Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        final long gameTick = player.level().getGameTime();

        // -- Resolve pending special breakthrough (player survived the lightning) --
        if (BreakthroughManager.hasPendingSpecialBreakthrough(player) && player.isAlive()) {
            // We wait one full second after the bolt before confirming survival to let
            // damage processing complete.
            final long strikeTime = player.getPersistentData().getLong(Cultivation.SPECIAL_BREAKTHROUGH_STRIKE_TICK_KEY);
            if (strikeTime > 0 && gameTick - strikeTime >= CultivationManager.TPS) {
                BreakthroughManager.resolveSpecialBreakthrough(player, data);
                player.getPersistentData().remove(Cultivation.SPECIAL_BREAKTHROUGH_STRIKE_TICK_KEY);
            }
        }

        Stats.syncDerivedPlayerStats(player, data);
        regenerateSpiritEnergy(player, data);
        final int level = data.getLevel();

        // -- Flight abilities --
        CultivationManager.applyFlightAbilities(player, level);

        // -- Elytra-like glide at level 70–89 --
        CultivationManager.tickElytraGlide(player, level);

        // -- Meditation XP (when sneaking and not moving) --
        if (player.hasEffect(SoulLand.MEDITATION_EFFECT)) {
            tickMeditation(player, data, gameTick);
        }
    }

    /**
     * Awards XP when the player kills a spirit beast (any hostile mob for now).
     * The reward scales with the beast's tier relative to the player's tier.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        final LivingEntity victim = event.getEntity();

        // Only reward XP for spirit beasts (hostile mobs)
        if (!(victim instanceof Monster)) return;
        final Monster spiritBeast = (Monster) victim;

        // Find the player responsible for the kill
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        final int playerLevel = data.getLevel();
        final int playerTier = data.getPlayerTier();
        final int beastTier = SpiritBeastManager.getTier(spiritBeast);

        final double xpReward = CultivationManager.spiritBeastXpReward(playerLevel, playerTier, beastTier);

        // Apply innate-stat and region-qi multipliers
        final double spiritValue = Stats.getSpirit(player);
        final double innateMultiplier = CultivationManager.innateStatXpMultiplier(spiritValue);
        final int regionQi = getRegionQi(player);
        final double qiMultiplier = CultivationManager.regionQiMultiplier(regionQi);
        final double cultivationSpeedMultiplier = getCultivationSpeedMultiplier(player);

        final double finalXp = xpReward * innateMultiplier * qiMultiplier * cultivationSpeedMultiplier;
        addXpAndCheckLevelUp(player, data, finalXp);
    }

    /**
     * Re-applies flight abilities when a player logs in.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        final Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        CultivationManager.applyFlightAbilities(player, data.getLevel());
        Stats.syncDerivedPlayerStats(player, data);
    }

    /**
     * Re-applies flight abilities when a player respawns.
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        final Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        CultivationManager.applyFlightAbilities(player, data.getLevel());
        Stats.syncDerivedPlayerStats(player, data);

        // Cancel any pending special breakthrough (player died during it)
        if (BreakthroughManager.hasPendingSpecialBreakthrough(player)) {
            BreakthroughManager.cancelSpecialBreakthroughOnDeath(player);
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(final LivingIncomingDamageEvent event) {
        final LivingEntity victim = event.getEntity();
        float updatedAmount = event.getAmount();

        if (event.getSource().getEntity() instanceof Player attackingPlayer) {
            updatedAmount = (float) Stats.applyOutgoingDamageBonus(updatedAmount, Stats.getDamage(attackingPlayer));
        } else if (event.getSource().getEntity() instanceof Monster spiritBeast) {
            SpiritBeastManager.ensureSpiritBeast(spiritBeast);
            updatedAmount = (float) Stats.applyOutgoingDamageBonus(updatedAmount, SpiritBeastManager.getDamageStat(spiritBeast));
        }

        if (victim instanceof Player defendingPlayer) {
            updatedAmount = Stats.applyDefenseReduction(updatedAmount, Stats.getDefense(defendingPlayer));
        } else if (victim instanceof Monster spiritBeast) {
            updatedAmount = Stats.applyDefenseReduction(updatedAmount, SpiritBeastManager.getDefenseStat(spiritBeast));
        }

        event.setAmount(updatedAmount);
    }

    // ---- Helper Methods ----

    /**
     * Handles passive meditation XP accumulation and level-up checks.
     * Also applies the spirit-only bottleneck boost once per minute.
     */
    private static void tickMeditation(Player player, CultivationData data, long gameTick) {
        // Use a simple counter stored in persistent data to throttle ticks
        final long lastMeditationTick = player.getPersistentData().getLong(Cultivation.LAST_MEDITATION_TICK_KEY);

        // -- Regular meditation XP every MEDITATION_TICK_INTERVAL --
        if (gameTick - lastMeditationTick >= CultivationManager.MEDITATION_TICK_INTERVAL) {
            player.getPersistentData().putLong(Cultivation.LAST_MEDITATION_TICK_KEY, gameTick);

            final double spiritValue = Stats.getSpirit(player);
            final double innateMultiplier = CultivationManager.innateStatXpMultiplier(spiritValue);
            final int regionQi = getRegionQi(player);
            final double qiMultiplier = CultivationManager.regionQiMultiplier(regionQi);
            final double cultivationSpeedMultiplier = getCultivationSpeedMultiplier(player);

            final double xpGain = CultivationManager.MEDITATION_XP_PER_TICK
                    * innateMultiplier * qiMultiplier * cultivationSpeedMultiplier;

            if (data.isInBottleneck()) {
                // During a bottleneck, XP still stacks (stored in data.xp for future use),
                // but only the Spirit stat increases (once per minute).
                data.addXp(xpGain);

                final long lastSpiritTick = player.getPersistentData().getLong(Cultivation.LAST_SPIRIT_TICK_KEY);
                if (gameTick - lastSpiritTick >= CultivationManager.TICKS_PER_MINUTE) {
                    player.getPersistentData().putLong(Cultivation.LAST_SPIRIT_TICK_KEY, gameTick);
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
            final int level = data.getLevel();
            if (level >= CultivationManager.MAX_LEVEL) break;
            if (data.isInBottleneck()) break;

            final double required = CultivationManager.xpRequiredForLevel(level);
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
            final int newLevel = level + 1;
            data.setLevel(newLevel);

            // Apply stats for the new level
            final boolean isBreakthroughLevel = (newLevel % 10 == 1) && newLevel >= 11 && newLevel <= 91;
            if (isBreakthroughLevel) {
                CultivationManager.applyBreakthroughStats(player, newLevel);
            } else {
                CultivationManager.applyRegularLevelStats(player, newLevel);
            }

            Stats.syncDerivedPlayerStats(player, data);
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
        final double cultivationSpeed = Stats.getCultivationSpeed(player);
        return 1.0 + Math.max(0.0, cultivationSpeed / 100.0);
    }

    private static void regenerateSpiritEnergy(final Player player, final CultivationData data) {
        final double maxSpiritEnergy = Stats.getMaxSpiritEnergy(player);
        if (maxSpiritEnergy <= 0.0D || data.getSpiritEnergy() >= maxSpiritEnergy) {
            return;
        }

        final double regenPerTick = Stats.getSpiritEnergyRegenPerSecond(player) / CultivationManager.TPS;
        data.setSpiritEnergy(Math.min(maxSpiritEnergy, data.getSpiritEnergy() + regenPerTick));
    }
}
