package com.zelf115.soulland.trial;

import com.zelf115.soulland.SoulLand;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/** A reward the player has already been granted, kept so its attribute modifier can be removed. */
public record GodTrialReward(GodTrialRewardType type, double amount) {

    private static final String TYPE_KEY = "Type";
    private static final String AMOUNT_KEY = "Amount";

    public static GodTrialReward readFrom(final CompoundTag tag) {
        return new GodTrialReward(readType(tag), tag.getDouble(AMOUNT_KEY));
    }

    private static GodTrialRewardType readType(final CompoundTag tag) {
        try {
            return GodTrialRewardType.valueOf(tag.getString(TYPE_KEY));
        } catch (IllegalArgumentException ignored) {
            return GodTrialRewardType.EXPERIENCE;
        }
    }

    public CompoundTag toNbt() {
        final CompoundTag tag = new CompoundTag();
        tag.putString(TYPE_KEY, type.name());
        tag.putDouble(AMOUNT_KEY, amount);
        return tag;
    }

    /** Stable modifier id for the reward at the given index of the player reward list. */
    public static ResourceLocation modifierId(final int rewardIndex) {
        return ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "god_trial_reward_" + rewardIndex);
    }
}
