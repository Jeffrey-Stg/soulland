# Alchemy: Pill Furnace + Curios Design

Status: Approved
Date: 2026-09-04
Source: GitHub issue #16 (Alchemy)

## Problem

Issue #16's alchemy system is half-built. What exists today is wrong in
ways that block the rest of the feature:

- Pills are crafted through ordinary 3x3 crafting-table recipes
  (`mysterious_water_pill.json`, `spirit_ascension_pill.json`,
  `qi_gathering_pill_tier_1..5.json`). A crafting grid has 9 slots, so
  it cannot express the issue's costs (e.g. 64 Common Spirit Herb).
  Every existing pill recipe uses ~8-9 herbs instead.
- `PillFurnaceBlock` extends vanilla `FurnaceBlock` (ordinary
  smelting). Its `bonusPillCount()`/`processingMultiplier()` methods
  are never called from anywhere — dead code.
- `PillFurnaceItem` (holds the `Tier` enum, meant to be the real
  registered item) is never actually registered — `SoulLand.java`
  registers a plain `BlockItem` instead via
  `registerSimpleBlockItem`, so the class is dead code too.
- There is no menu, no screen, no hotkey, no Curios integration, and
  no level-gating on Qi Gathering Pill usage (it applies its effect
  unconditionally regardless of player level).
- `pill_furnace.json`'s craft recipe uses `minecraft:furnace` +
  `minecraft:iron_ingot`, not the issue's ring-of-iron + coal-center
  shape.

## Decisions

- Pill furnace is an **item only** — no placeable block. Right-click
  always opens the alchemy menu (no Curios required for that).
  Confirmed by user.
- Base furnace recipe ring material is `minecraft:iron_ingot`
  (confirmed by user, overriding the issue text's literal "iron bar"
  wording).
- Curios is a **soft/optional dependency**. Without it installed, the
  mod loads and the furnace still works via right-click. With it
  installed, the furnace item can additionally be worn in a Curios
  slot.
- The hotkey (as opposed to right-click) only opens the menu when a
  furnace is equipped in a Curios slot. Confirmed by user.
- Enchanted/Nether/Star furnaces stay dungeon-loot-only, uncraftable.
  Divine stays craftable by combining all three (current 3x3 shape,
  `divine_pill_furnace.json`, is kept as-is). Confirmed by user.

## Architecture

### 1. Item consolidation

Delete:
- `block/PillFurnaceBlock.java`
- The 5 `DeferredBlock` registrations and `registerPillFurnaceBlock`
  helper in `SoulLand.java`
- `assets/soulland/blockstates/*_pill_furnace.json` (5 files)
- `assets/soulland/models/block/*_pill_furnace.json` (5 files)

