# Martial Soul Selection & Twin-Soul System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let a player pick a martial soul through a GUI, roll the innate stat and twin-soul chance, and make the tool ability / ring tracks / evolution / HUD all understand a possible second soul.

**Architecture:** Reuses the existing but unused `AffinitySystem.chooseMartialSoul` as the server-side "apply a pick" entry point; adds a client `Screen` (category → soul list) driven by two new network payloads; tags each `AbsorbedRing` with a `SoulSlot` so ring caps, evolution and HUD display can filter per soul; extends the existing (but never wired) `RingDisplayMode` enum instead of adding new client state.

**Tech Stack:** NeoForge 21.1.249, Minecraft 1.21.1, Java 21.

**Spec:** docs/superpowers/specs/2026-09-06-martial-soul-selection-design.md

## Global Constraints

- No test harness exists in this repo (confirmed: no test source set, no JUnit dependency). Verification is `./gradlew compileJava` after each task plus the manual check listed in that task.
- Match existing style: named constants for anything that isn't 0/1, guard clauses, no boolean-flag parameters, comments only where they carry a decision (see `~/.claude/skills/clean-code`).
- Twin-soul chance formula: `20 + 2 * (innateStat - 10)`, percent, clamped `[0, 100]`, and only rolled when `innateStat >= 10`.
- Ring cap per track = `CultivationManager.maxSoulRingCountForLevel(level)` (existing, unchanged formula) unless the soul occupying that track overrides it (Seven Treasure Glazed Tile Pagoda → 7, Nine Treasure Glazed Tile Pagoda → 9).
- Descoped, do not build: rebirth itself (only a hook method), Nine Heart Begonia's ring-skill lock (no active-skill system exists), and the Pagoda's per-index Berserk/Greater Berserk/Greatest Berserk buffs (undefined mechanic).

---

### Task 1: `SoulSlot` enum and `AbsorbedRing` slot tagging

**Files:**
- Create: `src/main/java/com/zelf115/soulland/cultivation/SoulSlot.java`
- Modify: `src/main/java/com/zelf115/soulland/cultivation/AbsorbedRing.java`

**Interfaces:**
- Produces: `SoulSlot` enum with values `PRIMARY`, `SECONDARY`. `AbsorbedRing.slot()` accessor; `AbsorbedRing` constructor gains a `SoulSlot slot` parameter as its last component.

- [ ] **Step 1: Create `SoulSlot`**

```java
package com.zelf115.soulland.cultivation;

/** Which of a player's (up to two) martial souls a soul ring belongs to. */
public enum SoulSlot {
    PRIMARY,
    SECONDARY
}
```

- [ ] **Step 2: Add the field to `AbsorbedRing`**

Change the record and its NBT read/write to carry a slot, defaulting to `PRIMARY` for rings absorbed before this change (missing tag):

```java
package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.StatBonus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/** A soul ring the player has absorbed, kept so it can be rendered, re-applied and inspected. */
public record AbsorbedRing(String sourceName, int tier, int years, StatBonus bonus, SoulSlot slot) {

    public static final String SOURCE_NAME_KEY = "SourceName";
    public static final String TIER_KEY = "Tier";
    public static final String YEARS_KEY = "Years";
    private static final String SLOT_KEY = "Slot";

    public static AbsorbedRing readFrom(final CompoundTag tag) {
        return new AbsorbedRing(
                tag.getString(SOURCE_NAME_KEY),
                tag.getInt(TIER_KEY),
                tag.getInt(YEARS_KEY),
                StatBonus.readFrom(tag),
                readSlot(tag));
    }

    private static SoulSlot readSlot(final CompoundTag tag) {
        if (!tag.contains(SLOT_KEY)) {
            return SoulSlot.PRIMARY;
        }
        try {
            return SoulSlot.valueOf(tag.getString(SLOT_KEY));
        } catch (IllegalArgumentException ignored) {
            return SoulSlot.PRIMARY;
        }
    }

    public CompoundTag toNbt() {
        final CompoundTag tag = new CompoundTag();
        tag.putString(SOURCE_NAME_KEY, sourceName);
        tag.putInt(TIER_KEY, tier);
        tag.putInt(YEARS_KEY, years);
        tag.putString(SLOT_KEY, slot.name());
        bonus.writeTo(tag);
        return tag;
    }

    /** Stable modifier id for the ring in the given slot of the player ring list. */
    public static ResourceLocation modifierId(final int ringIndex) {
        return ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "soul_ring_" + ringIndex);
    }
}
```

- [ ] **Step 3: Compile**

Run: `./gradlew compileJava`
Expected: FAILS — `SoulRingAbsorption.grant` still calls the 4-arg `AbsorbedRing` constructor. That's fixed in Task 2.

---

### Task 2: Per-track ring caps in `SoulRingAbsorption` and `MartialSoul.ringCapOverride`

**Files:**
- Modify: `src/main/java/com/zelf115/soulland/cultivation/MartialSoul.java`
- Modify: `src/main/java/com/zelf115/soulland/cultivation/CultivationData.java`
- Modify: `src/main/java/com/zelf115/soulland/cultivation/SoulRingAbsorption.java`

**Interfaces:**
- Consumes: `SoulSlot` (Task 1), `CultivationManager.maxSoulRingCountForLevel(int)` (existing).
- Produces: `MartialSoul.ringCapOverride()` returning `OptionalInt`; `CultivationData.getRingCount(SoulSlot)`, `CultivationData.getSecondaryMartialSoul()`/`setSecondaryMartialSoul(MartialSoul)`.

- [ ] **Step 1: Add the cap override to `MartialSoul`**

Add this method to `MartialSoul` (near `isEvolution()`):

```java
/** Seven/Nine Treasure Glazed Tile Pagoda cap their own ring track below the normal level formula. */
public java.util.OptionalInt ringCapOverride() {
    return switch (this) {
        case SEVEN_TREASURE_GLAZED_TILE_PAGODA -> java.util.OptionalInt.of(7);
        case NINE_TREASURE_GLAZED_TILE_PAGODA -> java.util.OptionalInt.of(9);
        default -> java.util.OptionalInt.empty();
    };
}
```

