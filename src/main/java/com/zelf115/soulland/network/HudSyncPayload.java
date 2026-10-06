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
public record HudSyncPayload(int level, Gauge xp, boolean inBottleneck, Gauge spiritEnergy, int qi,
                             String martialSoulName, Selection ring, Selection bone, List<Integer> ringTiers)
        implements CustomPacketPayload {

    /** A bounded resource shown as a bar: XP toward the next level, or spirit energy toward its cap. */
    public record Gauge(double current, double max) {
    }

    /**
     * The ring or bone the cast keys currently use: where it sits, the beast it came from and the
     * translation key of what it does. An empty source name means nothing is selected; an empty
     * ability key means it does nothing to cast.
     */
    public record Selection(String slot, String sourceName, int tier, String abilityKey) {
        public static final Selection NONE = new Selection("", "", 0, "");

        public boolean isEmpty() {
            return sourceName.isEmpty();
        }
    }

    private static final int MAX_TEXT_LENGTH = 64;

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
        writeText(buf, payload.martialSoulName());
        writeSelection(buf, payload.ring());
        writeSelection(buf, payload.bone());
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
        final String martialSoulName = readText(buf);
        final Selection ring = readSelection(buf);
        final Selection bone = readSelection(buf);
        final int ringCount = ByteBufCodecs.VAR_INT.decode(buf);
        final List<Integer> ringTiers = new ArrayList<>(ringCount);
        for (int i = 0; i < ringCount; i++) {
            ringTiers.add(ByteBufCodecs.VAR_INT.decode(buf));
        }
        return new HudSyncPayload(level, xp, inBottleneck, spiritEnergy, qi, martialSoulName, ring, bone, ringTiers);
    }

    private static void writeSelection(final RegistryFriendlyByteBuf buf, final Selection selection) {
        writeText(buf, selection.slot());
        writeText(buf, selection.sourceName());
        ByteBufCodecs.VAR_INT.encode(buf, selection.tier());
        writeText(buf, selection.abilityKey());
    }

    private static Selection readSelection(final RegistryFriendlyByteBuf buf) {
        return new Selection(readText(buf), readText(buf), ByteBufCodecs.VAR_INT.decode(buf), readText(buf));
    }

    private static void writeText(final RegistryFriendlyByteBuf buf, final String text) {
        ByteBufCodecs.stringUtf8(MAX_TEXT_LENGTH).encode(buf, text);
    }

    private static String readText(final RegistryFriendlyByteBuf buf) {
        return ByteBufCodecs.stringUtf8(MAX_TEXT_LENGTH).decode(buf);
    }

    @Override
    public Type<HudSyncPayload> type() {
        return TYPE;
    }
}
