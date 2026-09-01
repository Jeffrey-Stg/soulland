package com.zelf115.soulland.cultivation;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Per-player cultivation data stored via NeoForge attachment.
 */
public class CultivationData implements INBTSerializable<CompoundTag> {

    // Current cultivation level (1–120)
    private int level = 1;
    // Accumulated XP toward the next level (or toward breakthrough if bottlenecked)
    private double xp = 0.0;
    // Number of soul rings absorbed (determines player tier)
    private int soulRingCount = 0;
    // Whether the player is at a bottleneck (XP-capped until breakthrough)
    private boolean inBottleneck = false;
    // Number of consecutive breakthrough failures (resets on success)
    private int breakthroughFailures = 0;
    // Server game-time (ticks) when the next breakthrough attempt is allowed
    private long breakthroughCooldownUntil = 0L;
    // Player-chosen title (unlocked at level 90)
    private String title = "";
    // Whether the player has completed a god inheritance quest (gate for level 100)
    private boolean hasGodInheritance = false;
    // Number of times the player has reborn (gate for level 100 as an alternative)
    private int rebirthCount = 0;
    // Permanent flat bonus applied to all stats, accumulated across rebirths
    private double permanentBonusStats = 0.0;

    // ---- Getters ----

    public int getLevel() { return level; }
    public double getXp() { return xp; }
    public int getSoulRingCount() { return soulRingCount; }
    public boolean isInBottleneck() { return inBottleneck; }
    public int getBreakthroughFailures() { return breakthroughFailures; }
    public long getBreakthroughCooldownUntil() { return breakthroughCooldownUntil; }
    public String getTitle() { return title; }
    public boolean hasGodInheritance() { return hasGodInheritance; }
    public int getRebirthCount() { return rebirthCount; }
    public double getPermanentBonusStats() { return permanentBonusStats; }

    // ---- Setters ----

    public void setLevel(int level) { this.level = Math.max(1, Math.min(120, level)); }
    public void setXp(double xp) { this.xp = Math.max(0, xp); }
    public void setSoulRingCount(int count) { this.soulRingCount = Math.max(0, count); }
    public void setInBottleneck(boolean inBottleneck) { this.inBottleneck = inBottleneck; }
    public void setBreakthroughFailures(int failures) { this.breakthroughFailures = Math.max(0, failures); }
    public void setBreakthroughCooldownUntil(long tick) { this.breakthroughCooldownUntil = tick; }
    public void setTitle(String title) { this.title = title == null ? "" : title; }
    public void setHasGodInheritance(boolean value) { this.hasGodInheritance = value; }
    public void setRebirthCount(int count) { this.rebirthCount = Math.max(0, count); }
    public void setPermanentBonusStats(double bonus) { this.permanentBonusStats = bonus; }

    // ---- Convenience ----

    /** Player tier is soul-ring count + 1, minimum 1. */
    public int getPlayerTier() { return Math.max(1, soulRingCount + 1); }

    /** Adds XP and returns the amount actually added (capped at max if provided). */
    public void addXp(double amount) { this.xp += amount; }

    // ---- NBT Serialization ----

    @Override
    public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("level", level);
        tag.putDouble("xp", xp);
        tag.putInt("soulRingCount", soulRingCount);
        tag.putBoolean("inBottleneck", inBottleneck);
        tag.putInt("breakthroughFailures", breakthroughFailures);
        tag.putLong("breakthroughCooldownUntil", breakthroughCooldownUntil);
        tag.putString("title", title);
        tag.putBoolean("hasGodInheritance", hasGodInheritance);
        tag.putInt("rebirthCount", rebirthCount);
        tag.putDouble("permanentBonusStats", permanentBonusStats);
        return tag;
    }

    @Override
    public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, CompoundTag tag) {
        level = tag.getInt("level");
        if (level < 1) level = 1;
        xp = tag.getDouble("xp");
        soulRingCount = tag.getInt("soulRingCount");
        inBottleneck = tag.getBoolean("inBottleneck");
        breakthroughFailures = tag.getInt("breakthroughFailures");
        breakthroughCooldownUntil = tag.getLong("breakthroughCooldownUntil");
        title = tag.getString("title");
        hasGodInheritance = tag.getBoolean("hasGodInheritance");
        rebirthCount = tag.getInt("rebirthCount");
        permanentBonusStats = tag.getDouble("permanentBonusStats");
    }
}
