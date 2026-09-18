package com.zelf115.soulland.tournament;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.item.SpiritBoneItem;
import com.zelf115.soulland.spirit.SpiritBeastEntities;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** What a won tournament round pays out, all of it scaled by the level of the opponent beaten. */
public final class TournamentRewards {

    private static final int MIN_ORES = 10;
    private static final int MAX_ORES = 64;
    private static final int MIN_HERBS = 2;
    private static final int MAX_HERBS = 20;
    private static final int LEVELS_PER_HERB = 5;
    private static final int MIN_PILLS = 1;
    private static final int MAX_PILLS = 10;
    private static final int LEVELS_PER_PILL = 10;
    /** Spirit bones are only worth carrying once a cultivator can absorb them. */
    private static final int SPIRIT_BONE_LEVEL = 30;

    private static final List<Item> ORES = List.of(
            Items.COAL, Items.RAW_COPPER, Items.RAW_IRON, Items.RAW_GOLD,
            Items.REDSTONE, Items.LAPIS_LAZULI, Items.DIAMOND, Items.EMERALD);

    private static final List<Item> HERBS = List.of(
            SoulLand.BEAUTIFUL_SILK_TULIP.get(), SoulLand.BLACK_JADE_DIVINE_BAMBOO.get(),
            SoulLand.COMMON_SPIRIT_HERB.get(), SoulLand.DRAGONSCALE_FRUIT.get(),
            SoulLand.EIGHT_PETAL_IMMORTAL_ORCHID.get(), SoulLand.FULL_MOON_WEARING_AUTUMN_DEW.get(),
            SoulLand.ICE_CRYSTAL_FRUIT.get(), SoulLand.INFERNAL_DELICATE_APRICOT.get(),
            SoulLand.OCTAGONAL_MYSTERIOUS_ICE_GRASS.get(), SoulLand.ORIGIN_ENERGY_IMMORTAL_GRASS.get(),
            SoulLand.SACRED_SOUL_GRASS.get(), SoulLand.SCARLET_FLAME_FRUIT.get(),
            SoulLand.SINGULAR_VELVET_SKY_CHRYSANTHEMUM.get(), SoulLand.WATER_CRYSTAL_PEACH.get(),
            SoulLand.YEARNING_HEARTBROKEN_RED.get());

    private static final List<Item> PILLS = List.of(
            SoulLand.MYSTERIOUS_WATER_PILL.get(), SoulLand.SPIRIT_ASCENSION_PILL.get(),
            SoulLand.QI_GATHERING_PILL_TIER_1.get(), SoulLand.QI_GATHERING_PILL_TIER_2.get(),
            SoulLand.QI_GATHERING_PILL_TIER_3.get(), SoulLand.QI_GATHERING_PILL_TIER_4.get(),
            SoulLand.QI_GATHERING_PILL_TIER_5.get());

    /** Age bands a rewarded bone can come from, with the weight of each. */
    private static final int[] BONE_YEAR_FLOORS = {10_000, 100_000, 200_000, 1_000_000};
    private static final int[] BONE_YEAR_CEILINGS = {99_999, 199_999, 999_999, 9_999_999};
    private static final int[] BONE_BAND_WEIGHTS = {50, 30, 15, 5};

    private TournamentRewards() {
    }

    /** One reward for a won round, rolled from the pool the player qualifies for. */
    public static void grantRoundReward(final ServerPlayer player, final CultivationData data, final int opponentLevel) {
        final List<ItemStack> pool = rollablePool(player, data, opponentLevel);
        give(player, pool.get(player.getRandom().nextInt(pool.size())));
    }

    /** The final round pays out the whole pool at once, bone included when it is on the table. */
    public static void grantFinalReward(final ServerPlayer player, final CultivationData data, final int opponentLevel) {
        for (final ItemStack stack : rollablePool(player, data, opponentLevel)) {
            give(player, stack);
        }
    }

    private static List<ItemStack> rollablePool(final ServerPlayer player, final CultivationData data,
                                                final int opponentLevel) {
        final RandomSource random = player.getRandom();
        final List<ItemStack> pool = new ArrayList<>();
        pool.add(new ItemStack(pick(ORES, random), oreCount(opponentLevel)));
        pool.add(new ItemStack(pick(HERBS, random), herbCount(opponentLevel)));
        pool.add(new ItemStack(pick(PILLS, random), pillCount(opponentLevel)));
        if (data.getLevel() >= SPIRIT_BONE_LEVEL) {
            pool.add(rollSpiritBone(random));
        }
        return pool;
    }

    /** Names the prize before handing it over: the inventory drains the stack it is given. */
    private static void give(final ServerPlayer player, final ItemStack stack) {
        final Component name = stack.getHoverName();
        final int count = stack.getCount();
        player.getInventory().placeItemBackInInventory(stack);
        player.sendSystemMessage(Component.translatable("soulland.tournament.reward", count, name));
    }

    private static int oreCount(final int opponentLevel) {
        return Math.max(MIN_ORES, Math.min(MAX_ORES, opponentLevel));
    }

    private static int herbCount(final int opponentLevel) {
        return Math.max(MIN_HERBS, Math.min(MAX_HERBS, ceilDiv(opponentLevel, LEVELS_PER_HERB)));
    }

    private static int pillCount(final int opponentLevel) {
        return Math.max(MIN_PILLS, Math.min(MAX_PILLS, ceilDiv(opponentLevel, LEVELS_PER_PILL)));
    }

    private static int ceilDiv(final int value, final int divisor) {
        return (value + divisor - 1) / divisor;
    }

    private static Item pick(final List<Item> items, final RandomSource random) {
        return items.get(random.nextInt(items.size()));
    }

    private static ItemStack rollSpiritBone(final RandomSource random) {
        final int band = rollYearBand(random);
        final int years = BONE_YEAR_FLOORS[band]
                + random.nextInt(BONE_YEAR_CEILINGS[band] - BONE_YEAR_FLOORS[band] + 1);
        final int tier = SpiritBeastManager.tierForYears(years);
        final String slot = SpiritBeastManager.INTERNAL_BONE_SLOTS
                .get(random.nextInt(SpiritBeastManager.INTERNAL_BONE_SLOTS.size()));
        return SpiritBoneItem.create(rollSourceBeastName(random), slot, tier, years,
                SpiritBeastManager.spiritBoneBonusForAge(tier, years));
    }

    private static int rollYearBand(final RandomSource random) {
        int roll = random.nextInt(totalBandWeight());
        for (int band = 0; band < BONE_BAND_WEIGHTS.length; band++) {
            roll -= BONE_BAND_WEIGHTS[band];
            if (roll < 0) {
                return band;
            }
        }
        return BONE_BAND_WEIGHTS.length - 1;
    }

    private static int totalBandWeight() {
        int total = 0;
        for (final int weight : BONE_BAND_WEIGHTS) {
            total += weight;
        }
        return total;
    }

    /** Bosses keep their own bones, so a tournament prize never carries one. */
    private static String rollSourceBeastName(final RandomSource random) {
        final List<EntityType<?>> candidates = new ArrayList<>();
        for (final var holder : SpiritBeastEntities.ALL) {
            final EntityType<?> type = holder.get();
            if (!SpiritBeastManager.BOSS_BEAST_PATHS.contains(pathOf(type))) {
                candidates.add(type);
            }
        }
        return candidates.get(random.nextInt(candidates.size())).getDescription().getString();
    }

    private static String pathOf(final EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
    }
}
