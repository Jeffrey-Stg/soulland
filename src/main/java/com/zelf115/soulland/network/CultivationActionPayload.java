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
    public static final int CYCLE_RING_DISPLAY = 3;
    public static final int TOGGLE_EXTERNAL_BONE = 4;
    public static final int ATTEMPT_BREAKTHROUGH = 5;
    public static final int USE_MARTIAL_SOUL = 6;
    public static final int CAST_MARTIAL_SOUL = 7;
    public static final int OPEN_ALCHEMY_MENU = 8;
    public static final int SWITCH_MARTIAL_SOUL = 10;
    public static final int SELECT_NEXT_RING = 11;
    public static final int DEMON_EYE = 12;
    public static final int DEMON_EYE_STRIKE = 13;
    public static final int SHADOW_STEP = 14;
    public static final int SELECT_NEXT_BONE = 15;
    public static final int CAST_BONE_SKILL = 16;
    public static final int RELEASE_CHANNEL = 17;
    public static final int TOGGLE_MOD_STATS = 18;

    @Override
    public Type<CultivationActionPayload> type() {
        return TYPE;
    }
}
