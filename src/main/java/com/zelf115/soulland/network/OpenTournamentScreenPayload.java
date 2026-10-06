package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * What the tournament registry at {@code pos} shows: the round the run is on (0 before it starts),
 * whether this period's run is over, how long until the next one, and whether an opponent already waits.
 */
public record OpenTournamentScreenPayload(BlockPos pos, int round, boolean spent, long millisUntilReset,
                                          boolean opponentWaiting) implements CustomPacketPayload {
    public static final Type<OpenTournamentScreenPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "open_tournament_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTournamentScreenPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, OpenTournamentScreenPayload::pos,
                    ByteBufCodecs.VAR_INT, OpenTournamentScreenPayload::round,
                    ByteBufCodecs.BOOL, OpenTournamentScreenPayload::spent,
                    ByteBufCodecs.VAR_LONG, OpenTournamentScreenPayload::millisUntilReset,
                    ByteBufCodecs.BOOL, OpenTournamentScreenPayload::opponentWaiting,
                    OpenTournamentScreenPayload::new);

    @Override
    public Type<OpenTournamentScreenPayload> type() {
        return TYPE;
    }
}
