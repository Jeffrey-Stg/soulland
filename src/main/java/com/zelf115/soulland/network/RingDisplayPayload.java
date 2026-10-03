package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The soul rings one player is showing off, sent to everyone who renders that player. */
public record RingDisplayPayload(UUID playerId, List<Integer> ringTiers) implements CustomPacketPayload {

    public static final Type<RingDisplayPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "ring_display"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RingDisplayPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, RingDisplayPayload::playerId,
                    ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), RingDisplayPayload::ringTiers,
                    RingDisplayPayload::new);

    @Override
    public Type<RingDisplayPayload> type() {
        return TYPE;
    }
}
