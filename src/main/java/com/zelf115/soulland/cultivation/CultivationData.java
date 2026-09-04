package com.zelf115.soulland.cultivation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.zelf115.soulland.spirit.Affinity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Per-player cultivation data stored via NeoForge attachment.
 *
 * <p>Everything cultivation-related lives here rather than in {@code Player.getPersistentData()},
 * because only the attachment is carried across death.
 */
public class CultivationData implements INBTSerializable<CompoundTag> {

    public static final int MIN_INNATE_STAT = 1;
    public static final int MAX_INNATE_STAT = 20;
    public static final int NEUTRAL_INNATE_STAT = 10;
    private static final int MIN_MOVEMENT_USAGE_PERCENT = 25;
    private static final int MAX_MOVEMENT_USAGE_PERCENT = 200;

    // Current cultivation level (1–120)
    private int level = 1;
    // Accumulated XP toward the next level (or toward breakthrough if bottlenecked)
    private double xp = 0.0;
    // Soul rings absorbed, in absorption order; the index is the modifier id suffix
    private final List<AbsorbedRing> absorbedRings = new ArrayList<>();
    // Spirit bones absorbed, keyed by normalized slot name; one per slot
    private final Map<String, AbsorbedBone> spiritBones = new LinkedHashMap<>();
    private final Map<Affinity, Double> affinityMultipliers = new LinkedHashMap<>();
    private MartialSoul martialSoul;
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
    private boolean martialSoulCanReachLevel100 = false;
    // Number of times the player has reborn (gate for level 100 as an alternative)
    private int rebirthCount = 0;
    // Permanent flat bonus applied to all stats, accumulated across rebirths
    private double permanentBonusStats = 0.0;
    // Current spirit energy resource derived from spirit stat
    private double spiritEnergy = 0.0;
    // Percentage of the derived movement speed the player wants to actively use
    private int movementUsagePercent = 100;
    // Innate stat rolled 1–20 when the martial soul is chosen; drives the XP bonus
    private int innateStat = NEUTRAL_INNATE_STAT;
    // How absorbed soul rings are shown
    private RingDisplayMode ringDisplayMode = RingDisplayMode.NONE;
    // Whether an equipped external spirit bone is currently shown
    private boolean externalBoneVisible = true;
    // Server tick of the last meditation XP grant
    private long lastMeditationTick = 0L;
    // Server tick of the last bottleneck spirit grant
    private long lastSpiritTick = 0L;
    private boolean martialSoulActive = false;
    private long martialSoulBuffUntil = 0L;

    // ---- Getters ----

    public int getLevel() { return level; }
    public double getXp() { return xp; }
    public int getSoulRingCount() { return absorbedRings.size(); }
    public List<AbsorbedRing> getAbsorbedRings() { return Collections.unmodifiableList(absorbedRings); }
    public Map<String, AbsorbedBone> getSpiritBones() { return Collections.unmodifiableMap(spiritBones); }
    public double getAffinityMultiplier(final Affinity affinity) { return affinityMultipliers.getOrDefault(affinity, 1.0); }
    public MartialSoul getMartialSoul() { return martialSoul; }
    public boolean isInBottleneck() { return inBottleneck; }
    public int getBreakthroughFailures() { return breakthroughFailures; }
    public long getBreakthroughCooldownUntil() { return breakthroughCooldownUntil; }
    public String getTitle() { return title; }
    public boolean hasGodInheritance() { return hasGodInheritance; }
    public boolean canMartialSoulReachLevel100() { return martialSoulCanReachLevel100; }
    public int getRebirthCount() { return rebirthCount; }
    public double getPermanentBonusStats() { return permanentBonusStats; }
    public double getSpiritEnergy() { return spiritEnergy; }
    public int getMovementUsagePercent() { return movementUsagePercent; }
    public int getInnateStat() { return innateStat; }
    public RingDisplayMode getRingDisplayMode() { return ringDisplayMode; }
    public boolean isExternalBoneVisible() { return externalBoneVisible; }
    public long getLastMeditationTick() { return lastMeditationTick; }
    public long getLastSpiritTick() { return lastSpiritTick; }
    public boolean isMartialSoulActive() { return martialSoulActive; }
    public void setMartialSoulActive(final boolean active) { martialSoulActive = active; }
    public long getMartialSoulBuffUntil() { return martialSoulBuffUntil; }
    public void setMartialSoulBuffUntil(final long tick) { martialSoulBuffUntil = tick; }

