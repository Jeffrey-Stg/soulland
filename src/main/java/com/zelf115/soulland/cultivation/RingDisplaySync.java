package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.network.RingDisplayPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Pushes a player's visible soul rings to the clients that draw them. */
public final class RingDisplaySync {

    private RingDisplaySync() {
    }

    /** Sends the player's rings to everyone tracking them, and to the player. */
    public static void broadcast(final ServerPlayer player) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payloadFor(player));
    }

    /** Catches one viewer up on a player who just came into view. */
    public static void sendTo(final ServerPlayer viewer, final ServerPlayer subject) {
        PacketDistributor.sendToPlayer(viewer, payloadFor(subject));
    }

    private static RingDisplayPayload payloadFor(final ServerPlayer player) {
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        return new RingDisplayPayload(player.getUUID(),
                data.visibleRings().stream().map(AbsorbedRing::tier).toList());
    }
}
