# Martial Soul Selection & Twin-Soul System

Implements the remaining half of issue #3 (Martial Soul). The data model
(`MartialSoul` enum, affinity multipliers, evolution chains) already exists;
this spec covers picking a soul, the twin-soul roll, and the ability/HUD
consequences of having two.

## Scope

In scope:
- A selection screen (category → soul), reachable by keybind, gated to
  "no martial soul yet."
- Rolling the innate stat (already implemented in the unused
  `AffinitySystem.chooseMartialSoul` — this wires it up).
- The twin-soul chance roll (`20 + 2*(innate-10)`%), checked at exactly three
  points: on first pick, and after the 1st and 2nd successful breakthroughs.
- Twin souls stack affinities multiplicatively and get independent soul-ring
  tracks (9 each, so 18 total), independent evolution, and independent tool
  abilities.
- A HUD keybind to cycle which soul's ring track is displayed.
- Generalizing `MartialSoulAbility` so both slots can trigger tool behavior,
  and fixing Nine Heart Begonia's cast to heal self *and* target.
- Seven Treasure Glazed Tile Pagoda (and its evolution) getting a fixed ring
  cap (7, or 9 once evolved) instead of the level-based cap.

Out of scope (flagged, not built):
- Rebirth itself. No trigger exists anywhere in the codebase today (only a
  `rebirthCount` stat consumed by formulas). This spec adds one hook,
  `CultivationManager.clearMartialSoulForRebirth`, for a future rebirth
  feature to call; it does not build rebirth.
- Nine Heart Begonia's "cannot use any soul ring skill" restriction. There is
  no active soul-ring-skill system in the codebase — rings only ever grant
  passive `StatBonus`. Nothing exists to gate.
- Seven Treasure Pagoda's "ring skills become Berserk/Greater Berserk/
  Greatest Berserk stat buffs" for ring indices 4, 8 and 9. Berserk is not an
  existing mechanic (no damage/defense trade-off, duration, or trigger is
  defined anywhere). Indices 1-3 and 5-7 use the existing Damage/Defense/Heal
  categories, since those already exist as `StatBonus` fields; Berserk is
  left as a follow-up needing its own small design pass.

## Data model

`CultivationData` gains:
- `secondaryMartialSoul` (nullable `MartialSoul`)
- `secondMartialSoulPending` (boolean) — set when a chance roll succeeds;
  cleared when the second pick lands. Gates the second-pick network handler
  so a replayed packet can't grant a soul that wasn't rolled.
- `successfulBreakthroughCount` (int) — incremented once, in
  `BreakthroughManager.advancePastBottleneck`, the single point both regular
  and special breakthroughs already funnel through.
- `activeSoulSlot` (`SoulSlot`, default `PRIMARY`) — which soul's ring track
  the HUD displays; cycled by keybind.

`AbsorbedRing` gains a `slot` field (`SoulSlot.PRIMARY`/`SECONDARY`),
defaulting to `PRIMARY` when absent from NBT (old saves).

## Selection flow

1. Keybind opens `MartialSoulSelectScreen` only when the player has no
   martial soul yet (first pick) or `secondMartialSoulPending` is true
   (bonus pick). Otherwise a chat message says the choice is already made.
2. Screen: three category buttons (Tool/Beast/Body), then a scrolling list
   of that category's souls with `isEvolution() == false`. Selecting one
   sends `ChooseMartialSoulPayload(category selection)` to the server.
3. Server validates the same "no soul yet" / "pending" gate again (never
   trust the client), then:
   - First pick: `AffinitySystem.chooseMartialSoul` (sets soul + affinities
     + rolls innate stat 1-20, as it already does), then rolls the twin-soul
     chance immediately.
   - Second pick: new `AffinitySystem.chooseSecondMartialSoul` — sets
     `secondaryMartialSoul`, multiplies its affinities into the existing map
     (stacking, not replacing), clears `secondMartialSoulPending`. No second
     innate-stat roll.

## Twin-soul chance

Rolled at exactly three moments, each independent, stopping once a second
soul exists:
- Immediately after the first pick (issue text: "at level 1").
- Right after `successfulBreakthroughCount` becomes 1.
- Right after `successfulBreakthroughCount` becomes 2.

Chance = `20 + 2 * (innateStat - 10)`, clamped to `[0, 100]`. On success:
`secondMartialSoulPending = true`, chat message, client auto-opens the
picker for the bonus soul (any category, no exclusion beyond
`isEvolution()`).

## Soul rings (9 → 18)

No new UI for choosing a track. Absorption auto-fills: a ring goes to
`PRIMARY` until that track reaches its own cap, then `SECONDARY` up to its
own cap. `SoulRingCapacity`/`SoulRingAbsorption` gate per-track counts
(each independently level-gated the way the single track is today) instead
of one flat total. Seven Treasure Pagoda/Nine Treasure override their own
track's cap to 7/9 regardless of level.

## Evolution

`MartialSoulEvolution` becomes slot-aware: it runs once for
`(primary soul, primary-track rings)` and once for
`(secondary soul, secondary-track rings)`, evolving each independently.
`evolveFromHerb` and `tryEvolvePolycoria` check both slots the same way.

## Tool ability

`MartialSoulAbility`'s hardcoded `if (soul == X)` chains become a per-soul
lookup invoked once per occupied TOOL-category slot, so two independent
tool souls (primary and secondary) both function if that happens. Nine
Heart Begonia's cast heals self and the targeted entity (today it only
heals the target).

## HUD

`HudSyncPayload`'s ring list/tiers reflect only `activeSoulSlot`'s track
(shape unchanged, just filtered server-side before sending). A new keybind
toggles `activeSoulSlot`; toggling when there's no secondary soul is a
no-op with a chat message.

## Testing

No test harness exists in this codebase (mod project, no unit tests found
anywhere). Verification is `gradlew compileJava` plus manual in-game checks
called out in the implementation plan.
