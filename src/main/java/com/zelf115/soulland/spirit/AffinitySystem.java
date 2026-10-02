package com.zelf115.soulland.spirit;

import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.MartialSoul;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;

public final class AffinitySystem {
    public static final String RING_AFFINITIES_KEY = "Affinities";
    private static final Map<String, Set<Affinity>> BEAST_AFFINITIES = Map.ofEntries(
            entry("white_tiger", Affinity.DARK, Affinity.LIGHT, Affinity.BEAST),
            entry("hell_spirit_cat", Affinity.SPIRIT, Affinity.DARK, Affinity.LIGHT, Affinity.BEAST),
            entry("golden_dragon", Affinity.DRAGON, Affinity.HOLY, Affinity.LIGHT),
            entry("silver_dragon", Affinity.DRAGON, Affinity.FIRE, Affinity.ICE, Affinity.DARK, Affinity.LIGHT),
            entry("ice_jade_scorpion", Affinity.INSECT, Affinity.POISON, Affinity.ICE),
            entry("blue_lightning_dragon", Affinity.LIGHTNING, Affinity.DRAGON),
            entry("phoenix", Affinity.BIRD, Affinity.FIRE),
            entry("fire_phoenix", Affinity.BIRD, Affinity.FIRE),
            entry("evil_phoenix", Affinity.BIRD, Affinity.DEMONIC),
            entry("radiant_phoenix", Affinity.BIRD, Affinity.LIGHT),
            entry("ice_phoenix", Affinity.BIRD, Affinity.ICE),
            entry("black_flame_phoenix", Affinity.BIRD, Affinity.FIRE, Affinity.DARK),
            entry("evil_fire_phoenix", Affinity.BIRD, Affinity.FIRE, Affinity.DEMONIC),
            entry("heavenly_phoenix", Affinity.BIRD, Affinity.HOLY),
            entry("darkness_phoenix", Affinity.BIRD, Affinity.DARK),
            entry("emerald_phoenix", Affinity.BIRD, Affinity.WIND),
            entry("demon_white_shark", Affinity.ICE, Affinity.BEAST, Affinity.DEMONIC),
            entry("romanti_snake", Affinity.POISON, Affinity.BEAST),
            entry("manfaced_demon_spider", Affinity.INSECT, Affinity.POISON),
            entry("phantom_tiger", Affinity.SPIRIT, Affinity.BEAST),
            entry("pit_demon_spider", Affinity.INSECT, Affinity.POISON),
            entry("dark_devilgod_tiger", Affinity.DEMONIC, Affinity.BEAST, Affinity.WIND, Affinity.LIGHTNING, Affinity.DARK),
            entry("evil_spirit_orca", Affinity.ICE, Affinity.BEAST, Affinity.DEMONIC),
            entry("nine_knot_ichthyosaur", Affinity.ICE, Affinity.BEAST),
            entry("wind_spirit_wolf", Affinity.WIND, Affinity.SPIRIT, Affinity.BEAST),
            entry("wind_baboon", Affinity.WIND, Affinity.BEAST),
            entry("dark_gold_terror_claw_bear", Affinity.DARK, Affinity.LIGHT, Affinity.SPIRIT, Affinity.BEAST),
            entry("elemental_silver_wolf", Affinity.FIRE, Affinity.ICE, Affinity.WIND, Affinity.LIGHTNING, Affinity.BEAST),
            entry("ice_silk_worm", Affinity.ICE, Affinity.SPIRIT, Affinity.INSECT),
            entry("flame_lion", Affinity.FIRE, Affinity.BEAST),
            entry("blood_demonic_bear", Affinity.FIRE, Affinity.BEAST),
            entry("gold_wolf", Affinity.LIGHT, Affinity.HOLY, Affinity.BEAST),
            entry("silverbrave", Affinity.SPIRIT, Affinity.BEAST),
            entry("scarlet_fire_monkey", Affinity.FIRE, Affinity.BEAST),
            entry("three_eyed_golden_lion", Affinity.LIGHT, Affinity.HOLY, Affinity.SPIRIT, Affinity.BEAST),
            entry("abyss_demon_dragon", Affinity.DARK, Affinity.DEMONIC, Affinity.DRAGON, Affinity.BEAST),
            entry("evileye_tyrant", Affinity.SPIRIT, Affinity.BEAST),
            entry("golden_eyed_black_dragon", Affinity.DARK, Affinity.DRAGON),
            entry("ice_devil_titan", Affinity.ICE, Affinity.DEMONIC, Affinity.BEAST),
            entry("star_anise", Affinity.ICE, Affinity.BEAST),
            entry("ice_bear", Affinity.ICE, Affinity.BEAST),
            entry("blazing_demon_lion", Affinity.FIRE, Affinity.DEMONIC, Affinity.BEAST),
            entry("ice_fire_demonic_tiger", Affinity.ICE, Affinity.FIRE, Affinity.DEMONIC, Affinity.BEAST),
            entry("poison_quill_porcupine", Affinity.POISON, Affinity.BEAST),
            entry("emerald_demon_bird", Affinity.WIND, Affinity.BIRD, Affinity.DEMONIC),
            entry("tyrant_dragon", Affinity.DRAGON),
            entry("three_eyed_demon_ape", Affinity.SPIRIT, Affinity.DEMONIC, Affinity.BEAST),
            entry("giant_octopus", Affinity.ICE, Affinity.BEAST),
            entry("titanohippo", Affinity.ICE, Affinity.BEAST),
            entry("lava_hound", Affinity.FIRE, Affinity.BEAST),
            entry("gold_eyed_leopard", Affinity.LIGHT, Affinity.SPIRIT, Affinity.BEAST),
            entry("gold_silk_ape", Affinity.LIGHT, Affinity.BEAST),
            entry("purple_thunder_bear", Affinity.LIGHTNING, Affinity.BEAST),
            entry("yin_yang_chaos_bird", Affinity.LIGHT, Affinity.DARK, Affinity.HOLY, Affinity.DEMONIC, Affinity.BIRD),
            entry("lightning_blue_leopard", Affinity.LIGHTNING),
            entry("thunder_dragon", Affinity.LIGHTNING, Affinity.DRAGON),
            entry("ice_jade_scorpion_empress", Affinity.INSECT, Affinity.POISON, Affinity.ICE),
            entry("ice_bear_king", Affinity.ICE, Affinity.BEAST),
            entry("skydream_ice_worm", Affinity.ICE, Affinity.SPIRIT, Affinity.INSECT),
            entry("three_eyed_golden_lion_boss", Affinity.LIGHT, Affinity.HOLY, Affinity.SPIRIT, Affinity.BEAST),
            entry("sky_azure_bull_python", Affinity.LIGHTNING, Affinity.DRAGON, Affinity.BEAST),
            entry("titan_giant_ape", Affinity.BEAST, Affinity.SPIRIT),
            entry("evil_spirit_orca_king", Affinity.ICE, Affinity.BEAST, Affinity.DEMONIC));

