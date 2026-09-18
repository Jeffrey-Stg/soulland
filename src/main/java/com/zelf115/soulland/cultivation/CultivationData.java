package com.zelf115.soulland.cultivation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.zelf115.soulland.spirit.Affinity;
import com.zelf115.soulland.trial.GodTrial;
import com.zelf115.soulland.trial.GodTrialReward;
import com.zelf115.soulland.trial.TrialTask;
import com.zelf115.soulland.trial.TrialTasks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
    private static final int MIN_MOVEMENT_USAGE_PERCENT = 10;
    private static final int MAX_MOVEMENT_USAGE_PERCENT = 100;

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
    // A second martial soul, rolled rarely off a high innate stat; null for most players
    private MartialSoul secondaryMartialSoul;
    // Set when a twin-soul chance roll succeeds, until the bonus pick lands
    private boolean secondMartialSoulPending = false;
    // Whether the player is at a bottleneck (XP-capped until breakthrough)
    private boolean inBottleneck = false;
    // Number of consecutive breakthrough failures (resets on success)
    private int breakthroughFailures = 0;
    // Total successful breakthroughs; gates the twin-soul chance roll to the first two
    private int successfulBreakthroughCount = 0;
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
    // The god trial undertaken this rebirth; null until an altar starts one
    private GodTrial godTrial;
    // The five tasks rolled when the trial started, in order
    private final List<TrialTask> godTrialTasks = new ArrayList<>();
    // How far through those tasks the player is; reaching the task count finishes the trial
    private int godTrialTaskIndex = 0;
    // Progress toward the current task only
    private int godTrialProgress = 0;
    // Rewards earned by finishing tasks but not yet collected at the altar
    private int godTrialPendingRewards = 0;
    // Every reward granted so far; the index is the modifier id suffix
    private final List<GodTrialReward> godTrialRewards = new ArrayList<>();
    // Which gods the player has passed; this is what lets them wield a relic, and it outlives rebirth
    private final Set<GodTrial> completedGodTrials = EnumSet.noneOf(GodTrial.class);
    // Round reached in the tournament run of the current period; 0 when no run is open
    private int tournamentRound = 0;
    // Wall-clock time the current tournament run began, so the daily reset survives a server restart
    private long tournamentRunStartedAt = 0L;
    // Whether this period's single run is over, by victory or defeat
    private boolean tournamentRunSpent = false;
    // Current spirit energy resource derived from spirit stat
    private double spiritEnergy = 0.0;
    // Whether the starting spirit energy pool has already been filled once, on first login
    private boolean spiritEnergySeeded = false;
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
    // Which of the two souls the activation key summons, and whose ring track the skills come from
    private SoulSlot activeSoulSlot = SoulSlot.PRIMARY;
    // Index of the ring, inside the active soul's own track, whose skill the cast key uses
    private int selectedRingIndex = 0;

    // ---- Getters ----

    public int getLevel() { return level; }
    public double getXp() { return xp; }
    public int getSoulRingCount() { return absorbedRings.size(); }
    public List<AbsorbedRing> getAbsorbedRings() { return Collections.unmodifiableList(absorbedRings); }
    public Map<String, AbsorbedBone> getSpiritBones() { return Collections.unmodifiableMap(spiritBones); }
    public double getAffinityMultiplier(final Affinity affinity) { return affinityMultipliers.getOrDefault(affinity, 1.0); }
    public MartialSoul getMartialSoul() { return martialSoul; }
    public MartialSoul getSecondaryMartialSoul() { return secondaryMartialSoul; }
    public boolean isSecondMartialSoulPending() { return secondMartialSoulPending; }
    public boolean isInBottleneck() { return inBottleneck; }
    public int getBreakthroughFailures() { return breakthroughFailures; }
    public int getSuccessfulBreakthroughCount() { return successfulBreakthroughCount; }
    public long getBreakthroughCooldownUntil() { return breakthroughCooldownUntil; }
    public String getTitle() { return title; }
    public boolean hasGodInheritance() { return hasGodInheritance; }
    public boolean canMartialSoulReachLevel100() { return martialSoulCanReachLevel100; }
    public int getRebirthCount() { return rebirthCount; }
    public double getPermanentBonusStats() { return permanentBonusStats; }
    public double getSpiritEnergy() { return spiritEnergy; }
    public boolean isSpiritEnergySeeded() { return spiritEnergySeeded; }
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
    public SoulSlot getActiveSoulSlot() { return activeSoulSlot; }
    public int getSelectedRingIndex() { return selectedRingIndex; }
    public GodTrial getGodTrial() { return godTrial; }
    public List<TrialTask> getGodTrialTasks() { return Collections.unmodifiableList(godTrialTasks); }
    public int getGodTrialTaskIndex() { return godTrialTaskIndex; }
    public int getGodTrialProgress() { return godTrialProgress; }
    public int getGodTrialPendingRewards() { return godTrialPendingRewards; }
    public List<GodTrialReward> getGodTrialRewards() { return Collections.unmodifiableList(godTrialRewards); }
    public Set<GodTrial> getCompletedGodTrials() { return Collections.unmodifiableSet(completedGodTrials); }
    public int getTournamentRound() { return tournamentRound; }
    public long getTournamentRunStartedAt() { return tournamentRunStartedAt; }
    public boolean isTournamentRunSpent() { return tournamentRunSpent; }

    // ---- Setters ----

    public void setLevel(int level) { this.level = Math.max(1, Math.min(CultivationManager.MAX_LEVEL, level)); }
    public void setXp(double xp) { this.xp = Math.max(0, xp); }
    public void setInBottleneck(boolean inBottleneck) { this.inBottleneck = inBottleneck; }
    public void setBreakthroughFailures(int failures) { this.breakthroughFailures = Math.max(0, failures); }
    public void setSuccessfulBreakthroughCount(int count) { this.successfulBreakthroughCount = Math.max(0, count); }
    public void setBreakthroughCooldownUntil(long tick) { this.breakthroughCooldownUntil = tick; }
    public void setTitle(String title) { this.title = title == null ? "" : title; }
    public void setHasGodInheritance(boolean value) { this.hasGodInheritance = value; }
    public void setMartialSoulCanReachLevel100(final boolean value) { martialSoulCanReachLevel100 = value; }
    public void setRebirthCount(int count) { this.rebirthCount = Math.max(0, count); }
    public void setPermanentBonusStats(double bonus) { this.permanentBonusStats = bonus; }
    public void setSpiritEnergy(double spiritEnergy) { this.spiritEnergy = Math.max(0.0, spiritEnergy); }
    public void markSpiritEnergySeeded() { this.spiritEnergySeeded = true; }
    public void setRingDisplayMode(RingDisplayMode mode) { this.ringDisplayMode = mode == null ? RingDisplayMode.NONE : mode; }
    public void setExternalBoneVisible(boolean visible) { this.externalBoneVisible = visible; }
    public void setLastMeditationTick(long tick) { this.lastMeditationTick = tick; }
    public void setLastSpiritTick(long tick) { this.lastSpiritTick = tick; }

    public void setAffinityMultiplier(final Affinity affinity, final double multiplier) {
        affinityMultipliers.put(affinity, Math.max(0.0, multiplier));
    }

    public void setActiveSoulSlot(final SoulSlot slot) { this.activeSoulSlot = slot == null ? SoulSlot.PRIMARY : slot; }
    public void setSelectedRingIndex(final int index) { this.selectedRingIndex = Math.max(0, index); }

    public void setGodTrial(final GodTrial trial) { this.godTrial = trial; }
    public void setGodTrialTaskIndex(final int index) { this.godTrialTaskIndex = Math.max(0, index); }
    public void setGodTrialProgress(final int progress) { this.godTrialProgress = Math.max(0, progress); }
    public void setGodTrialPendingRewards(final int count) { this.godTrialPendingRewards = Math.max(0, count); }
    public void setTournamentRound(final int round) { this.tournamentRound = Math.max(0, round); }
    public void setTournamentRunStartedAt(final long epochMillis) { this.tournamentRunStartedAt = epochMillis; }
    public void setTournamentRunSpent(final boolean spent) { this.tournamentRunSpent = spent; }

    public void setGodTrialTasks(final List<TrialTask> tasks) {
        godTrialTasks.clear();
        godTrialTasks.addAll(tasks);
    }

    public void addGodTrialReward(final GodTrialReward reward) { godTrialRewards.add(reward); }

    public void clearGodTrialRewards() { godTrialRewards.clear(); }

    public void addCompletedGodTrial(final GodTrial trial) { completedGodTrials.add(trial); }

    public void setMartialSoul(final MartialSoul martialSoul) { this.martialSoul = martialSoul; }
    public void setSecondaryMartialSoul(final MartialSoul soul) { this.secondaryMartialSoul = soul; }
    public void setSecondMartialSoulPending(final boolean pending) { this.secondMartialSoulPending = pending; }

    public void clearAffinityMultipliers() { affinityMultipliers.clear(); }

    public void setMovementUsagePercent(int movementUsagePercent) {
        this.movementUsagePercent =
                Math.max(MIN_MOVEMENT_USAGE_PERCENT, Math.min(MAX_MOVEMENT_USAGE_PERCENT, movementUsagePercent));
    }

    public void setInnateStat(int innateStat) {
        this.innateStat = Math.max(MIN_INNATE_STAT, Math.min(MAX_INNATE_STAT, innateStat));
    }

    // ---- Convenience ----

    /** The soul held in the given slot, or null when that slot is empty. */
    public MartialSoul getMartialSoul(final SoulSlot slot) {
        return slot == SoulSlot.SECONDARY ? secondaryMartialSoul : martialSoul;
    }

    /** The soul the activation and cast keys act on. */
    public MartialSoul getActiveMartialSoul() {
        return getMartialSoul(activeSoulSlot);
    }

    /** The rings of one soul's own track, in absorption order. */
    public List<AbsorbedRing> getRings(final SoulSlot slot) {
        return absorbedRings.stream().filter(ring -> ring.slot() == slot).toList();
    }

    public boolean hasStartedGodTrial() { return godTrial != null; }

    public boolean isGodTrialFinished() { return godTrialTaskIndex >= TrialTasks.TASK_COUNT; }

    public boolean hasRelicEntitlement(final GodTrial trial) { return completedGodTrials.contains(trial); }

    /** The task the trial is waiting on, or null when no trial is running or it is already finished. */
    public TrialTask getCurrentTrialTask() {
        if (godTrialTaskIndex < 0 || godTrialTaskIndex >= godTrialTasks.size()) {
            return null;
        }
        return godTrialTasks.get(godTrialTaskIndex);
    }

    /** Player tier is soul-ring count + 1, minimum 1. */
    public int getPlayerTier() { return Math.max(1, getSoulRingCount() + 1); }

    /** How many rings the given soul slot's track holds, out of its own cap. */
    public int getRingCount(final SoulSlot slot) {
        return (int) absorbedRings.stream().filter(ring -> ring.slot() == slot).count();
    }

    /** Adds raw cultivation XP to the stored total. */
    public void addXp(double amount) { this.xp += amount; }

    public void addRing(final AbsorbedRing ring) { absorbedRings.add(ring); }

    public void clearAbsorbedRings() { absorbedRings.clear(); }

    public void clearSpiritBones() { spiritBones.clear(); }

    public AbsorbedBone putBone(final AbsorbedBone bone) {
        return spiritBones.put(AbsorbedBone.slotKey(bone.slot()), bone);
    }

    /**
     * The innate stat as it counts for the XP bonus, including the +1 per rebirth.
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
        if (secondaryMartialSoul != null) {
            tag.putString("secondaryMartialSoul", secondaryMartialSoul.name());
        }
        tag.putBoolean("secondMartialSoulPending", secondMartialSoulPending);
        tag.putBoolean("inBottleneck", inBottleneck);
        tag.putInt("breakthroughFailures", breakthroughFailures);
        tag.putInt("successfulBreakthroughCount", successfulBreakthroughCount);
        tag.putLong("breakthroughCooldownUntil", breakthroughCooldownUntil);
        tag.putString("title", title);
        tag.putBoolean("hasGodInheritance", hasGodInheritance);
        tag.putBoolean("martialSoulCanReachLevel100", martialSoulCanReachLevel100);
        tag.putInt("rebirthCount", rebirthCount);
        tag.putDouble("permanentBonusStats", permanentBonusStats);
        tag.putDouble("spiritEnergy", spiritEnergy);
        tag.putBoolean("spiritEnergySeeded", spiritEnergySeeded);
        tag.putInt("movementUsagePercent", movementUsagePercent);
        tag.putInt("innateStat", innateStat);
        tag.putInt("ringDisplayMode", ringDisplayMode.ordinal());
        tag.putBoolean("externalBoneVisible", externalBoneVisible);
        tag.putLong("lastMeditationTick", lastMeditationTick);
        tag.putLong("lastSpiritTick", lastSpiritTick);
        tag.putBoolean("martialSoulActive", martialSoulActive);
        tag.putLong("martialSoulBuffUntil", martialSoulBuffUntil);
        tag.putString("activeSoulSlot", activeSoulSlot.name());
        tag.putInt("selectedRingIndex", selectedRingIndex);
        if (godTrial != null) {
            tag.putString("godTrial", godTrial.name());
        }
        tag.put("godTrialTasks", writeTrialTasks());
        tag.putInt("godTrialTaskIndex", godTrialTaskIndex);
        tag.putInt("godTrialProgress", godTrialProgress);
        tag.putInt("godTrialPendingRewards", godTrialPendingRewards);
        tag.put("godTrialRewards", writeTrialRewards());
        tag.put("godTrialsCompleted", writeCompletedTrials());
        tag.putInt("tournamentRound", tournamentRound);
        tag.putLong("tournamentRunStartedAt", tournamentRunStartedAt);
        tag.putBoolean("tournamentRunSpent", tournamentRunSpent);
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
        if (tag.contains("secondaryMartialSoul")) {
            try {
                secondaryMartialSoul = MartialSoul.valueOf(tag.getString("secondaryMartialSoul"));
            } catch (IllegalArgumentException ignored) {
                secondaryMartialSoul = null;
            }
        }
        secondMartialSoulPending = tag.getBoolean("secondMartialSoulPending");
        inBottleneck = tag.getBoolean("inBottleneck");
        breakthroughFailures = tag.getInt("breakthroughFailures");
        successfulBreakthroughCount = tag.getInt("successfulBreakthroughCount");
        breakthroughCooldownUntil = tag.getLong("breakthroughCooldownUntil");
        title = tag.getString("title");
        hasGodInheritance = tag.getBoolean("hasGodInheritance");
        martialSoulCanReachLevel100 = tag.getBoolean("martialSoulCanReachLevel100");
        rebirthCount = tag.getInt("rebirthCount");
        permanentBonusStats = tag.getDouble("permanentBonusStats");
        spiritEnergy = Math.max(0.0, tag.getDouble("spiritEnergy"));
        spiritEnergySeeded = tag.getBoolean("spiritEnergySeeded");
        setMovementUsagePercent(tag.contains("movementUsagePercent") ? tag.getInt("movementUsagePercent") : 100);
        setInnateStat(tag.contains("innateStat") ? tag.getInt("innateStat") : NEUTRAL_INNATE_STAT);
        ringDisplayMode = RingDisplayMode.byOrdinal(tag.getInt("ringDisplayMode"));
        externalBoneVisible = !tag.contains("externalBoneVisible") || tag.getBoolean("externalBoneVisible");
        lastMeditationTick = tag.getLong("lastMeditationTick");
        lastSpiritTick = tag.getLong("lastSpiritTick");
        martialSoulActive = tag.getBoolean("martialSoulActive");
        martialSoulBuffUntil = tag.getLong("martialSoulBuffUntil");
        activeSoulSlot = readSoulSlot(tag.getString("activeSoulSlot"));
        selectedRingIndex = Math.max(0, tag.getInt("selectedRingIndex"));
        godTrial = readGodTrial(tag);
        readTrialTasks(tag.getList("godTrialTasks", Tag.TAG_COMPOUND));
        godTrialTaskIndex = Math.max(0, tag.getInt("godTrialTaskIndex"));
        godTrialProgress = Math.max(0, tag.getInt("godTrialProgress"));
        godTrialPendingRewards = Math.max(0, tag.getInt("godTrialPendingRewards"));
        readTrialRewards(tag.getList("godTrialRewards", Tag.TAG_COMPOUND));
        readCompletedTrials(tag.getList("godTrialsCompleted", Tag.TAG_STRING));
        tournamentRound = Math.max(0, tag.getInt("tournamentRound"));
        tournamentRunStartedAt = tag.getLong("tournamentRunStartedAt");
        tournamentRunSpent = tag.getBoolean("tournamentRunSpent");
    }

    private static GodTrial readGodTrial(final CompoundTag tag) {
        if (!tag.contains("godTrial")) {
            return null;
        }
        try {
            return GodTrial.valueOf(tag.getString("godTrial"));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private ListTag writeTrialTasks() {
        final ListTag list = new ListTag();
        for (final TrialTask task : godTrialTasks) {
            list.add(task.toNbt());
        }
        return list;
    }

    private ListTag writeTrialRewards() {
        final ListTag list = new ListTag();
        for (final GodTrialReward reward : godTrialRewards) {
            list.add(reward.toNbt());
        }
        return list;
    }

    private ListTag writeCompletedTrials() {
        final ListTag list = new ListTag();
        for (final GodTrial trial : completedGodTrials) {
            list.add(StringTag.valueOf(trial.name()));
        }
        return list;
    }

    private void readTrialTasks(final ListTag list) {
        godTrialTasks.clear();
        for (int index = 0; index < list.size(); index++) {
            godTrialTasks.add(TrialTask.readFrom(list.getCompound(index)));
        }
    }

    private void readTrialRewards(final ListTag list) {
        godTrialRewards.clear();
        for (int index = 0; index < list.size(); index++) {
            godTrialRewards.add(GodTrialReward.readFrom(list.getCompound(index)));
        }
    }

    private void readCompletedTrials(final ListTag list) {
        completedGodTrials.clear();
        for (int index = 0; index < list.size(); index++) {
            try {
                completedGodTrials.add(GodTrial.valueOf(list.getString(index)));
            } catch (IllegalArgumentException ignored) {
                // Ignore a god removed or renamed by another version.
            }
        }
    }

    private static SoulSlot readSoulSlot(final String name) {
        try {
            return SoulSlot.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return SoulSlot.PRIMARY;
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
