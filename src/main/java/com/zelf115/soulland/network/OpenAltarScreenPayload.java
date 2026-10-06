package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Opens the altar screen for the altar at {@code pos}, of the god with the given {@code GodTrial} ordinal. */
public record OpenAltarScreenPayload(BlockPos pos, int trialOrdinal) implements CustomPacketPayload {
    public static final Type<OpenAltarScreenPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "open_altar_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenAltarScreenPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, OpenAltarScreenPayload::pos,
                    ByteBufCodecs.VAR_INT, OpenAltarScreenPayload::trialOrdinal,
                    OpenAltarScreenPayload::new);

    @Override
    public Type<OpenAltarScreenPayload> type() {
        return TYPE;
    }
}