    private AffinitySystem() {
    }

    public static Set<Affinity> affinitiesOf(final SpiritBeastEntity beast) {
        final var key = BuiltInRegistries.ENTITY_TYPE.getKey(beast.getType());
        return BEAST_AFFINITIES.getOrDefault(key == null ? "" : key.getPath(), Set.of(Affinity.BEAST));
    }

    private static Map.Entry<String, Set<Affinity>> entry(final String name, final Affinity... affinities) {
        return Map.entry(name, Set.of(affinities));
    }

    public static void writeRingAffinities(final CompoundTag tag, final Set<Affinity> affinities) {
        final ListTag list = new ListTag();
        for (final Affinity affinity : affinities) {
            list.add(StringTag.valueOf(affinity.name()));
        }
        tag.put(RING_AFFINITIES_KEY, list);
    }

    public static double ringMultiplier(final Player player, final CompoundTag ringTag) {
        final ListTag list = ringTag.getList(RING_AFFINITIES_KEY, 8);
        if (list.isEmpty()) return 1.0;
        double multiplier = 1.0;
        for (int index = 0; index < list.size(); index++) {
            try {
                final Affinity affinity = Affinity.valueOf(list.getString(index));
                multiplier *= playerAffinityMultiplier(player, affinity);
            } catch (IllegalArgumentException ignored) {
                // Ignore affinity names from future versions.
            }
        }
        return multiplier;
    }

