package com.zelf115.soulland.trial;

import net.minecraft.nbt.CompoundTag;

/** One step of a god trial: what to do, and how many times. */
public record TrialTask(TrialTaskType type, int target) {

    private static final String TYPE_KEY = "Type";
    private static final String TARGET_KEY = "Target";
    private static final int SINGLE = 1;

    public static TrialTask readFrom(final CompoundTag tag) {
        return new TrialTask(readType(tag), Math.max(SINGLE, tag.getInt(TARGET_KEY)));
    }

    private static TrialTaskType readType(final CompoundTag tag) {
        try {
            return TrialTaskType.valueOf(tag.getString(TYPE_KEY));
        } catch (IllegalArgumentException ignored) {
            return TrialTaskType.REACH_LEVEL_99;
        }
    }

    public CompoundTag toNbt() {
        final CompoundTag tag = new CompoundTag();
        tag.putString(TYPE_KEY, type.name());
        tag.putInt(TARGET_KEY, target);
        return tag;
    }
}
