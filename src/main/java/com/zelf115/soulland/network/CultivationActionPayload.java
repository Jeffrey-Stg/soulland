package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record CultivationActionPayload(int action) implements CustomPacketPayload {
    public static final Type<CultivationActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "cultivation_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CultivationActionPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, CultivationActionPayload::action, CultivationActionPayload::new);

    public static final int START_MEDITATION = 0;
    public static final int INCREASE_SPEED = 1;
    public static final int DECREASE_SPEED = 2;
    public static final int CYCLE_RING_DISPLAY = 3;
    public static final int TOGGLE_EXTERNAL_BONE = 4;
    public static final int ATTEMPT_BREAKTHROUGH = 5;
    public static final int USE_MARTIAL_SOUL = 6;
    public static final int CAST_MARTIAL_SOUL = 7;
    public static final int OPEN_ALCHEMY_MENU = 8;
    public static final int OPEN_MARTIAL_SOUL_MENU = 9;

    @Override
    public Type<CultivationActionPayload> type() {
        return TYPE;
    }
}
