package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.spirit.AffinitySystem;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class MartialSoulEvolution {

    private static final int BLUE_SILVER_EMPEROR_LEVEL = 40;
    private static final int BLUE_LIGHTNING_TYRANT_DRAGON_LEVEL = 20;
    private static final int GOLDEN_HOLY_DRAGON_LEVEL = 60;
    private static final int ABYSS_ICE_DEMON_DRAGON_LEVEL = 90;
    private static final int SKY_BLUE_ICE_DEVOURING_DRAGON_LEVEL = 100;
    private static final int ICE_JADE_SCORPION_EMPEROR_LEVEL = 50;
    private static final int DRAGON_KING_LEVEL = 50;
    private static final int SERAPHIM_LEVEL = 70;
    private static final int POLYCORIA_EYES_LEVEL = 40;
    private static final int EYES_OF_ASURA_LEVEL = 70;
    private static final int TEN_HEADED_PHOENIX_RING_COUNT = 9;

    private MartialSoulEvolution() {
    }

    public static void tryEvolve(final Player player, final CultivationData data) {
        tryEvolveSlot(player, data, SoulSlot.PRIMARY);
        tryEvolveSlot(player, data, SoulSlot.SECONDARY);
    }

    private static void tryEvolveSlot(final Player player, final CultivationData data, final SoulSlot slot) {
        final MartialSoul soul = soulIn(data, slot);
        if (soul == null) return;

        final MartialSoul target = nextStageOf(soul, data, ringsIn(data, slot));
        if (target != null) evolve(player, data, slot, target);
    }

    /** The stage a soul grows into once its own condition is met, or null while it stays as it is. */
    private static MartialSoul nextStageOf(final MartialSoul soul, final CultivationData data,
                                           final List<AbsorbedRing> rings) {
        final int level = data.getLevel();
        return switch (soul) {
            case BLUE_SILVER_GRASS -> level >= BLUE_SILVER_EMPEROR_LEVEL ? MartialSoul.BLUE_SILVER_EMPEROR : null;
            case BLUE_LIGHTNING_DRAGON -> level >= BLUE_LIGHTNING_TYRANT_DRAGON_LEVEL ? MartialSoul.BLUE_LIGHTNING_TYRANT_DRAGON : null;
            case BLUE_LIGHTNING_TYRANT_DRAGON -> level >= GOLDEN_HOLY_DRAGON_LEVEL ? MartialSoul.GOLDEN_HOLY_DRAGON : null;
            case DEMON_SOUL_GREAT_WHITE_SHARK -> hasRing(rings, "abyss", "demon", "dragon") ? MartialSoul.ABYSS_DEMON_DRAGON_SHARK : null;
            case ABYSS_DEMON_DRAGON_SHARK -> level >= ABYSS_ICE_DEMON_DRAGON_LEVEL ? MartialSoul.ABYSS_ICE_DEMON_DRAGON : null;
            case ABYSS_ICE_DEMON_DRAGON -> level >= SKY_BLUE_ICE_DEVOURING_DRAGON_LEVEL ? MartialSoul.SKY_BLUE_ICE_DEVOURING_DRAGON : null;
            case ICE_JADE_SCORPION -> level >= ICE_JADE_SCORPION_EMPEROR_LEVEL ? MartialSoul.ICE_JADE_SCORPION_EMPEROR : null;
            case PHOENIX -> hasNineOtherPhoenixRings(rings) ? MartialSoul.TEN_HEADED_FIRE_PHOENIX : null;
            case GOLDEN_DRAGON -> level >= DRAGON_KING_LEVEL && hasRing(rings, "dragon") ? MartialSoul.GOLDEN_DRAGON_KING : null;
            case SILVER_DRAGON -> level >= DRAGON_KING_LEVEL && hasRing(rings, "dragon") ? MartialSoul.SILVER_DRAGON_KING : null;
            case HOLY_ANGEL -> level >= SERAPHIM_LEVEL ? MartialSoul.SERAPHIM : null;
            case POLYCORIA_EYES -> level >= EYES_OF_ASURA_LEVEL ? MartialSoul.EYES_OF_ASURA : null;
            default -> null;
        };
    }

    /** Beautiful Silk Tulip: grows the pagoda, which also lifts the level cap to 100. */
    public static void evolveFromSilkTulip(final Player player, final CultivationData data) {
        evolveSoulOfKind(player, data, MartialSoul.SEVEN_TREASURE_GLAZED_TILE_PAGODA,
                MartialSoul.NINE_TREASURE_GLAZED_TILE_PAGODA);
    }

    /** Full Moon Wearing Autumn Dew: opens Spirit Eyes into Polycoria Eyes from level 40 on. */
    public static void evolveFromFullMoonDew(final Player player, final CultivationData data) {
        if (data.getLevel() < POLYCORIA_EYES_LEVEL) return;
        evolveSoulOfKind(player, data, MartialSoul.SPIRIT_EYES, MartialSoul.POLYCORIA_EYES);
    }

    private static void evolveSoulOfKind(final Player player, final CultivationData data,
                                         final MartialSoul required, final MartialSoul target) {
        for (final SoulSlot slot : SoulSlot.values()) {
            if (soulIn(data, slot) == required) {
                evolve(player, data, slot, target);
            }
        }
    }

    private static MartialSoul soulIn(final CultivationData data, final SoulSlot slot) {
        return slot == SoulSlot.PRIMARY ? data.getMartialSoul() : data.getSecondaryMartialSoul();
    }

    private static List<AbsorbedRing> ringsIn(final CultivationData data, final SoulSlot slot) {
        return data.getAbsorbedRings().stream().filter(ring -> ring.slot() == slot).toList();
    }

    private static void evolve(final Player player, final CultivationData data, final SoulSlot slot, final MartialSoul target) {
        if (slot == SoulSlot.PRIMARY) {
            data.setMartialSoul(target);
        } else {
            data.setSecondaryMartialSoul(target);
        }
        if (target == MartialSoul.NINE_TREASURE_GLAZED_TILE_PAGODA) {
            data.setMartialSoulCanReachLevel100(true);
        }
        AffinitySystem.recomputeAffinities(data);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.martial_soul.evolved", target.displayName()));
    }

    private static boolean hasRing(final List<AbsorbedRing> rings, final String... parts) {
        return rings.stream().anyMatch(ring -> containsAll(ring.sourceName(), parts));
    }

    private static boolean containsAll(final String value, final String... parts) {
        final String normalized = value.toLowerCase(Locale.ROOT);
        for (final String part : parts) if (!normalized.contains(part)) return false;
        return true;
    }

    private static boolean hasNineOtherPhoenixRings(final List<AbsorbedRing> rings) {
        final Set<String> names = new HashSet<>();
        for (final AbsorbedRing ring : rings) {
            final String name = ring.sourceName().toLowerCase(Locale.ROOT);
            if (name.contains("phoenix") && !name.equals("phoenix")) names.add(name);
        }
        return names.size() >= TEN_HEADED_PHOENIX_RING_COUNT;
    }
}
