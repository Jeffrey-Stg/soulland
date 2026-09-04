package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Asks the client to confirm an overreaching soul ring absorption. */
public record OverreachPromptPayload(int hand, int ringTier, int allowedTier, int successChancePercent)
        implements CustomPacketPayload {

    public static final Type<OverreachPromptPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "overreach_prompt"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OverreachPromptPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, OverreachPromptPayload::hand,
                    ByteBufCodecs.VAR_INT, OverreachPromptPayload::ringTier,
                    ByteBufCodecs.VAR_INT, OverreachPromptPayload::allowedTier,
                    ByteBufCodecs.VAR_INT, OverreachPromptPayload::successChancePercent,
                    OverreachPromptPayload::new);

    @Override
    public Type<OverreachPromptPayload> type() {
        return TYPE;
    }
}
