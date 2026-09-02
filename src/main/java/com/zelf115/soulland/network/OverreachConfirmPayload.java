package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The player accepted the overreach gamble for the ring held in {@code hand}. */
public record OverreachConfirmPayload(int hand) implements CustomPacketPayload {

    public static final Type<OverreachConfirmPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "overreach_confirm"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OverreachConfirmPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, OverreachConfirmPayload::hand, OverreachConfirmPayload::new);

    @Override
    public Type<OverreachConfirmPayload> type() {
        return TYPE;
    }
}
