package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Tells the client to open the martial soul picker (first pick or a twin-soul bonus pick). */
public record OpenMartialSoulPickerPayload() implements CustomPacketPayload {
    public static final Type<OpenMartialSoulPickerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "open_martial_soul_picker"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenMartialSoulPickerPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenMartialSoulPickerPayload());

    @Override
    public Type<OpenMartialSoulPickerPayload> type() {
        return TYPE;
    }
}
