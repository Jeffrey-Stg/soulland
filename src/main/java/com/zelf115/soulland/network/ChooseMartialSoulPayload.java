package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The player picked a martial soul (by enum ordinal) from the selection screen. */
public record ChooseMartialSoulPayload(int martialSoulOrdinal) implements CustomPacketPayload {
    public static final Type<ChooseMartialSoulPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "choose_martial_soul"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChooseMartialSoulPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, ChooseMartialSoulPayload::martialSoulOrdinal, ChooseMartialSoulPayload::new);

    @Override
    public Type<ChooseMartialSoulPayload> type() {
        return TYPE;
    }
}