- [ ] **Step 2: Add `secondaryMartialSoul` and `getRingCount` to `CultivationData`**

Add the field next to `martialSoul`:

```java
private MartialSoul secondaryMartialSoul;
```

Add getter/setter next to the existing martial soul ones:

```java
public MartialSoul getSecondaryMartialSoul() { return secondaryMartialSoul; }
public void setSecondaryMartialSoul(final MartialSoul soul) { this.secondaryMartialSoul = soul; }
```

Add a ring-count-by-slot helper next to `getSoulRingCount()`:

```java
public int getRingCount(final SoulSlot slot) {
    return (int) absorbedRings.stream().filter(ring -> ring.slot() == slot).count();
}
```

Serialize/deserialize it in `serializeNBT`/`deserializeNBT` next to `martialSoul`:

```java
if (secondaryMartialSoul != null) {
    tag.putString("secondaryMartialSoul", secondaryMartialSoul.name());
}
```

```java
if (tag.contains("secondaryMartialSoul")) {
    try {
        secondaryMartialSoul = MartialSoul.valueOf(tag.getString("secondaryMartialSoul"));
    } catch (IllegalArgumentException ignored) {
        secondaryMartialSoul = null;
    }
}
```

Remove the old re-apply-affinities-on-load block at the end of `deserializeNBT` (`if (martialSoul != null && !martialSoul.isEvolution()) {...}`) — Task 4 replaces it with a call that accounts for both souls.

- [ ] **Step 3: Make ring absorption slot-aware in `SoulRingAbsorption`**

Replace the level-limit check and `grant` method:

```java
private static SoulSlot resolveSlotForNewRing(final CultivationData data) {
    if (data.getRingCount(SoulSlot.PRIMARY) < capFor(data.getMartialSoul(), data.getLevel())) {
        return SoulSlot.PRIMARY;
    }
    if (data.getSecondaryMartialSoul() != null
            && data.getRingCount(SoulSlot.SECONDARY) < capFor(data.getSecondaryMartialSoul(), data.getLevel())) {
        return SoulSlot.SECONDARY;
    }
    return null;
}

private static int capFor(final MartialSoul soul, final int level) {
    if (soul != null) {
        final java.util.OptionalInt override = soul.ringCapOverride();
        if (override.isPresent()) {
            return override.getAsInt();
        }
    }
    return CultivationManager.maxSoulRingCountForLevel(level);
}
```

In `absorb` and `absorbByOverreach`, replace
`if (data.getSoulRingCount() >= CultivationManager.maxSoulRingCountForLevel(data.getLevel())) {`
with:
`final SoulSlot slot = resolveSlotForNewRing(data);`
`if (slot == null) {`
(both occurrences — the condition body stays `return Result.LEVEL_LIMIT;`).

Thread `slot` through to `grant`, updating its signature and the two call sites (`grant(player, data, stack, tag);` becomes `grant(player, data, stack, tag, slot);`):

```java
private static void grant(final Player player, final CultivationData data, final ItemStack stack,
                          final CompoundTag tag, final SoulSlot slot) {
    final AbsorbedRing ring = new AbsorbedRing(
            tag.getString(AbsorbedRing.SOURCE_NAME_KEY), tierOf(tag), tag.getInt(AbsorbedRing.YEARS_KEY),
            StatBonus.readFrom(tag), slot);
    data.addRing(ring);
    Stats.applyBonus(player, AbsorbedRing.modifierId(data.getSoulRingCount() - 1),
        ring.bonus().scaled(AffinitySystem.ringMultiplier(player, tag)));
    MartialSoulEvolution.tryEvolve(player, data);
    Stats.syncDerivedPlayerStats(player, data);
    destroy(stack);
}
```

Also update `absorbByOverreach`'s inner call `return absorb(player, stack);` — unchanged, it re-enters `absorb` which now recomputes the slot itself, so nothing else in that method needs the slot directly.

- [ ] **Step 4: Compile**

Run: `./gradlew compileJava`
Expected: FAILS — `MartialSoulEvolution.tryEvolve` doesn't handle two souls yet. Fixed in Task 3.

---

### Task 3: Slot-aware evolution

**Files:**
- Modify: `src/main/java/com/zelf115/soulland/cultivation/MartialSoulEvolution.java`

**Interfaces:**
- Consumes: `SoulSlot`, `CultivationData.getSecondaryMartialSoul()/setSecondaryMartialSoul()`, `CultivationData.getRingCount(SoulSlot)`.
- Produces: `MartialSoulEvolution.tryEvolve`, `evolveFromHerb`, `tryEvolvePolycoria` unchanged signatures (still take `player, data`), now evolve whichever slot matches.

- [ ] **Step 1: Rewrite the file**

