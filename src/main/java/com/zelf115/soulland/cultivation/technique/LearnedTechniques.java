package com.zelf115.soulland.cultivation.technique;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;

/** The techniques a player has learned from books, with the progress each one has built up. */
public final class LearnedTechniques {

    private final Map<Technique, Long> progress = new EnumMap<>(Technique.class);

    // Runtime state only: never saved, so a relog closes the eye and clears cooldowns.
    private boolean demonEyeOpen;
    private long demonEyePulseUntil;
    private long demonEyeStrikeReadyAt;
    private long shadowStepReadyAt;

    public boolean isLearned(final Technique technique) {
        return progress.containsKey(technique);
    }

    public void learn(final Technique technique) {
        progress.putIfAbsent(technique, 0L);
    }

    public Set<Technique> learned() {
        return Collections.unmodifiableSet(progress.keySet());
    }

    public long getProgress(final Technique technique) {
        return progress.getOrDefault(technique, 0L);
    }

    public void setProgress(final Technique technique, final long value) {
        progress.put(technique, Math.max(0L, value));
    }

    public void addProgress(final Technique technique, final long amount) {
        if (!isLearned(technique)) return;
        progress.merge(technique, amount, Long::sum);
    }

    public int level(final Technique technique) {
        return technique.levelFor(getProgress(technique));
    }

    public boolean isDemonEyeOpen() { return demonEyeOpen; }
    public void setDemonEyeOpen(final boolean open) { demonEyeOpen = open; }
    public long getDemonEyePulseUntil() { return demonEyePulseUntil; }
    public void setDemonEyePulseUntil(final long tick) { demonEyePulseUntil = tick; }
    public long getDemonEyeStrikeReadyAt() { return demonEyeStrikeReadyAt; }
    public void setDemonEyeStrikeReadyAt(final long tick) { demonEyeStrikeReadyAt = tick; }
    public long getShadowStepReadyAt() { return shadowStepReadyAt; }
    public void setShadowStepReadyAt(final long tick) { shadowStepReadyAt = tick; }

    public CompoundTag toNbt() {
        final CompoundTag tag = new CompoundTag();
        progress.forEach((technique, value) -> tag.putLong(technique.name(), value));
        return tag;
    }

    public static LearnedTechniques readFrom(final CompoundTag tag) {
        final LearnedTechniques techniques = new LearnedTechniques();
        for (final String name : tag.getAllKeys()) {
            try {
                techniques.setProgress(Technique.valueOf(name), tag.getLong(name));
            } catch (IllegalArgumentException ignored) {
                // Ignore a technique removed or renamed by another version.
            }
        }
        return techniques;
    }
}
