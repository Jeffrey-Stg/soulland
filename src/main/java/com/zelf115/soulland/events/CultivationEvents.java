package com.zelf115.soulland.events;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.AbsorbedRing;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.skill.MeleeRiders;
import com.zelf115.soulland.cultivation.skill.SkillEffects;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.cultivation.MartialSoulAbility;
import com.zelf115.soulland.cultivation.RingDisplaySync;
import com.zelf115.soulland.cultivation.technique.MysteriousHaven;
import com.zelf115.soulland.cultivation.technique.PurpleDemonEye;
import com.zelf115.soulland.item.GodRelic;
import com.zelf115.soulland.item.MartialSoulSwordItem;
import com.zelf115.soulland.network.HudSyncPayload;
import com.zelf115.soulland.qi.QiManager;
import com.zelf115.soulland.spirit.SpiritBeastEntity;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import com.zelf115.soulland.tournament.SoulMasterEntity;
import com.zelf115.soulland.tournament.TournamentManager;
import com.zelf115.soulland.trial.GodTrialManager;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Handles all cultivation-related game events:
 * <ul>
 *   <li>Passive meditation XP gain (player tick)</li>
 *   <li>Spirit-stat boost during bottleneck meditation</li>
 *   <li>Flight ability reapplication on tick</li>
 *   <li>Spirit-beast kill XP rewards</li>
 *   <li>Stat-driven damage dealt and taken</li>
 *   <li>Ability restoration on player respawn and login</li>
 * </ul>
 */