```java
package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.spirit.Affinity;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class MartialSoulEvolution {
    private MartialSoulEvolution() {
    }

    public static void tryEvolve(final Player player, final CultivationData data) {
        tryEvolveSlot(player, data, SoulSlot.PRIMARY);
        tryEvolveSlot(player, data, SoulSlot.SECONDARY);
    }

    private static void tryEvolveSlot(final Player player, final CultivationData data, final SoulSlot slot) {
        final MartialSoul soul = soulIn(data, slot);
        if (soul == null || soul.isEvolution()) return;

        final List<AbsorbedRing> rings = ringsIn(data, slot);
        final MartialSoul target = switch (soul) {
            case BLUE_SILVER_GRASS -> data.getLevel() >= 40 ? MartialSoul.BLUE_SILVER_EMPEROR : null;
            case BLUE_LIGHTNING_DRAGON -> data.getLevel() >= 20 ? MartialSoul.BLUE_LIGHTNING_TYRANT_DRAGON : null;
            case DEMON_SOUL_GREAT_WHITE_SHARK -> hasRing(rings, "abyss", "demon", "dragon") ? MartialSoul.ABYSS_DEMON_DRAGON_SHARK : null;
            case ABYSS_DEMON_DRAGON_SHARK -> data.getLevel() >= 90 ? MartialSoul.ABYSS_ICE_DEMON_DRAGON : null;
            case ABYSS_ICE_DEMON_DRAGON -> data.getLevel() >= 100 ? MartialSoul.SKY_BLUE_ICE_DEVOURING_DRAGON : null;
            case ICE_JADE_SCORPION -> data.getLevel() >= 50 ? MartialSoul.ICE_JADE_SCORPION_EMPEROR : null;
            case PHOENIX -> hasNineOtherPhoenixRings(rings) ? MartialSoul.TEN_HEADED_FIRE_PHOENIX : null;
            case GOLDEN_DRAGON -> data.getLevel() >= 50 && hasRing(rings, "dragon") ? MartialSoul.GOLDEN_DRAGON_KING : null;
            case SILVER_DRAGON -> data.getLevel() >= 50 && hasRing(rings, "dragon") ? MartialSoul.SILVER_DRAGON_KING : null;
            case HOLY_ANGEL -> data.getLevel() >= 70 ? MartialSoul.SERAPHIM : null;
            default -> null;
        };
        if (target != null) evolve(player, data, slot, target);
    }

    public static void evolveFromHerb(final Player player, final CultivationData data, final boolean fullMoon) {
        evolveFromHerbSlot(player, data, SoulSlot.PRIMARY, fullMoon);
        evolveFromHerbSlot(player, data, SoulSlot.SECONDARY, fullMoon);
    }

    private static void evolveFromHerbSlot(final Player player, final CultivationData data, final SoulSlot slot, final boolean fullMoon) {
        final MartialSoul soul = soulIn(data, slot);
        if (soul == MartialSoul.SEVEN_TREASURE_GLAZED_TILE_PAGODA) {
            evolve(player, data, slot, MartialSoul.NINE_TREASURE_GLAZED_TILE_PAGODA);
            data.setMartialSoulCanReachLevel100(true);
        } else if (fullMoon && soul == MartialSoul.SPIRIT_EYES && data.getLevel() >= 40) {
            evolve(player, data, slot, MartialSoul.POLYCORIA_EYES);
        }
    }

    public static void tryEvolvePolycoria(final Player player, final CultivationData data) {
        tryEvolvePolycoriaSlot(player, data, SoulSlot.PRIMARY);
        tryEvolvePolycoriaSlot(player, data, SoulSlot.SECONDARY);
    }

    private static void tryEvolvePolycoriaSlot(final Player player, final CultivationData data, final SoulSlot slot) {
        if (soulIn(data, slot) == MartialSoul.POLYCORIA_EYES && data.getLevel() >= 70) {
            evolve(player, data, slot, MartialSoul.EYES_OF_ASURA);
        }
    }

    private static MartialSoul soulIn(final CultivationData data, final SoulSlot slot) {
        return slot == SoulSlot.PRIMARY ? data.getMartialSoul() : data.getSecondaryMartialSoul();
    }

    private static List<AbsorbedRing> ringsIn(final CultivationData data, final SoulSlot slot) {
        return data.getAbsorbedRings().stream().filter(ring -> ring.slot() == slot).toList();
    }

    private static void evolve(final Player player, final CultivationData data, final SoulSlot slot, final MartialSoul target) {
        if (slot == SoulSlot.PRIMARY) {
            data.setMartialSoul(target);
        } else {
            data.setSecondaryMartialSoul(target);
        }
        com.zelf115.soulland.spirit.AffinitySystem.recomputeAffinities(data);
        player.sendSystemMessage(Component.literal("Your martial soul evolved into " + target.displayName() + "."));
    }

    private static boolean hasRing(final List<AbsorbedRing> rings, final String... parts) {
        return rings.stream().anyMatch(ring -> containsAll(ring.sourceName(), parts));
    }

    private static boolean containsAll(final String value, final String... parts) {
        final String normalized = value.toLowerCase(Locale.ROOT);
        for (final String part : parts) if (!normalized.contains(part)) return false;
        return true;
    }

    private static boolean hasNineOtherPhoenixRings(final List<AbsorbedRing> rings) {
        final Set<String> names = new HashSet<>();
        for (final AbsorbedRing ring : rings) {
            final String name = ring.sourceName().toLowerCase(Locale.ROOT);
            if (name.contains("phoenix") && !name.equals("phoenix")) names.add(name);
        }
        return names.size() >= 9;
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew compileJava`
Expected: FAILS — `AffinitySystem.recomputeAffinities` doesn't exist yet. Fixed in Task 4.

---

### Task 4: `AffinitySystem` — recompute/stack affinities, first and second pick, twin-soul roll

**Files:**
- Modify: `src/main/java/com/zelf115/soulland/spirit/AffinitySystem.java`
- Modify: `src/main/java/com/zelf115/soulland/cultivation/CultivationData.java`
- Modify: `src/main/java/com/zelf115/soulland/cultivation/CultivationManager.java`

**Interfaces:**
- Consumes: `CultivationData.getMartialSoul/getSecondaryMartialSoul/setSecondaryMartialSoul`.
- Produces: `AffinitySystem.recomputeAffinities(CultivationData)`, `AffinitySystem.chooseSecondMartialSoul(Player, MartialSoul)`, `AffinitySystem.rollTwinSoulChance(Player, CultivationData)` (called by Task 5), `CultivationManager.twinMartialSoulChancePercent(int innateStat)`.
- `CultivationData` gains `secondMartialSoulPending` (boolean) get/set.

- [ ] **Step 1: Add the pending flag to `CultivationData`**

