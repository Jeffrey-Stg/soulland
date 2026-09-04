package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.spirit.Affinity;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class MartialSoulEvolution {
    private MartialSoulEvolution() {
    }

    public static void tryEvolve(final Player player, final CultivationData data) {
        if (data.getMartialSoul() == null || data.getMartialSoul().isEvolution()) return;
        final MartialSoul target = switch (data.getMartialSoul()) {
            case BLUE_SILVER_GRASS -> data.getLevel() >= 40 ? MartialSoul.BLUE_SILVER_EMPEROR : null;
            case BLUE_LIGHTNING_DRAGON -> data.getLevel() >= 20 ? MartialSoul.BLUE_LIGHTNING_TYRANT_DRAGON : null;
            case DEMON_SOUL_GREAT_WHITE_SHARK -> hasRing(data, "abyss", "demon", "dragon") ? MartialSoul.ABYSS_DEMON_DRAGON_SHARK : null;
            case ABYSS_DEMON_DRAGON_SHARK -> data.getLevel() >= 90 ? MartialSoul.ABYSS_ICE_DEMON_DRAGON : null;
            case ABYSS_ICE_DEMON_DRAGON -> data.getLevel() >= 100 ? MartialSoul.SKY_BLUE_ICE_DEVOURING_DRAGON : null;
            case ICE_JADE_SCORPION -> data.getLevel() >= 50 ? MartialSoul.ICE_JADE_SCORPION_EMPEROR : null;
            case PHOENIX -> hasNineOtherPhoenixRings(data) ? MartialSoul.TEN_HEADED_FIRE_PHOENIX : null;
            case GOLDEN_DRAGON -> data.getLevel() >= 50 && hasRing(data, "dragon") ? MartialSoul.GOLDEN_DRAGON_KING : null;
            case SILVER_DRAGON -> data.getLevel() >= 50 && hasRing(data, "dragon") ? MartialSoul.SILVER_DRAGON_KING : null;
            case HOLY_ANGEL -> data.getLevel() >= 70 ? MartialSoul.SERAPHIM : null;
            default -> null;
        };
        if (target != null) evolve(player, data, target);
    }

    public static void evolveFromHerb(final Player player, final CultivationData data, final boolean fullMoon) {
        if (data.getMartialSoul() == MartialSoul.SEVEN_TREASURE_GLAZED_TILE_PAGODA) {
            evolve(player, data, MartialSoul.NINE_TREASURE_GLAZED_TILE_PAGODA);
            data.setMartialSoulCanReachLevel100(true);
        } else if (fullMoon && data.getMartialSoul() == MartialSoul.SPIRIT_EYES && data.getLevel() >= 40) {
            evolve(player, data, MartialSoul.POLYCORIA_EYES);
        }
    }

    public static void tryEvolvePolycoria(final Player player, final CultivationData data) {
        if (data.getMartialSoul() == MartialSoul.POLYCORIA_EYES && data.getLevel() >= 70) {
            evolve(player, data, MartialSoul.EYES_OF_ASURA);
        }
    }

    private static void evolve(final Player player, final CultivationData data, final MartialSoul target) {
        data.setMartialSoul(target);
        data.clearAffinityMultipliers();
        target.affinityMultipliers().forEach(data::setAffinityMultiplier);
        player.sendSystemMessage(Component.literal("Your martial soul evolved into " + target.displayName() + "."));
    }

    private static boolean hasRing(final CultivationData data, final String... parts) {
        return data.getAbsorbedRings().stream().anyMatch(ring -> containsAll(ring.sourceName(), parts));
    }

    private static boolean containsAll(final String value, final String... parts) {
        final String normalized = value.toLowerCase(Locale.ROOT);
        for (final String part : parts) if (!normalized.contains(part)) return false;
        return true;
    }

    private static boolean hasNineOtherPhoenixRings(final CultivationData data) {
        final Set<String> names = new HashSet<>();
        for (final AbsorbedRing ring : data.getAbsorbedRings()) {
            final String name = ring.sourceName().toLowerCase(Locale.ROOT);
            if (name.contains("phoenix") && !name.equals("phoenix")) names.add(name);
        }
        return names.size() >= 9;
    }
}