    public static double playerAffinityMultiplier(final Player player, final Affinity affinity) {
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        return data.getAffinityMultiplier(affinity);
    }

    public static void setPlayerAffinity(final Player player, final Affinity affinity, final double multiplier) {
        player.getData(CultivationAttachment.CULTIVATION_DATA.get()).setAffinityMultiplier(affinity, multiplier);
    }

    public static void chooseMartialSoul(final Player player, final MartialSoul martialSoul) {
        if (player.level().isClientSide() || martialSoul == null || martialSoul.isEvolution()) {
            return;
        }
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        if (data.getMartialSoul() != null) {
            return;
        }
        data.setMartialSoul(martialSoul);
        recomputeAffinities(data);
        data.setInnateStat(1 + player.getRandom().nextInt(CultivationData.MAX_INNATE_STAT));
        rollTwinSoulChance(player, data);
    }

    /** Applies the twin-soul bonus pick once a chance roll has flagged one as pending. */
    public static void chooseSecondMartialSoul(final Player player, final MartialSoul martialSoul) {
        if (player.level().isClientSide() || martialSoul == null || martialSoul.isEvolution()) {
            return;
        }
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        if (!data.isSecondMartialSoulPending() || data.getSecondaryMartialSoul() != null) {
            return;
        }
        if (martialSoul == data.getMartialSoul()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "soulland.cultivation.martial_soul.second_must_differ"));
            return;
        }
        data.setSecondaryMartialSoul(martialSoul);
        recomputeAffinities(data);
        data.setSecondMartialSoulPending(false);
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                "soulland.cultivation.martial_soul.second_chosen", martialSoul.displayName()));
    }

    /**
     * Rolls the twin-soul chance. Called once right after the first pick, and again after each of
     * the first two successful breakthroughs (see {@link com.zelf115.soulland.cultivation.BreakthroughManager}).
     */
    public static void rollTwinSoulChance(final Player player, final CultivationData data) {
        if (data.getSecondaryMartialSoul() != null || data.isSecondMartialSoulPending()) {
            return;
        }
        final int chancePercent = com.zelf115.soulland.cultivation.CultivationManager.twinMartialSoulChancePercent(data.getEffectiveInnateStat());
        if (chancePercent <= 0 || player.getRandom().nextInt(100) >= chancePercent) {
            return;
        }
        data.setSecondMartialSoulPending(true);
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("soulland.cultivation.martial_soul.twin_soul_available"));
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                    serverPlayer, new com.zelf115.soulland.network.OpenMartialSoulPickerPayload());
        }
    }

    /** Clears both souls so a future rebirth feature can send the player back through the picker. */
    public static void clearMartialSoulForRebirth(final CultivationData data) {
        data.setMartialSoul(null);
        data.setSecondaryMartialSoul(null);
        data.setSecondMartialSoulPending(false);
        data.clearAffinityMultipliers();
    }

    /** Recomputes the affinity map from scratch: both souls' multipliers stack (multiply) together. */
    public static void recomputeAffinities(final CultivationData data) {
        data.clearAffinityMultipliers();
        stackSoulAffinities(data, data.getMartialSoul());
        stackSoulAffinities(data, data.getSecondaryMartialSoul());
    }

    private static void stackSoulAffinities(final CultivationData data, final MartialSoul soul) {
        if (soul == null) return;
        soul.affinityMultipliers().forEach((affinity, multiplier) ->
                data.setAffinityMultiplier(affinity, data.getAffinityMultiplier(affinity) * multiplier));
    }
}