`PillFurnaceItem` becomes the actually-registered item for all 5
tiers (replacing today's plain `BlockItem`). Its `Tier` enum keeps
`bonusPillCount()` (0/1/2/3/4 for REGULAR/ENCHANTED/NETHER/STAR/DIVINE)
and gains `costMultiplier()` (1.0 for all tiers except DIVINE's 0.8).
`processingMultiplier()` is deleted — it encoded a smelting-speed
concept that has no home once the block is gone and isn't in the
issue.

`PillFurnaceItem.use(...)` opens `AlchemyMenu` server-side via
`player.openMenu(...)`, passing the item's `Tier` so the menu knows
which bonus/discount to apply.

Item model JSONs for the 5 tiers already exist under
`models/item/*_pill_furnace.json` and stay; they just stop being
`registerSimpleBlockItem` wrappers and become the models for the real
`PillFurnaceItem` registrations.

### 2. Curios soft dependency

- `build.gradle`: add the Curios maven repo
  (`https://maven.theillusivec4.top/`) and a `compileOnly` dependency
  on `curios-neoforge` for the target Minecraft/NeoForge version.
- `neoforge.mods.toml`: add a `[[dependencies.${mod_id}]]` block for
  `modId="curios"`, `type="optional"`.
- New class `client`/`compat` package: `CuriosCompat`. This is the
  **only** class allowed to reference Curios API types. Every call
  site into it is guarded by
  `ModList.get().isLoaded("curios")` first, so the JVM never verifies
  `CuriosCompat`'s bytecode when Curios isn't present.
- `CuriosCompat` exposes two intention-revealing methods:
  `findEquippedPillFurnace(Player): Optional<ItemStack>` (used by the
  hotkey) and `registerCurio(RegisterCapabilitiesEvent)` (registers
  `PillFurnaceItem` as a valid curio, called only when Curios is
  loaded).

### 3. Recipe type `soulland:alchemy_pill`

New `AlchemyPillRecipe implements Recipe<AlchemyPillRecipe.Input>` +
`AlchemyPillRecipe.Serializer` + `RecipeType` registration, mirroring
how other custom recipe types are registered in this codebase (see
existing `DeferredRegister<RecipeSerializer<?>>` usage, if any —
otherwise follow the vanilla `RecipeSerializer` codec pattern used by
NeoForge 1.21.1).

JSON schema:

```json
{
  "type": "soulland:alchemy_pill",
  "ingredients": [
    { "item": "soulland:common_spirit_herb", "count": 64 }
  ],
  "result": { "id": "soulland:mysterious_water_pill", "count": 1 },
  "min_level": 1,
  "max_level": 20
}
```

`min_level`/`max_level` are optional (only Qi Gathering tiers use
them). This recipe type is **not** a `CraftingRecipe` — it has no
grid shape, no `RecipeBookCategory` entry, and is never picked up by
the crafting table. It exists purely to be queried by `AlchemyMenu`
from `RecipeManager.getAllRecipesFor(ALCHEMY_PILL_TYPE)`.

Replace, at the issue's actual quantities:
- `mysterious_water_pill.json` → 64 Common Spirit Herb
- `spirit_ascension_pill.json` → 64 Common Spirit Herb + 5 Ice
  Crystal Fruit + 5 Scarlet Flame Fruit
- `qi_gathering_pill_tier_1.json` → 64 Common Spirit Herb, levels 1-20
- `qi_gathering_pill_tier_2.json` → 64 Common Spirit Herb + 1 Tier 1
  pill, levels 21-40
- `qi_gathering_pill_tier_3.json` → 64 Common Spirit Herb + 1 Tier 2
  pill, levels 41-60
- `qi_gathering_pill_tier_4.json` → 64 Common Spirit Herb + 1 Tier 3
  pill, levels 61-80
- `qi_gathering_pill_tier_5.json` → 64 Common Spirit Herb + 1 Tier 4
  pill, levels 81-100

`pill_furnace.json` (craft the base item — ring of `iron_ingot`, coal
center) and `divine_pill_furnace.json` (combine 3 tier furnaces) are
unaffected — they stay ordinary `minecraft:crafting_shaped` recipes.

### 4. Menu / Screen

- `AlchemyMenu extends AbstractContainerMenu` — no real inventory
  slots (nothing to place). Constructed with the opening `Tier`.
  Holds the level's/`RecipeManager`'s current `AlchemyPillRecipe`
  list for display.
- Server-bound craft action: reuses `AbstractContainerMenu`'s
  `clickMenuButton(Player, int)` with the button id encoding which
  recipe was clicked (index into the menu's own recipe list, which is
  identical client/server since both read the same `RecipeManager`).
  On click, the server:
  1. Re-validates the player still has the ingredients.
  2. For Qi Gathering recipes, re-validates the player's cultivation
     level is within `[min_level, max_level]`.
  3. Removes the ingredients.
  4. Gives `result.count + tier.bonusPillCount()` pills (Divine also
     applies `costMultiplier()` to each ingredient count, rounded
     down, before step 1-3).
- `AlchemyScreen extends AbstractContainerScreen<AlchemyMenu>` — a
  plain scrolling list, one row per recipe: icon, name, ingredient
  costs, a craft button. No new textures beyond a background panel
  (reuse vanilla widget sprites where possible, matching this
  project's existing light-touch approach to custom screens).

### 5. Qi Gathering level gate on use

`AlchemyItem.applyEffect`'s `QI_GATHERING` branch currently calls
`Stats.addSpirit(player, qiLevel * 10.0)` unconditionally. Add the
issue's per-tier level band check (tier N valid for player levels
`(N-1)*20 + 1` through `N*20`); outside the band, the pill is not
consumed for effect (no-op, matching `finishUsingItem`'s existing
early-return style for invalid states elsewhere in this codebase).

### 6. Hotkey

New `KeyMapping OPEN_ALCHEMY_MENU` added to the existing
`CultivationKeyMappings`/`key.categories.soulland` category (already
the project's dedicated category, not `CATEGORY_MISC`). Client handler
calls `CuriosCompat.findEquippedPillFurnace(player)`; if present,
opens `AlchemyMenu` for that tier; if Curios isn't loaded or nothing
is equipped, no-op.

## Testing

This repository has no JUnit harness (`src/test` doesn't exist
anywhere in the tree) — verification for this feature follows the
project's existing convention: `./gradlew build` for compilation, plus
a manual dev-run check (`/give` the furnace item, right-click to open
the menu, craft a pill, verify bonus-pill counts per tier and the
Divine cost discount, verify Qi Gathering pills silently no-op outside
their level band).

## Out of scope

- Making Enchanted/Nether/Star furnaces craftable (explicitly rejected
  — dungeon-loot-only, per user answer).
- A placeable furnace block (explicitly rejected — item-only, per user
  answer).