    // ---- Setters ----

    public void setLevel(int level) { this.level = Math.max(1, Math.min(CultivationManager.MAX_LEVEL, level)); }
    public void setXp(double xp) { this.xp = Math.max(0, xp); }
    public void setInBottleneck(boolean inBottleneck) { this.inBottleneck = inBottleneck; }
    public void setBreakthroughFailures(int failures) { this.breakthroughFailures = Math.max(0, failures); }
    public void setBreakthroughCooldownUntil(long tick) { this.breakthroughCooldownUntil = tick; }
    public void setTitle(String title) { this.title = title == null ? "" : title; }
    public void setHasGodInheritance(boolean value) { this.hasGodInheritance = value; }
    public void setMartialSoulCanReachLevel100(final boolean value) { martialSoulCanReachLevel100 = value; }
    public void setRebirthCount(int count) { this.rebirthCount = Math.max(0, count); }
    public void setPermanentBonusStats(double bonus) { this.permanentBonusStats = bonus; }
    public void setSpiritEnergy(double spiritEnergy) { this.spiritEnergy = Math.max(0.0, spiritEnergy); }
    public void setRingDisplayMode(RingDisplayMode mode) { this.ringDisplayMode = mode == null ? RingDisplayMode.NONE : mode; }
    public void setExternalBoneVisible(boolean visible) { this.externalBoneVisible = visible; }
    public void setLastMeditationTick(long tick) { this.lastMeditationTick = tick; }
    public void setLastSpiritTick(long tick) { this.lastSpiritTick = tick; }

    public void setAffinityMultiplier(final Affinity affinity, final double multiplier) {
        affinityMultipliers.put(affinity, Math.max(0.0, multiplier));
    }

    public void setMartialSoul(final MartialSoul martialSoul) { this.martialSoul = martialSoul; }

    public void clearAffinityMultipliers() { affinityMultipliers.clear(); }

    public void setMovementUsagePercent(int movementUsagePercent) {
        this.movementUsagePercent =
                Math.max(MIN_MOVEMENT_USAGE_PERCENT, Math.min(MAX_MOVEMENT_USAGE_PERCENT, movementUsagePercent));
    }

    public void setInnateStat(int innateStat) {
        this.innateStat = Math.max(MIN_INNATE_STAT, Math.min(MAX_INNATE_STAT, innateStat));
    }

    // ---- Convenience ----

    /** Player tier is soul-ring count + 1, minimum 1. */
    public int getPlayerTier() { return Math.max(1, getSoulRingCount() + 1); }

    /** Adds raw cultivation XP to the stored total. */
    public void addXp(double amount) { this.xp += amount; }

    public void addRing(final AbsorbedRing ring) { absorbedRings.add(ring); }

    public AbsorbedBone putBone(final AbsorbedBone bone) {
        return spiritBones.put(AbsorbedBone.slotKey(bone.slot()), bone);
    }

    /**
     * The innate stat as it counts for the XP bonus, including the +1 per rebirth from issue #23.
     */
    public int getEffectiveInnateStat() {
        return Math.min(MAX_INNATE_STAT, innateStat + rebirthCount);
    }

    // ---- NBT Serialization ----

