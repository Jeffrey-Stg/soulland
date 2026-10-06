package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.block.GodAltarBlock;
import com.zelf115.soulland.block.TournamentRegistryBlock;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.tournament.TournamentManager;
import com.zelf115.soulland.trial.GodTrialManager;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * The screens behind the tournament registry and the god altars: opening them on the client, and
 * acting on their buttons on the server once the player is shown to stand at that very block.
 */
public final class StationNetwork {

    /** Extra reach allowed past the player's own, for the latency between click and packet. */
    private static final double REACH_MARGIN = 1.0;

    private StationNetwork() {
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(OpenTournamentScreenPayload.TYPE, OpenTournamentScreenPayload.STREAM_CODEC, StationNetwork::handleOpenTournament)
                .playToServer(TournamentFightPayload.TYPE, TournamentFightPayload.STREAM_CODEC, StationNetwork::handleTournamentFight)
                .playToClient(OpenAltarScreenPayload.TYPE, OpenAltarScreenPayload.STREAM_CODEC, StationNetwork::handleOpenAltar)
                .playToServer(AltarActionPayload.TYPE, AltarActionPayload.STREAM_CODEC, StationNetwork::handleAltarAction);
    }

    private static void handleOpenTournament(final OpenTournamentScreenPayload payload, final IPayloadContext context) {
        // Resolved inside the lambda so the dedicated server never loads the client screen class.
        context.enqueueWork(() -> com.zelf115.soulland.client.station.TournamentScreen.open(payload));
    }

    private static void handleOpenAltar(final OpenAltarScreenPayload payload, final IPayloadContext context) {
        // Resolved inside the lambda so the dedicated server never loads the client screen class.
        context.enqueueWork(() -> com.zelf115.soulland.client.station.GodAltarScreen.open(payload));
    }

    private static void handleTournamentFight(final TournamentFightPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (reachableBlock(player, payload.pos(), TournamentRegistryBlock.class).isPresent()) {
                TournamentManager.fight(player, dataOf(player), payload.pos());
            }
        });
    }

    private static void handleAltarAction(final AltarActionPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            reachableBlock(player, payload.pos(), GodAltarBlock.class).ifPresent(altar -> {
                final CultivationData data = dataOf(player);
                switch (payload.action()) {
                    case AltarActionPayload.BEGIN -> GodTrialManager.begin(player, data, altar.trial());
                    case AltarActionPayload.CLAIM -> GodTrialManager.claimOneReward(player, data, altar.trial());
                    default -> SoulLand.LOGGER.warn("Ignoring unknown altar action {}", payload.action());
                }
                GodTrialManager.open(player, data, altar.trial(), payload.pos());
            });
        });
    }

    /** The block at {@code pos}, when it is of the expected kind and within the player's reach. */
    private static <T extends Block> Optional<T> reachableBlock(final ServerPlayer player, final BlockPos pos,
                                                                final Class<T> kind) {
        if (!player.canInteractWithBlock(pos, REACH_MARGIN)) {
            return Optional.empty();
        }
        final Block block = player.level().getBlockState(pos).getBlock();
        return kind.isInstance(block) ? Optional.of(kind.cast(block)) : Optional.empty();
    }

    private static CultivationData dataOf(final ServerPlayer player) {
        return player.getData(CultivationAttachment.CULTIVATION_DATA.get());
    }
}
