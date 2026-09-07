package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.spirit.AffinitySystem;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class MartialSoulEvolution {
    private MartialSoulEvolution() {
    }

    public static void tryEvolve(final Player player, final CultivationData data) {
        tryEvolveSlot(player, data, SoulSlot.PRIMARY);
        tryEvolveSlot(player, data, SoulSlot.SECONDARY);
    }

    private static void tryEvolveSlot(final Player player, final CultivationData data, final SoulSlot slot) {
        final MartialSoul soul = soulIn(data, slot);
        if (soul == null || soul.isEvolution()) return;

        final List<AbsorbedRing> rings = ringsIn(data, slot);
        final MartialSoul target = switch (soul) {
            case BLUE_SILVER_GRASS -> data.getLevel() >= 40 ? MartialSoul.BLUE_SILVER_EMPEROR : null;
            case BLUE_LIGHTNING_DRAGON -> data.getLevel() >= 20 ? MartialSoul.BLUE_LIGHTNING_TYRANT_DRAGON : null;
            case DEMON_SOUL_GREAT_WHITE_SHARK -> hasRing(rings, "abyss", "demon", "dragon") ? MartialSoul.ABYSS_DEMON_DRAGON_SHARK : null;
            case ABYSS_DEMON_DRAGON_SHARK -> data.getLevel() >= 90 ? MartialSoul.ABYSS_ICE_DEMON_DRAGON : null;
            case ABYSS_ICE_DEMON_DRAGON -> data.getLevel() >= 100 ? MartialSoul.SKY_BLUE_ICE_DEVOURING_DRAGON : null;
            case ICE_JADE_SCORPION -> data.getLevel() >= 50 ? MartialSoul.ICE_JADE_SCORPION_EMPEROR : null;
            case PHOENIX -> hasNineOtherPhoenixRings(rings) ? MartialSoul.TEN_HEADED_FIRE_PHOENIX : null;
            case GOLDEN_DRAGON -> data.getLevel() >= 50 && hasRing(rings, "dragon") ? MartialSoul.GOLDEN_DRAGON_KING : null;
            case SILVER_DRAGON -> data.getLevel() >= 50 && hasRing(rings, "dragon") ? MartialSoul.SILVER_DRAGON_KING : null;
            case HOLY_ANGEL -> data.getLevel() >= 70 ? MartialSoul.SERAPHIM : null;
            default -> null;
        };
        if (target != null) evolve(player, data, slot, target);
    }

    public static void evolveFromHerb(final Player player, final CultivationData data, final boolean fullMoon) {
        evolveFromHerbSlot(player, data, SoulSlot.PRIMARY, fullMoon);
        evolveFromHerbSlot(player, data, SoulSlot.SECONDARY, fullMoon);
    }

    private static void evolveFromHerbSlot(final Player player, final CultivationData data, final SoulSlot slot, final boolean fullMoon) {
        final MartialSoul soul = soulIn(data, slot);
        if (soul == MartialSoul.SEVEN_TREASURE_GLAZED_TILE_PAGODA) {
            evolve(player, data, slot, MartialSoul.NINE_TREASURE_GLAZED_TILE_PAGODA);
            data.setMartialSoulCanReachLevel100(true);
        } else if (fullMoon && soul == MartialSoul.SPIRIT_EYES && data.getLevel() >= 40) {
            evolve(player, data, slot, MartialSoul.POLYCORIA_EYES);
        }
    }

    public static void tryEvolvePolycoria(final Player player, final CultivationData data) {
        tryEvolvePolycoriaSlot(player, data, SoulSlot.PRIMARY);
        tryEvolvePolycoriaSlot(player, data, SoulSlot.SECONDARY);
    }

    private static void tryEvolvePolycoriaSlot(final Player player, final CultivationData data, final SoulSlot slot) {
        if (soulIn(data, slot) == MartialSoul.POLYCORIA_EYES && data.getLevel() >= 70) {
            evolve(player, data, slot, MartialSoul.EYES_OF_ASURA);
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
        AffinitySystem.recomputeAffinities(data);
        player.sendSystemMessage(Component.literal("Your martial soul evolved into " + target.displayName() + "."));
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
        return names.size() >= 9;
    }
}