    @Override
    public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("level", level);
        tag.putDouble("xp", xp);
        tag.put("absorbedRings", writeRings());
        tag.put("spiritBones", writeBones());
        CompoundTag affinities = new CompoundTag();
        affinityMultipliers.forEach((affinity, multiplier) -> affinities.putDouble(affinity.name(), multiplier));
        tag.put("affinityMultipliers", affinities);
        if (martialSoul != null) {
            tag.putString("martialSoul", martialSoul.name());
        }
        tag.putBoolean("inBottleneck", inBottleneck);
        tag.putInt("breakthroughFailures", breakthroughFailures);
        tag.putLong("breakthroughCooldownUntil", breakthroughCooldownUntil);
        tag.putString("title", title);
        tag.putBoolean("hasGodInheritance", hasGodInheritance);
        tag.putBoolean("martialSoulCanReachLevel100", martialSoulCanReachLevel100);
        tag.putInt("rebirthCount", rebirthCount);
        tag.putDouble("permanentBonusStats", permanentBonusStats);
        tag.putDouble("spiritEnergy", spiritEnergy);
        tag.putInt("movementUsagePercent", movementUsagePercent);
        tag.putInt("innateStat", innateStat);
        tag.putInt("ringDisplayMode", ringDisplayMode.ordinal());
        tag.putBoolean("externalBoneVisible", externalBoneVisible);
        tag.putLong("lastMeditationTick", lastMeditationTick);
        tag.putLong("lastSpiritTick", lastSpiritTick);
        tag.putBoolean("martialSoulActive", martialSoulActive);
        tag.putLong("martialSoulBuffUntil", martialSoulBuffUntil);
        return tag;
    }

    @Override
    public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, CompoundTag tag) {
        setLevel(tag.getInt("level"));
        xp = tag.getDouble("xp");
        readRings(tag.getList("absorbedRings", Tag.TAG_COMPOUND));
        readBones(tag.getCompound("spiritBones"));
        readAffinities(tag.getCompound("affinityMultipliers"));
        if (tag.contains("martialSoul")) {
            try {
                martialSoul = MartialSoul.valueOf(tag.getString("martialSoul"));
            } catch (IllegalArgumentException ignored) {
                martialSoul = null;
            }
        }
        inBottleneck = tag.getBoolean("inBottleneck");
        breakthroughFailures = tag.getInt("breakthroughFailures");
        breakthroughCooldownUntil = tag.getLong("breakthroughCooldownUntil");
        title = tag.getString("title");
        hasGodInheritance = tag.getBoolean("hasGodInheritance");
        martialSoulCanReachLevel100 = tag.getBoolean("martialSoulCanReachLevel100");
        rebirthCount = tag.getInt("rebirthCount");
        permanentBonusStats = tag.getDouble("permanentBonusStats");
        spiritEnergy = Math.max(0.0, tag.getDouble("spiritEnergy"));
        setMovementUsagePercent(tag.contains("movementUsagePercent") ? tag.getInt("movementUsagePercent") : 100);
        setInnateStat(tag.contains("innateStat") ? tag.getInt("innateStat") : NEUTRAL_INNATE_STAT);
        ringDisplayMode = RingDisplayMode.byOrdinal(tag.getInt("ringDisplayMode"));
        externalBoneVisible = !tag.contains("externalBoneVisible") || tag.getBoolean("externalBoneVisible");
        lastMeditationTick = tag.getLong("lastMeditationTick");
        lastSpiritTick = tag.getLong("lastSpiritTick");
        martialSoulActive = tag.getBoolean("martialSoulActive");
        martialSoulBuffUntil = tag.getLong("martialSoulBuffUntil");
        if (martialSoul != null && !martialSoul.isEvolution()) {
            affinityMultipliers.clear();
            martialSoul.affinityMultipliers().forEach(this::setAffinityMultiplier);
        }
    }

    private ListTag writeRings() {
        final ListTag list = new ListTag();
        for (final AbsorbedRing ring : absorbedRings) {
            list.add(ring.toNbt());
        }
        return list;
    }

    private CompoundTag writeBones() {
        final CompoundTag tag = new CompoundTag();
        spiritBones.forEach((slotKey, bone) -> tag.put(slotKey, bone.toNbt()));
        return tag;
    }

    private void readRings(final ListTag list) {
        absorbedRings.clear();
        for (int index = 0; index < list.size(); index++) {
            absorbedRings.add(AbsorbedRing.readFrom(list.getCompound(index)));
        }
    }

    private void readBones(final CompoundTag tag) {
        spiritBones.clear();
        for (final String slotKey : tag.getAllKeys()) {
            spiritBones.put(slotKey, AbsorbedBone.readFrom(tag.getCompound(slotKey)));
        }
    }

    private void readAffinities(final CompoundTag tag) {
        affinityMultipliers.clear();
        for (final String name : tag.getAllKeys()) {
            try {
                affinityMultipliers.put(Affinity.valueOf(name), Math.max(0.0, tag.getDouble(name)));
            } catch (IllegalArgumentException ignored) {
                // Ignore affinity names removed or added by another version.
            }
        }
    }
}
