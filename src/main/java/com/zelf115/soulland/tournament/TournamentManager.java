package com.zelf115.soulland.tournament;

import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.trial.GodTrialManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Runs the daily tournament ladder: one run per period, ten rounds, one opponent at a time. */
public final class TournamentManager {

    public static final int TOTAL_ROUNDS = 10;
    private static final int FIRST_ROUND = 1;
    private static final int NO_RUN = 0;
    private static final int SPAWN_DISTANCE = 3;
    private static final long MILLIS_PER_MINUTE = 60_000L;
    private static final long MINUTES_PER_HOUR = 60L;
    private static final double HALF_BLOCK = 0.5;
    private static final double OPPONENT_SEARCH_RADIUS = 64.0;

    private TournamentManager() {
    }

    /** The registry block right-click: open the day's run, or send in the next opponent. */
    public static void interact(final ServerPlayer player, final CultivationData data, final BlockPos pos) {
        if (TournamentClock.isNewPeriod(data.getTournamentRunStartedAt())) {
            beginRun(player, data);
        }
        if (data.isTournamentRunSpent()) {
            reportWait(player, data);
            return;
        }
        if (hasOpponentWaiting(player)) {
            player.sendSystemMessage(Component.translatable("soulland.tournament.opponent_waiting"));
            return;
        }

        spawnOpponent(player, data, pos);
    }

    private static void beginRun(final ServerPlayer player, final CultivationData data) {
        data.setTournamentRunStartedAt(TournamentClock.now());
        data.setTournamentRunSpent(false);
        data.setTournamentRound(FIRST_ROUND);
        player.sendSystemMessage(Component.translatable("soulland.tournament.started", TOTAL_ROUNDS));
    }

    private static void reportWait(final ServerPlayer player, final CultivationData data) {
        final long millisLeft = TournamentClock.millisUntilNextPeriod(data.getTournamentRunStartedAt());
        final long minutesLeft = millisLeft / MILLIS_PER_MINUTE;
        player.sendSystemMessage(Component.translatable("soulland.tournament.spent",
                minutesLeft / MINUTES_PER_HOUR, minutesLeft % MINUTES_PER_HOUR));
    }

    private static boolean hasOpponentWaiting(final ServerPlayer player) {
        return findOpponent(player) != null;
    }

    private static SoulMasterEntity findOpponent(final ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(SoulMasterEntity.class, player.getBoundingBox().inflate(OPPONENT_SEARCH_RADIUS))
                .stream()
                .filter(opponent -> player.getUUID().equals(opponent.getChallengerId()))
                .findFirst()
                .orElse(null);
    }

    private static void spawnOpponent(final ServerPlayer player, final CultivationData data, final BlockPos pos) {
        final ServerLevel level = player.serverLevel();
        final SoulMasterEntity opponent = TournamentEntities.SOUL_MASTER.get().create(level);
        if (opponent == null) {
            return;
        }

        final int opponentLevel = SoulMasterStats.rollOpponentLevel(data.getLevel(), player.getRandom());
        opponent.moveTo(pos.getX() + SPAWN_DISTANCE + HALF_BLOCK, pos.getY() + 1, pos.getZ() + HALF_BLOCK,
                player.getYRot(), 0.0F);
        opponent.prepareForDuel(opponentLevel, player.getUUID(), data.getTournamentRound());
        opponent.setTarget(player);
        level.addFreshEntity(opponent);
        player.sendSystemMessage(Component.translatable("soulland.tournament.round",
                data.getTournamentRound(), TOTAL_ROUNDS, opponentLevel));
    }

    /** The challenger won the round: pay out, then advance or crown them. */
    public static void recordOpponentDefeat(final ServerPlayer player, final CultivationData data,
                                            final SoulMasterEntity opponent) {
        if (data.isTournamentRunSpent()) {
            return;
        }
        if (data.getTournamentRound() >= TOTAL_ROUNDS) {
            finishRun(player, data, opponent.getSimulatedLevel());
            return;
        }

        TournamentRewards.grantRoundReward(player, data, opponent.getSimulatedLevel());
        data.setTournamentRound(data.getTournamentRound() + 1);
        player.sendSystemMessage(Component.translatable("soulland.tournament.won_round", data.getTournamentRound()));
    }

    private static void finishRun(final ServerPlayer player, final CultivationData data, final int opponentLevel) {
        TournamentRewards.grantFinalReward(player, data, opponentLevel);
        data.setTournamentRunSpent(true);
        data.setTournamentRound(NO_RUN);
        player.sendSystemMessage(Component.translatable("soulland.tournament.won_tournament"));
        GodTrialManager.recordTournamentWin(player, data);
    }

    /** The challenger fell: the run is over for this period, but the winnings stay won. */
    public static void recordChallengerDefeat(final ServerPlayer player, final CultivationData data) {
        if (data.getTournamentRound() == NO_RUN) {
            return;
        }

        data.setTournamentRunSpent(true);
        data.setTournamentRound(NO_RUN);
        removeOpponent(player);
        player.sendSystemMessage(Component.translatable("soulland.tournament.lost"));
    }

    private static void removeOpponent(final ServerPlayer player) {
        final SoulMasterEntity opponent = findOpponent(player);
        if (opponent != null) {
            opponent.discard();
        }
    }
}
