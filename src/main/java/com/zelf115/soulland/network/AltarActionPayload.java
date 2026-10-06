package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** A button pressed on the altar screen for the altar at {@code pos}. */
public record AltarActionPayload(BlockPos pos, int action) implements CustomPacketPayload {
    public static final int BEGIN = 0;
    public static final int CLAIM = 1;

    public static final Type<AltarActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "altar_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AltarActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, AltarActionPayload::pos,
                    ByteBufCodecs.VAR_INT, AltarActionPayload::action,
                    AltarActionPayload::new);

    @Override
    public Type<AltarActionPayload> type() {
        return TYPE;
    }
}
