package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The share of the Speed stat, in percent, the player set on the settings slider. */
public record SetMovementUsagePayload(int percent) implements CustomPacketPayload {
    public static final Type<SetMovementUsagePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "set_movement_usage"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetMovementUsagePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, SetMovementUsagePayload::percent, SetMovementUsagePayload::new);

    @Override
    public Type<SetMovementUsagePayload> type() {
        return TYPE;
    }
}