@EventBusSubscriber(modid = SoulLand.MODID)
public class CultivationEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        final Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        final long gameTick = player.level().getGameTime();

        Stats.syncDerivedPlayerStats(player, data);
        regenerateSpiritEnergy(player, data);
        MartialSoulAbility.tick(player, data, gameTick);
        PurpleDemonEye.tick(player, data, gameTick);

        final int level = data.getLevel();
        CultivationManager.applyFlightAbilities(player, level);

        if (player.hasEffect(SoulLand.MEDITATION_EFFECT)) {
            tickMeditation(player, data, gameTick);
        }

        if (player instanceof ServerPlayer serverPlayer && gameTick % CultivationManager.HUD_SYNC_INTERVAL_TICKS == 0) {
            syncHud(serverPlayer, data);
        }
    }

    private static void syncHud(final ServerPlayer player, final CultivationData data) {
        // The player's own panel shows every ring they hold. visibleRings() is the choice of what
        // other players get to see, and it starts out hidden, which would leave this panel blank.
        final List<AbsorbedRing> rings = data.getAbsorbedRings();
        final AbsorbedRing currentRing = rings.isEmpty() ? null : rings.get(rings.size() - 1);
        final List<Integer> ringTiers = rings.stream().map(AbsorbedRing::tier).toList();
        final HudSyncPayload.Gauge xp = new HudSyncPayload.Gauge(data.getXp(), CultivationManager.xpRequiredForLevel(data.getLevel()));
        final HudSyncPayload.Gauge spiritEnergy = new HudSyncPayload.Gauge(data.getSpiritEnergy(), Stats.getMaxSpiritEnergy(player));
        final HudSyncPayload.SoulBeast soulBeast = new HudSyncPayload.SoulBeast(
                currentRing == null ? "" : currentRing.sourceName(), currentRing == null ? 0 : currentRing.tier());
        PacketDistributor.sendToPlayer(player, new HudSyncPayload(
                data.getLevel(), xp, data.isInBottleneck(), spiritEnergy, getRegionQi(player), soulBeast, ringTiers));
    }

    /**
     * Awards XP when the player kills a registered spirit beast.
     * The reward scales with the beast's tier relative to the player's tier.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        final LivingEntity victim = event.getEntity();

        if (victim instanceof SoulMasterEntity opponent) {
            recordTournamentResult(event, opponent);
            return;
        }
        if (victim instanceof ServerPlayer fallenChallenger) {
            TournamentManager.recordChallengerDefeat(fallenChallenger,
                    fallenChallenger.getData(CultivationAttachment.CULTIVATION_DATA.get()));
            return;
        }

        // Only reward XP for registered spirit beasts.
        if (!(victim instanceof SpiritBeastEntity spiritBeast)) return;

        // Find the player responsible for the kill
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        final double xpReward = CultivationManager.spiritBeastXpReward(
                data.getLevel(), data.getPlayerTier(), SpiritBeastManager.getTier(spiritBeast));

        CultivationManager.grantXp(player, data, xpReward * baseXpMultiplier(player, data));
        if (player instanceof ServerPlayer serverPlayer) {
            GodTrialManager.recordBeastKill(serverPlayer, data, spiritBeast);
        }
    }

    /** Only the challenger who was sent this opponent may claim the round. */
    private static void recordTournamentResult(final LivingDeathEvent event, final SoulMasterEntity opponent) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!player.getUUID().equals(opponent.getChallengerId())) return;

        TournamentManager.recordOpponentDefeat(player, player.getData(CultivationAttachment.CULTIVATION_DATA.get()),
                opponent);
    }

    /**
     * A martial soul's tool isn't loot: dying deactivates the ability instead of dropping it.
     */
    @SubscribeEvent
    public static void onLivingDrops(final LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        if (!data.isMartialSoulActive()) return;

        event.getDrops().removeIf(itemEntity -> itemEntity.getItem().getItem() instanceof MartialSoulSwordItem);
        MartialSoulAbility.forceDeactivate(player, data);
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

        if (!data.isSpiritEnergySeeded()) {
            data.setSpiritEnergy(Stats.getMaxSpiritEnergy(player));
            data.markSpiritEnergySeeded();
        }
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
    }

    /** A viewer who just started rendering a player needs that player's rings straight away. */
    @SubscribeEvent
    public static void onStartTracking(final PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof ServerPlayer subject) {
            RingDisplaySync.sendTo(viewer, subject);
        }
    }

    /** A fresh client, a respawned body and a new dimension all begin with an empty ring cache. */
    @SubscribeEvent
    public static void onLoginSyncRings(final PlayerEvent.PlayerLoggedInEvent event) {
        broadcastRings(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawnSyncRings(final PlayerEvent.PlayerRespawnEvent event) {
        broadcastRings(event.getEntity());
    }

    @SubscribeEvent
    public static void onChangedDimensionSyncRings(final PlayerEvent.PlayerChangedDimensionEvent event) {
        broadcastRings(event.getEntity());
    }

    private static void broadcastRings(final Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            RingDisplaySync.broadcast(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(final LivingIncomingDamageEvent event) {
        final LivingEntity victim = event.getEntity();
        float updatedAmount = event.getAmount();

        if (event.getSource().getEntity() instanceof Player attackingPlayer) {
            if (GodRelic.isUnearnedRelic(attackingPlayer)) {
                GodRelic.refuse(attackingPlayer);
                event.setCanceled(true);
                return;
            }
            MeleeRiders.applyOnHitEffects(attackingPlayer, victim, event.getSource());
            updatedAmount += MeleeRiders.consumeSmashBonus(attackingPlayer, event.getSource());
            updatedAmount = (float) Stats.applyOutgoingDamageBonus(updatedAmount, Stats.getDamage(attackingPlayer));
        } else if (event.getSource().getEntity() instanceof SpiritBeastEntity spiritBeast) {
            SpiritBeastManager.ensureSpiritBeast(spiritBeast);
            updatedAmount = (float) Stats.applyOutgoingDamageBonus(updatedAmount, SpiritBeastManager.getDamageStat(spiritBeast));
        }

        if (victim instanceof Player defendingPlayer) {
            updatedAmount = Stats.applyDefenseReduction(updatedAmount,
                    SkillEffects.defenseAfterSunder(victim, Stats.getDefense(defendingPlayer)));
        } else if (victim instanceof SpiritBeastEntity spiritBeast) {
            updatedAmount = Stats.applyDefenseReduction(updatedAmount,
                    SkillEffects.defenseAfterSunder(victim, SpiritBeastManager.getDefenseStat(spiritBeast)));
        }

        event.setAmount(updatedAmount);
    }

    // ---- Helper Methods ----

    /**
     * Handles passive meditation XP accumulation and level-up checks.
     * Also applies the spirit-only bottleneck boost once per minute.
     */
    private static void tickMeditation(Player player, CultivationData data, long gameTick) {
        if (gameTick - data.getLastMeditationTick() < CultivationManager.MEDITATION_TICK_INTERVAL) {
            return;
        }
        data.setLastMeditationTick(gameTick);
        MysteriousHaven.trainWhileMeditating(player, data);

        final double xpGain = CultivationManager.MEDITATION_XP_PER_TICK
                * meditationXpMultiplier(player, data);

        final boolean wasInBottleneck = data.isInBottleneck();
        CultivationManager.grantXp(player, data, xpGain);
        if (!wasInBottleneck) {
            return;
        }

        // During a bottleneck XP still banks for the post-breakthrough cascade, but of the stats
        // only Spirit grows, and only once a minute.
        if (gameTick - data.getLastSpiritTick() >= CultivationManager.TICKS_PER_MINUTE) {
            data.setLastSpiritTick(gameTick);
            Stats.addSpirit(player, CultivationManager.SPIRIT_BOTTLENECK_INCREASE_PER_MINUTE);
        }
    }

    private static double meditationXpMultiplier(final Player player, final CultivationData data) {
        return baseXpMultiplier(player, data) * CultivationManager.regionQiMultiplier(getRegionQi(player))
                * MysteriousHaven.cultivationMultiplier(data);
    }

    /** The multipliers that apply wherever the XP came from. */
    private static double baseXpMultiplier(final Player player, final CultivationData data) {
        return CultivationManager.innateStatXpMultiplier(data.getEffectiveInnateStat())
                * cultivationSpeedMultiplier(player);
    }

    /** Returns the region qi value for the chunk the player stands in. */
    private static int getRegionQi(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return QiManager.MIN_QI;
        }
        return QiManager.getQiAt(serverLevel, player.blockPosition());
    }

    /**
     * Returns a cultivation-speed multiplier derived from the player's CultivationSpeed stat.
     * The base value of 0 means no bonus; positive values add a proportional bonus.
     */
    private static double cultivationSpeedMultiplier(Player player) {
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
