package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The player pressed Fight at the tournament registry at {@code pos}. */
public record TournamentFightPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<TournamentFightPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "tournament_fight"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TournamentFightPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, TournamentFightPayload::pos, TournamentFightPayload::new);

    @Override
    public Type<TournamentFightPayload> type() {
        return TYPE;
    }
}