Field next to `secondaryMartialSoul`:
```java
private boolean secondMartialSoulPending = false;
```
Getter/setter next to its getter/setter:
```java
public boolean isSecondMartialSoulPending() { return secondMartialSoulPending; }
public void setSecondMartialSoulPending(final boolean pending) { this.secondMartialSoulPending = pending; }
```
Serialize/deserialize next to the martial soul block:
```java
tag.putBoolean("secondMartialSoulPending", secondMartialSoulPending);
```
```java
secondMartialSoulPending = tag.getBoolean("secondMartialSoulPending");
```

- [ ] **Step 2: Add the chance formula to `CultivationManager`**

Constants next to the overreach ones:
```java
private static final int TWIN_SOUL_BASE_CHANCE_PERCENT = 20;
private static final int TWIN_SOUL_CHANCE_PER_INNATE_POINT = 2;
```
Method next to `overreachSuccessChance`:
```java
/**
 * The odds (0-100) of rolling a second martial soul, checked at the first pick and after each
 * of the first two breakthroughs. Only innate stats of 10 or higher get a chance at all.
 *
 * <p>Formula: {@code 20% + 2% per innate stat point above 10}.
 */
public static int twinMartialSoulChancePercent(final int innateStat) {
    if (innateStat < CultivationData.NEUTRAL_INNATE_STAT) {
        return 0;
    }
    final int aboveNeutral = innateStat - CultivationData.NEUTRAL_INNATE_STAT;
    return Math.min(100, TWIN_SOUL_BASE_CHANCE_PERCENT + TWIN_SOUL_CHANCE_PER_INNATE_POINT * aboveNeutral);
}
```

- [ ] **Step 3: Rewrite the bottom of `AffinitySystem`**

Replace `chooseMartialSoul` and everything below it with:

```java
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
    final int chancePercent = com.zelf115.soulland.cultivation.CultivationManager.twinMartialSoulChancePercent(data.getInnateStat());
    if (chancePercent <= 0 || player.getRandom().nextInt(100) >= chancePercent) {
        return;
    }
    data.setSecondMartialSoulPending(true);
    player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("soulland.cultivation.martial_soul.twin_soul_available"));
    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
            (net.minecraft.server.level.ServerPlayer) player, new com.zelf115.soulland.network.OpenMartialSoulPickerPayload());
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
```

`data.setMartialSoul(null)` needs `CultivationData.setMartialSoul` to accept null — it already does (no null-check in the existing setter).

Note: fully-qualified names above (`com.zelf115.soulland.cultivation.CultivationManager`, etc.) avoid a new import cycle warning some IDEs flag between `spirit` and `cultivation`/`network`; the existing `SoulRingAbsorption` already imports `spirit.AffinitySystem` the other way, so this is consistent with the codebase, not a new problem. Feel free to use regular imports at the top of the file instead if you prefer — behaviorally identical.

- [ ] **Step 4: Compile**

Run: `./gradlew compileJava`
Expected: FAILS — `OpenMartialSoulPickerPayload` doesn't exist yet (Task 6) and `CultivationData`'s old re-apply-on-load block (removed in Task 2 step 2) needs replacing (Task 2 already removed it; nothing calls it now, which is correct — loading calls no recompute because affinities are already persisted as their own map. Confirm `affinityMultipliers` is part of `serializeNBT`/`deserializeNBT`: it already is, unchanged, so removing the reapply-on-load block is safe — the stored multipliers are the source of truth after this change, not a derived recomputation from the soul enum.)

This step's compile failure is expected and resolved once Task 6 adds the payload. Continue to Task 5 first (order doesn't matter for compilation, both are needed before it goes green).

---

### Task 5: Breakthrough counter + chance-roll hook

**Files:**
- Modify: `src/main/java/com/zelf115/soulland/cultivation/CultivationData.java`
- Modify: `src/main/java/com/zelf115/soulland/cultivation/BreakthroughManager.java`

**Interfaces:**
- Consumes: `AffinitySystem.rollTwinSoulChance(Player, CultivationData)` (Task 4).
- Produces: `CultivationData.getSuccessfulBreakthroughCount()`.

- [ ] **Step 1: Add the counter to `CultivationData`**

Field next to `breakthroughFailures`:
```java
private int successfulBreakthroughCount = 0;
```
Getter next to `getBreakthroughFailures()`:
```java
public int getSuccessfulBreakthroughCount() { return successfulBreakthroughCount; }
```
Serialize/deserialize next to `breakthroughFailures`:
```java
tag.putInt("successfulBreakthroughCount", successfulBreakthroughCount);
```
```java
successfulBreakthroughCount = tag.getInt("successfulBreakthroughCount");
```

- [ ] **Step 2: Hook it into `advancePastBottleneck`**

In `BreakthroughManager.advancePastBottleneck`, after `data.setInBottleneck(false);`, add:

```java
final int breakthroughsSoFar = data.getSuccessfulBreakthroughCount() + 1;
data.setSuccessfulBreakthroughCount(breakthroughsSoFar);
if (breakthroughsSoFar <= TWIN_SOUL_ROLL_BREAKTHROUGH_LIMIT) {
    com.zelf115.soulland.spirit.AffinitySystem.rollTwinSoulChance(player, data);
}
```

Add the constant near the top of the class with the other constants:
```java
/** The twin-soul chance is only rolled after the first two breakthroughs. */
private static final int TWIN_SOUL_ROLL_BREAKTHROUGH_LIMIT = 2;
```

- [ ] **Step 3: Compile**

