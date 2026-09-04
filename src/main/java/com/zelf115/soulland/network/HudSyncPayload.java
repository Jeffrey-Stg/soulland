package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Periodic server-to-client refresh of the cultivation HUD. */
public record HudSyncPayload(int level, Gauge xp, boolean inBottleneck, Gauge spiritEnergy,
                             int qi, SoulBeast soulBeast, List<Integer> ringTiers)
        implements CustomPacketPayload {

    /** A bounded resource shown as a bar: XP toward the next level, or spirit energy toward its cap. */
    public record Gauge(double current, double max) {
    }

    /** The soul beast whose ring most recently joined the belt, or an empty name if none has. */
    public record SoulBeast(String name, int tier) {
    }

    private static final int MAX_SOUL_BEAST_NAME_LENGTH = 64;

    public static final Type<HudSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "hud_sync"));

    // More fields than StreamCodec.composite's six-argument ceiling supports, so this codec is written by hand.
    public static final StreamCodec<RegistryFriendlyByteBuf, HudSyncPayload> STREAM_CODEC = StreamCodec.of(
            HudSyncPayload::write, HudSyncPayload::read);

    private static void write(final RegistryFriendlyByteBuf buf, final HudSyncPayload payload) {
        ByteBufCodecs.VAR_INT.encode(buf, payload.level());
        ByteBufCodecs.DOUBLE.encode(buf, payload.xp().current());
        ByteBufCodecs.DOUBLE.encode(buf, payload.xp().max());
        ByteBufCodecs.BOOL.encode(buf, payload.inBottleneck());
        ByteBufCodecs.DOUBLE.encode(buf, payload.spiritEnergy().current());
        ByteBufCodecs.DOUBLE.encode(buf, payload.spiritEnergy().max());
        ByteBufCodecs.VAR_INT.encode(buf, payload.qi());
        ByteBufCodecs.stringUtf8(MAX_SOUL_BEAST_NAME_LENGTH).encode(buf, payload.soulBeast().name());
        ByteBufCodecs.VAR_INT.encode(buf, payload.soulBeast().tier());
        ByteBufCodecs.VAR_INT.encode(buf, payload.ringTiers().size());
        for (final int tier : payload.ringTiers()) {
            ByteBufCodecs.VAR_INT.encode(buf, tier);
        }
    }

    private static HudSyncPayload read(final RegistryFriendlyByteBuf buf) {
        final int level = ByteBufCodecs.VAR_INT.decode(buf);
        final Gauge xp = new Gauge(ByteBufCodecs.DOUBLE.decode(buf), ByteBufCodecs.DOUBLE.decode(buf));
        final boolean inBottleneck = ByteBufCodecs.BOOL.decode(buf);
        final Gauge spiritEnergy = new Gauge(ByteBufCodecs.DOUBLE.decode(buf), ByteBufCodecs.DOUBLE.decode(buf));
        final int qi = ByteBufCodecs.VAR_INT.decode(buf);
        final SoulBeast soulBeast = new SoulBeast(
                ByteBufCodecs.stringUtf8(MAX_SOUL_BEAST_NAME_LENGTH).decode(buf), ByteBufCodecs.VAR_INT.decode(buf));
        final int ringCount = ByteBufCodecs.VAR_INT.decode(buf);
        final List<Integer> ringTiers = new ArrayList<>(ringCount);
        for (int i = 0; i < ringCount; i++) {
            ringTiers.add(ByteBufCodecs.VAR_INT.decode(buf));
        }
        return new HudSyncPayload(level, xp, inBottleneck, spiritEnergy, qi, soulBeast, ringTiers);
    }

    @Override
    public Type<HudSyncPayload> type() {
        return TYPE;
    }
}
