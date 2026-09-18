package com.zelf115.soulland.trial;

import com.zelf115.soulland.StatBonus;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.cultivation.SoulRingCapacity;
import com.zelf115.soulland.item.SoulRingItem;
import com.zelf115.soulland.spirit.Affinity;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Rolls and hands out the reward a god owes for a finished trial task. */
public final class GodTrialRewards {

    /** Stat points a single stat reward is worth. */
    private static final double REWARD_STAT_POINTS = 100.0;
    private static final int MIN_EXPERIENCE_PERCENT = 10;
    private static final int MAX_EXPERIENCE_PERCENT = 100;
    private static final double PERCENT = 100.0;
    /** A god may not hand out a ring older than the oldest beast a cultivator could have hunted. */
    private static final int MAX_REWARD_RING_YEARS = 1_000_000;
    private static final double NO_BONUS = 0.0;

    private GodTrialRewards() {
    }

    /** Rolls one reward the player can actually use and grants it. */
    public static Component grantRandom(final ServerPlayer player, final CultivationData data) {
        final RandomSource random = player.getRandom();
        final List<GodTrialRewardType> available = availableTypes(player, data);
        final GodTrialRewardType type = available.get(random.nextInt(available.size()));
        return grant(player, data, type, random);
    }

    private static List<GodTrialRewardType> availableTypes(final ServerPlayer player, final CultivationData data) {
        final List<GodTrialRewardType> available = new ArrayList<>(List.of(GodTrialRewardType.values()));
        if (!hasOpenRingSlot(player, data)) {
            available.remove(GodTrialRewardType.SOUL_RING);
        }
        return available;
    }

    private static Component grant(final ServerPlayer player, final CultivationData data,
                                   final GodTrialRewardType type, final RandomSource random) {
        if (type == GodTrialRewardType.EXPERIENCE) {
            return grantExperience(player, data, random);
        }
        if (type == GodTrialRewardType.SOUL_RING) {
            return grantSoulRing(player, data);
        }
        return grantStat(player, data, type);
    }

    private static Component grantExperience(final ServerPlayer player, final CultivationData data,
                                             final RandomSource random) {
        final int percent = MIN_EXPERIENCE_PERCENT
                + random.nextInt(MAX_EXPERIENCE_PERCENT - MIN_EXPERIENCE_PERCENT + 1);
        final double amount = CultivationManager.xpRequiredForLevel(data.getLevel()) * percent / PERCENT;
        data.addGodTrialReward(new GodTrialReward(GodTrialRewardType.EXPERIENCE, percent));
        CultivationManager.grantXp(player, data, amount);
        return Component.translatable("soulland.trial.reward.experience", percent);
    }

    private static Component grantStat(final ServerPlayer player, final CultivationData data,
                                       final GodTrialRewardType type) {
        data.addGodTrialReward(new GodTrialReward(type, REWARD_STAT_POINTS));
        final int rewardIndex = data.getGodTrialRewards().size() - 1;
        Stats.applyBonus(player, GodTrialReward.modifierId(rewardIndex), statBonusFor(type));
        return Component.translatable("soulland.trial.reward.stat", (int) REWARD_STAT_POINTS,
                Component.translatable(statNameKey(type)));
    }

    private static Component grantSoulRing(final ServerPlayer player, final CultivationData data) {
        final int tier = SoulRingCapacity.maxAbsorbableTier(Stats.getSpirit(player));
        final int years = Math.min(MAX_REWARD_RING_YEARS, SpiritBeastManager.oldestYearsOfTier(tier));
        data.addGodTrialReward(new GodTrialReward(GodTrialRewardType.SOUL_RING, tier));
        final ItemStack ring = SoulRingItem.create(
                Component.translatable("soulland.trial.reward.ring_source").getString(),
                tier, years, SpiritBeastManager.soulRingBonusForAge(tier, years), EnumSet.allOf(Affinity.class));
        player.getInventory().placeItemBackInInventory(ring);
        return Component.translatable("soulland.trial.reward.soul_ring", SpiritBeastManager.describeTier(tier));
    }

    private static boolean hasOpenRingSlot(final ServerPlayer player, final CultivationData data) {
        final int tier = SoulRingCapacity.maxAbsorbableTier(Stats.getSpirit(player));
        return data.getRingCount(data.getActiveSoulSlot())
                        < CultivationManager.maxSoulRingCountForLevel(data.getLevel())
                && SoulRingCapacity.hasRoomFor(Stats.getSpirit(player), data.getAbsorbedRings(), tier);
    }

    private static StatBonus statBonusFor(final GodTrialRewardType type) {
        return switch (type) {
            case DAMAGE -> new StatBonus(REWARD_STAT_POINTS, NO_BONUS, NO_BONUS, NO_BONUS, NO_BONUS, NO_BONUS);
            case HEALTH -> new StatBonus(NO_BONUS, REWARD_STAT_POINTS, NO_BONUS, NO_BONUS, NO_BONUS, NO_BONUS);
            case DEFENSE -> new StatBonus(NO_BONUS, NO_BONUS, REWARD_STAT_POINTS, NO_BONUS, NO_BONUS, NO_BONUS);
            default -> new StatBonus(NO_BONUS, NO_BONUS, NO_BONUS, NO_BONUS, REWARD_STAT_POINTS, NO_BONUS);
        };
    }

    private static String statNameKey(final GodTrialRewardType type) {
        return "attribute.soulland." + type.name().toLowerCase(Locale.ROOT);
    }
}