Run: `./gradlew compileJava`
Expected: still FAILS (Task 6's payload is still missing) — that's expected, continue.

---

### Task 6: New network payloads and keybind

**Files:**
- Create: `src/main/java/com/zelf115/soulland/network/ChooseMartialSoulPayload.java`
- Create: `src/main/java/com/zelf115/soulland/network/OpenMartialSoulPickerPayload.java`
- Modify: `src/main/java/com/zelf115/soulland/network/CultivationActionPayload.java`
- Modify: `src/main/java/com/zelf115/soulland/client/CultivationKeyMappings.java`
- Modify: `src/main/java/com/zelf115/soulland/client/CultivationClientEvents.java`

**Interfaces:**
- Produces: `ChooseMartialSoulPayload(int martialSoulOrdinal)`, `OpenMartialSoulPickerPayload()`, `CultivationActionPayload.OPEN_MARTIAL_SOUL_MENU = 9`, `CultivationKeyMappings.OPEN_MARTIAL_SOUL_MENU`.

- [ ] **Step 1: `ChooseMartialSoulPayload`**

```java
package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The player picked a martial soul (by enum ordinal) from the selection screen. */
public record ChooseMartialSoulPayload(int martialSoulOrdinal) implements CustomPacketPayload {
    public static final Type<ChooseMartialSoulPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "choose_martial_soul"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChooseMartialSoulPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, ChooseMartialSoulPayload::martialSoulOrdinal, ChooseMartialSoulPayload::new);

    @Override
    public Type<ChooseMartialSoulPayload> type() {
        return TYPE;
    }
}
```

- [ ] **Step 2: `OpenMartialSoulPickerPayload`**

```java
package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Tells the client to open the martial soul picker (first pick or a twin-soul bonus pick). */
public record OpenMartialSoulPickerPayload() implements CustomPacketPayload {
    public static final Type<OpenMartialSoulPickerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "open_martial_soul_picker"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenMartialSoulPickerPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenMartialSoulPickerPayload());

    @Override
    public Type<OpenMartialSoulPickerPayload> type() {
        return TYPE;
    }
}
```

- [ ] **Step 3: New action constant**

In `CultivationActionPayload`, add after `OPEN_ALCHEMY_MENU`:
```java
public static final int OPEN_MARTIAL_SOUL_MENU = 9;
```

- [ ] **Step 4: New keybind**

In `CultivationKeyMappings`, add after `OPEN_ALCHEMY_MENU`:
```java
public static final KeyMapping OPEN_MARTIAL_SOUL_MENU = new KeyMapping("key.soulland.open_martial_soul_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);
```
And register it in `registerKeyMappings`:
```java
event.register(OPEN_MARTIAL_SOUL_MENU);
```

- [ ] **Step 5: Wire the keybind client-side**

In `CultivationClientEvents.onClientTick`, add:
```java
sendOnPress(CultivationKeyMappings.OPEN_MARTIAL_SOUL_MENU, CultivationActionPayload.OPEN_MARTIAL_SOUL_MENU);
```

- [ ] **Step 6: Compile**

Run: `./gradlew compileJava`
Expected: FAILS — `CultivationNetwork` doesn't register/handle the new payloads or action, and `MartialSoulSelectScreen` doesn't exist. Fixed in Tasks 7-8.

---

### Task 7: The selection screen

**Files:**
- Create: `src/main/java/com/zelf115/soulland/client/MartialSoulSelectScreen.java`

**Interfaces:**
- Consumes: `MartialSoul.Category`, `MartialSoul.values()/category()/isEvolution()/displayName()`, `ChooseMartialSoulPayload` (Task 6).
- Produces: `MartialSoulSelectScreen.open()`.

- [ ] **Step 1: Write the screen**

```java
package com.zelf115.soulland.client;

import com.zelf115.soulland.cultivation.MartialSoul;
import com.zelf115.soulland.network.ChooseMartialSoulPayload;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Lets a player without a martial soul pick one: a category first (Tool/Beast/Body), then a
 * specific, non-evolution soul from that category.
 */
public final class MartialSoulSelectScreen extends Screen {
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int CATEGORY_TOP_OFFSET = 30;

    private MartialSoul.Category category;

    private MartialSoulSelectScreen() {
        super(Component.translatable("soulland.screen.martial_soul.title"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new MartialSoulSelectScreen());
    }

    @Override
    protected void init() {
        clearWidgets();
        if (category == null) {
            initCategoryStep();
        } else {
            initSoulStep();
        }
    }

    private void initCategoryStep() {
        int y = height / 2 - CATEGORY_TOP_OFFSET;
        for (final MartialSoul.Category candidate : MartialSoul.Category.values()) {
            addRenderableWidget(Button.builder(categoryLabel(candidate), button -> selectCategory(candidate))
                    .bounds(width / 2 - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
            y += BUTTON_HEIGHT + BUTTON_GAP;
        }
    }

    private static Component categoryLabel(final MartialSoul.Category category) {
        return Component.translatable("soulland.screen.martial_soul.category." + category.name().toLowerCase(Locale.ROOT));
    }

    private void selectCategory(final MartialSoul.Category selected) {
        this.category = selected;
        init();
    }

    private void initSoulStep() {
        final List<MartialSoul> souls = soulsIn(category);
        int y = height / 2 - (souls.size() * (BUTTON_HEIGHT + BUTTON_GAP)) / 2;
        for (final MartialSoul soul : souls) {
            addRenderableWidget(Button.builder(Component.literal(soul.displayName()), button -> choose(soul))
                    .bounds(width / 2 - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
            y += BUTTON_HEIGHT + BUTTON_GAP;
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> back())
                .bounds(width / 2 - BUTTON_WIDTH / 2, y + BUTTON_GAP, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    private void back() {
        this.category = null;
        init();
    }

    private static List<MartialSoul> soulsIn(final MartialSoul.Category category) {
        return Arrays.stream(MartialSoul.values())
                .filter(soul -> soul.category() == category && !soul.isEvolution())
                .toList();
    }

    private void choose(final MartialSoul soul) {
        PacketDistributor.sendToServer(new ChooseMartialSoulPayload(soul.ordinal()));
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew compileJava`
Expected: FAILS — `CultivationNetwork` still doesn't reference this screen or register the new payloads/action. Fixed in Task 8.

---

### Task 8: Wire it all into `CultivationNetwork`

**Files:**
- Modify: `src/main/java/com/zelf115/soulland/network/CultivationNetwork.java`

**Interfaces:**
- Consumes: everything from Tasks 4, 6, 7.

- [ ] **Step 1: Register the payloads**

In `register`, add two more calls (order among the existing four doesn't matter):
```java
.playToServer(ChooseMartialSoulPayload.TYPE, ChooseMartialSoulPayload.STREAM_CODEC, CultivationNetwork::handleChooseMartialSoul)
.playToClient(OpenMartialSoulPickerPayload.TYPE, OpenMartialSoulPickerPayload.STREAM_CODEC, CultivationNetwork::handleOpenMartialSoulPicker)
```

- [ ] **Step 2: Add the action case**

In `handleAction`'s `switch`, add:
```java
case CultivationActionPayload.OPEN_MARTIAL_SOUL_MENU -> openMartialSoulMenu(player);
```

- [ ] **Step 3: Add the handler methods**

```java
private static void handleOpenMartialSoulPicker(final OpenMartialSoulPickerPayload payload, final IPayloadContext context) {
    // Resolved inside the lambda so the dedicated server never loads the client screen class.
    context.enqueueWork(com.zelf115.soulland.client.MartialSoulSelectScreen::open);
}

private static void openMartialSoulMenu(final ServerPlayer player) {
    final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
    if (data.getMartialSoul() != null && !data.isSecondMartialSoulPending()) {
        player.sendSystemMessage(Component.translatable("soulland.cultivation.martial_soul.already_chosen"));
        return;
    }
    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new OpenMartialSoulPickerPayload());
}

private static void handleChooseMartialSoul(final ChooseMartialSoulPayload payload, final IPayloadContext context) {
    context.enqueueWork(() -> {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        final MartialSoul[] souls = MartialSoul.values();
        if (payload.martialSoulOrdinal() < 0 || payload.martialSoulOrdinal() >= souls.length) {
            return;
        }
        final MartialSoul chosen = souls[payload.martialSoulOrdinal()];
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        if (data.getMartialSoul() == null) {
            com.zelf115.soulland.spirit.AffinitySystem.chooseMartialSoul(player, chosen);
        } else if (data.isSecondMartialSoulPending()) {
            com.zelf115.soulland.spirit.AffinitySystem.chooseSecondMartialSoul(player, chosen);
        }
    });
}
```

- [ ] **Step 4: Compile**

Run: `./gradlew compileJava`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zelf115/soulland/cultivation/SoulSlot.java src/main/java/com/zelf115/soulland/cultivation/AbsorbedRing.java src/main/java/com/zelf115/soulland/cultivation/MartialSoul.java src/main/java/com/zelf115/soulland/cultivation/CultivationData.java src/main/java/com/zelf115/soulland/cultivation/SoulRingAbsorption.java src/main/java/com/zelf115/soulland/cultivation/MartialSoulEvolution.java src/main/java/com/zelf115/soulland/cultivation/CultivationManager.java src/main/java/com/zelf115/soulland/cultivation/BreakthroughManager.java src/main/java/com/zelf115/soulland/spirit/AffinitySystem.java src/main/java/com/zelf115/soulland/network/ChooseMartialSoulPayload.java src/main/java/com/zelf115/soulland/network/OpenMartialSoulPickerPayload.java src/main/java/com/zelf115/soulland/network/CultivationActionPayload.java src/main/java/com/zelf115/soulland/network/CultivationNetwork.java src/main/java/com/zelf115/soulland/client/CultivationKeyMappings.java src/main/java/com/zelf115/soulland/client/CultivationClientEvents.java src/main/java/com/zelf115/soulland/client/MartialSoulSelectScreen.java
git commit -m "feat: martial soul selection screen and twin-soul roll"
```

---

### Task 9: HUD ring display by soul (extend `RingDisplayMode`)

**Files:**
- Modify: `src/main/java/com/zelf115/soulland/cultivation/RingDisplayMode.java`
- Modify: `src/main/java/com/zelf115/soulland/network/CultivationNetwork.java`
- Modify: `src/main/java/com/zelf115/soulland/events/CultivationEvents.java`
- Modify: `src/main/resources/assets/soulland/lang/en_us.json`

**Interfaces:**
- Consumes: `SoulSlot`, `CultivationData.getSecondaryMartialSoul()`.
- Produces: `RingDisplayMode.PRIMARY`/`SECONDARY` (replacing `CURRENT_MARTIAL_SOUL`).

- [ ] **Step 1: Rewrite `RingDisplayMode`**

```java
package com.zelf115.soulland.cultivation;

/** How a player wants their absorbed soul rings shown: hidden, one soul's track, or both. */
public enum RingDisplayMode {
    NONE,
    PRIMARY,
    SECONDARY,
    ALL;

    private static final RingDisplayMode[] VALUES = values();

    public static RingDisplayMode byOrdinal(final int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) {
            return NONE;
        }
        return VALUES[ordinal];
    }

    /** The mode after this one, skipping {@link #SECONDARY} and {@link #ALL} without a second soul. */
    public RingDisplayMode next(final boolean hasSecondMartialSoul) {
        final RingDisplayMode candidate = VALUES[(ordinal() + 1) % VALUES.length];
        if (!hasSecondMartialSoul && (candidate == SECONDARY || candidate == ALL)) {
            return NONE;
        }
        return candidate;
    }

    public String translationKey() {
        return "soulland.soul_ring.display." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
```

- [ ] **Step 2: Fix the cycle handler**

In `CultivationNetwork.cycleRingDisplay`, replace:
```java
final RingDisplayMode next = data.getRingDisplayMode().next(false);
```
with:
```java
final RingDisplayMode next = data.getRingDisplayMode().next(data.getSecondaryMartialSoul() != null);
```
and delete the now-stale comment above it ("The 'all rings' mode belongs to players with a second martial soul, which isn't implemented yet...").

- [ ] **Step 3: Filter the HUD sync by display mode**

In `CultivationEvents.syncHud`, replace the ring/currentRing computation:
```java
final List<AbsorbedRing> rings = data.getAbsorbedRings();
final AbsorbedRing currentRing = rings.isEmpty() ? null : rings.get(rings.size() - 1);
final List<Integer> ringTiers = rings.stream().map(AbsorbedRing::tier).toList();
```
with:
```java
final List<AbsorbedRing> rings = visibleRings(data);
final AbsorbedRing currentRing = rings.isEmpty() ? null : rings.get(rings.size() - 1);
final List<Integer> ringTiers = rings.stream().map(AbsorbedRing::tier).toList();
```
and add this helper method to the class:
```java
/** The rings the HUD shows, per the player's {@link RingDisplayMode} choice. */
private static List<AbsorbedRing> visibleRings(final CultivationData data) {
    return switch (data.getRingDisplayMode()) {
        case NONE -> List.of();
        case PRIMARY -> ringsOfSlot(data, com.zelf115.soulland.cultivation.SoulSlot.PRIMARY);
        case SECONDARY -> ringsOfSlot(data, com.zelf115.soulland.cultivation.SoulSlot.SECONDARY);
        case ALL -> data.getAbsorbedRings();
    };
}

private static List<AbsorbedRing> ringsOfSlot(final CultivationData data, final com.zelf115.soulland.cultivation.SoulSlot slot) {
    return data.getAbsorbedRings().stream().filter(ring -> ring.slot() == slot).toList();
}
```

- [ ] **Step 4: Update the lang keys**

In `en_us.json`, replace:
```json
"soulland.soul_ring.display.current_martial_soul": "Current martial soul",
```
with:
```json
"soulland.soul_ring.display.primary": "Primary soul's rings",
"soulland.soul_ring.display.secondary": "Secondary soul's rings",
```
(keep `.none` and `.all` as they are).

- [ ] **Step 5: Compile**

Run: `./gradlew compileJava`
Expected: PASS.

- [ ] **Step 6: Manual check**

Run the client (`./gradlew runClient` or however this project is normally launched), spawn a player, absorb a couple of soul rings, and press R (`CYCLE_RING_DISPLAY`) repeatedly. Confirm the HUD ring pips show none, then the rings, then none again (no second soul yet, so `SECONDARY`/`ALL` should be skipped — the chat message should only ever say "Hidden" or "Primary soul's rings").

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/zelf115/soulland/cultivation/RingDisplayMode.java src/main/java/com/zelf115/soulland/network/CultivationNetwork.java src/main/java/com/zelf115/soulland/events/CultivationEvents.java src/main/resources/assets/soulland/lang/en_us.json
git commit -m "feat: HUD ring display follows the selected martial soul's track"
```

---

### Task 10: Generalize `MartialSoulAbility` for two souls + fix Nine Heart Begonia

**Files:**
- Modify: `src/main/java/com/zelf115/soulland/cultivation/MartialSoulAbility.java`

**Interfaces:**
- Consumes: `CultivationData.getSecondaryMartialSoul()`, `SoulLand.NINE_HEART_BEGONIA`/`SEVEN_TREASURE_GLAZED_TILE_PAGODA` (existing items, currently never granted — this task fixes that gap too).

- [ ] **Step 1: Rewrite the file**

```java
package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import java.util.Comparator;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.DeferredItem;
import net.minecraft.world.item.Item;

public final class MartialSoulAbility {
    private static final int TICKS_PER_SECOND = 20;
    private static final int TICKS_PER_MINUTE = 1200;
    private static final double ENERGY_COST_PER_SECOND = 1.0;
    private static final String TEMPORARY_BONUS_ID = "soulland_martial_soul_buff";
    private static final double NON_TOOL_ACTIVATION_BONUS_PERCENT = 0.10;
    private static final double NINE_HEART_BEGONIA_HEAL_PERCENT_PER_TEN_LEVELS = 0.10;
    private static final double PAGODA_BUFF_PERCENT_PER_TEN_LEVELS = 0.02;
    private static final int PAGODA_BUFF_DURATION_TICKS = 5 * TICKS_PER_MINUTE;
    private static final double CAST_TARGET_SEARCH_RADIUS = 15.0;

    private MartialSoulAbility() {
    }

    public static void toggle(final Player player, final CultivationData data) {
        if (data.getMartialSoul() == null) return;
        if (data.isMartialSoulActive()) {
            deactivate(player, data);
            return;
        }
        activate(player, data);
    }

    private static void deactivate(final Player player, final CultivationData data) {
        if (hasNonToolSoul(data)) {
            Stats.removeTemporaryBonus(player, TEMPORARY_BONUS_ID);
        }
        data.setMartialSoulActive(false);
    }

    private static void activate(final Player player, final CultivationData data) {
        if (data.getSpiritEnergy() < ENERGY_COST_PER_SECOND) return;

        data.setMartialSoulActive(true);
        grantToolItem(player, data.getMartialSoul());
        grantToolItem(player, data.getSecondaryMartialSoul());
        if (hasNonToolSoul(data)) {
            Stats.applyTemporaryStatPercentBonus(player, TEMPORARY_BONUS_ID, NON_TOOL_ACTIVATION_BONUS_PERCENT);
        }
    }

    private static boolean hasNonToolSoul(final CultivationData data) {
        return isNonToolCategory(data.getMartialSoul()) || isNonToolCategory(data.getSecondaryMartialSoul());
    }

    private static boolean isNonToolCategory(final MartialSoul soul) {
        return soul != null && soul.category() != MartialSoul.Category.TOOL;
    }

    private static void grantToolItem(final Player player, final MartialSoul soul) {
        final DeferredItem<Item> item = toolItemFor(soul);
        if (item != null) {
            player.getInventory().add(item.get().getDefaultInstance());
        }
    }

    private static DeferredItem<Item> toolItemFor(final MartialSoul soul) {
        if (soul == null) return null;
        return switch (soul) {
            case CLEAR_SKY_HAMMER -> SoulLand.CLEAR_SKY_HAMMER;
            case SEVEN_KILL_SWORD -> SoulLand.SEVEN_KILL_SWORD;
            case NINE_HEART_BEGONIA -> SoulLand.NINE_HEART_BEGONIA;
            case SEVEN_TREASURE_GLAZED_TILE_PAGODA -> SoulLand.SEVEN_TREASURE_GLAZED_TILE_PAGODA;
            default -> null;
        };
    }

    public static void tick(final Player player, final CultivationData data, final long gameTick) {
        if (data.isMartialSoulActive() && gameTick % TICKS_PER_SECOND == 0) {
            data.setSpiritEnergy(data.getSpiritEnergy() - ENERGY_COST_PER_SECOND);
            if (data.getSpiritEnergy() <= 0.0) data.setMartialSoulActive(false);
        }
        if (data.getMartialSoulBuffUntil() > 0 && gameTick >= data.getMartialSoulBuffUntil()) {
            Stats.removeTemporaryBonus(player, TEMPORARY_BONUS_ID);
            data.setMartialSoulBuffUntil(0L);
        }
    }

    public static void cast(final Player player, final CultivationData data) {
        if (!data.isMartialSoulActive()) return;
        castSoul(player, data, data.getMartialSoul());
        castSoul(player, data, data.getSecondaryMartialSoul());
    }

    private static void castSoul(final Player player, final CultivationData data, final MartialSoul soul) {
        if (soul == null) return;
        final int levelTens = data.getLevel() / 10;
        if (soul == MartialSoul.NINE_HEART_BEGONIA) {
            final Player target = nearestOtherPlayer(player);
            final float healAmount = (float) (target.getMaxHealth() * NINE_HEART_BEGONIA_HEAL_PERCENT_PER_TEN_LEVELS * levelTens);
            target.heal(healAmount);
            player.heal(healAmount);
        } else if (soul == MartialSoul.SEVEN_TREASURE_GLAZED_TILE_PAGODA) {
            final Player target = nearestOtherPlayer(player);
            final CultivationData targetData = target.getData(CultivationAttachment.CULTIVATION_DATA.get());
            Stats.applyTemporaryPercentBonus(target, TEMPORARY_BONUS_ID, PAGODA_BUFF_PERCENT_PER_TEN_LEVELS * levelTens);
            targetData.setMartialSoulBuffUntil(player.level().getGameTime() + PAGODA_BUFF_DURATION_TICKS);
        }
    }

    private static Player nearestOtherPlayer(final Player player) {
        return player.level().getEntitiesOfClass(Player.class,
                player.getBoundingBox().inflate(CAST_TARGET_SEARCH_RADIUS), candidate -> candidate != player
                    && player.hasLineOfSight(candidate)).stream()
            .min(Comparator.comparingDouble(player::distanceToSqr)).orElse(player);
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew compileJava`
Expected: PASS.

- [ ] **Step 3: Manual check**

In-game: give yourself Nine Heart Begonia's martial soul via the picker (or `/soulland` debug command if one exists — check `CultivationCommands.java`), reduce your own health, activate the ability (`V`), stand near another player or leave yourself as the only target, and cast (`C`). Confirm your own health increases (not just the target's) — this is the bug this task fixes.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/zelf115/soulland/cultivation/MartialSoulAbility.java
git commit -m "fix: martial soul ability grants both souls' tools and heals self+target"
```

---

### Task 11: Lang entries for the new screen and messages

**Files:**
- Modify: `src/main/resources/assets/soulland/lang/en_us.json`

- [ ] **Step 1: Add the keys**

Near the existing `key.soulland.*` entries, add:
```json
"key.soulland.open_martial_soul_menu": "Open Martial Soul Selection",
```

Near the existing `soulland.screen.overreach.*` entries, add:
```json
"soulland.screen.martial_soul.title": "Choose Your Martial Soul",
"soulland.screen.martial_soul.category.tool": "Tool",
"soulland.screen.martial_soul.category.beast": "Beast",
"soulland.screen.martial_soul.category.body": "Body",
```

Near the existing `soulland.cultivation.*` entries, add:
```json
"soulland.cultivation.martial_soul.already_chosen": "You have already chosen your martial soul.",
"soulland.cultivation.martial_soul.twin_soul_available": "Your innate talent has awakened a second martial soul! Choose it now.",
"soulland.cultivation.martial_soul.second_chosen": "Your second martial soul is %s.",
```

- [ ] **Step 2: Compile**

Run: `./gradlew compileJava`
Expected: PASS (lang files aren't compiled, this just confirms nothing else broke).

- [ ] **Step 3: Manual check**

Launch the client, press J (`OPEN_MARTIAL_SOUL_MENU`) on a fresh character. Confirm the screen shows "Choose Your Martial Soul", three category buttons with real English labels (not `soulland.screen.martial_soul.category.tool` literal text), and that picking Tool → Clear Sky Hammer closes the screen and grants the soul (check with the cultivation info command).

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/assets/soulland/lang/en_us.json
git commit -m "feat: martial soul selection lang entries"
```

## Self-review notes

- **Spec coverage:** selection flow (Tasks 6-8, 11), twin-soul chance timing (Task 4 step 3, Task 5), ring tracks/caps (Tasks 1-2), evolution (Task 3), tool ability generalization + Begonia fix (Task 10), HUD (Task 9), rebirth hook (Task 4's `clearMartialSoulForRebirth`) are all covered. Berserk buffs and the ring-skill lock stay out per the spec's explicit descope.
- **Type consistency checked:** `AbsorbedRing`'s 5th component is `SoulSlot slot` everywhere it's constructed (Task 1, Task 2 step 3); `CultivationData.getRingCount(SoulSlot)` name matches its two call sites (Task 2 step 3, Task 9 step 3); `AffinitySystem.rollTwinSoulChance(Player, CultivationData)` signature matches its Task 4 definition and Task 5 call site.
- **No placeholders:** every step above has literal code, not a description of code.
