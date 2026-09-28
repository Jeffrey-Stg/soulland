package com.zelf115.soulland.datagen;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.biome.SoulLandBiomes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Mirrors the tags of the vanilla biomes each mod biome stands in for. Without this, structures,
 * mob variants and ambience keyed off those tags would go missing from the share of the world the
 * mod's biomes occupy.
 */
public class SoulLandBiomeTagsProvider extends TagsProvider<Biome> {

    public SoulLandBiomeTagsProvider(
            final PackOutput output,
            final CompletableFuture<HolderLookup.Provider> registries,
            final ExistingFileHelper existingFileHelper) {
        super(output, Registries.BIOME, registries, SoulLand.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(final HolderLookup.Provider registries) {
        addOverworldTags();
        addExtremeNorthTags();
        addIceboundForestTags();
        addSunsetForestTags();
        addStarDouForestTags();
        addIslandTags();
        addSoulOceanTags();
        addSoulHellTags();
        addSoulEndTags();
    }

    private void addOverworldTags() {
        this.tag(BiomeTags.IS_OVERWORLD).add(
                SoulLandBiomes.EXTREME_NORTH,
                SoulLandBiomes.ICEBOUND_FOREST,
                SoulLandBiomes.SUNSET_FOREST,
                SoulLandBiomes.STAR_DOU_FOREST,
                SoulLandBiomes.ISLAND,
                SoulLandBiomes.SOUL_OCEAN);
        this.tag(BiomeTags.HAS_TRIAL_CHAMBERS).add(
                SoulLandBiomes.EXTREME_NORTH,
                SoulLandBiomes.ICEBOUND_FOREST,
                SoulLandBiomes.SUNSET_FOREST,
                SoulLandBiomes.STAR_DOU_FOREST,
                SoulLandBiomes.ISLAND,
                SoulLandBiomes.SOUL_OCEAN);
        this.tag(BiomeTags.STRONGHOLD_BIASED_TO).add(
                SoulLandBiomes.EXTREME_NORTH,
                SoulLandBiomes.ICEBOUND_FOREST,
                SoulLandBiomes.SUNSET_FOREST,
                SoulLandBiomes.STAR_DOU_FOREST,
                SoulLandBiomes.ISLAND);
    }

    /** Stands in for snowy plains, ice spikes, snowy slopes and frozen peaks. */
    private void addExtremeNorthTags() {
        this.tag(BiomeTags.IS_MOUNTAIN).add(SoulLandBiomes.EXTREME_NORTH);
        this.tag(Tags.Biomes.IS_SNOWY).add(SoulLandBiomes.EXTREME_NORTH);
        this.tag(BiomeTags.INCREASED_FIRE_BURNOUT).add(SoulLandBiomes.EXTREME_NORTH);
        this.tag(BiomeTags.SPAWNS_COLD_VARIANT_FROGS).add(SoulLandBiomes.EXTREME_NORTH);
        this.tag(BiomeTags.SPAWNS_SNOW_FOXES).add(SoulLandBiomes.EXTREME_NORTH);
        this.tag(BiomeTags.SPAWNS_WHITE_RABBITS).add(SoulLandBiomes.EXTREME_NORTH);
        this.tag(BiomeTags.HAS_IGLOO).add(SoulLandBiomes.EXTREME_NORTH);
        this.tag(BiomeTags.HAS_MINESHAFT).add(SoulLandBiomes.EXTREME_NORTH);
        this.tag(BiomeTags.HAS_PILLAGER_OUTPOST).add(SoulLandBiomes.EXTREME_NORTH);
        this.tag(BiomeTags.HAS_RUINED_PORTAL_STANDARD).add(SoulLandBiomes.EXTREME_NORTH);
        this.tag(BiomeTags.HAS_VILLAGE_SNOWY).add(SoulLandBiomes.EXTREME_NORTH);
    }

    /** Stands in for snowy taiga and grove. */
    private void addIceboundForestTags() {
        this.tag(BiomeTags.IS_TAIGA).add(SoulLandBiomes.ICEBOUND_FOREST);
        this.tag(BiomeTags.IS_FOREST).add(SoulLandBiomes.ICEBOUND_FOREST);
        this.tag(Tags.Biomes.IS_SNOWY).add(SoulLandBiomes.ICEBOUND_FOREST);
        this.tag(BiomeTags.SPAWNS_COLD_VARIANT_FROGS).add(SoulLandBiomes.ICEBOUND_FOREST);
        this.tag(BiomeTags.SPAWNS_SNOW_FOXES).add(SoulLandBiomes.ICEBOUND_FOREST);
        this.tag(BiomeTags.SPAWNS_WHITE_RABBITS).add(SoulLandBiomes.ICEBOUND_FOREST);
        this.tag(BiomeTags.HAS_IGLOO).add(SoulLandBiomes.ICEBOUND_FOREST);
        this.tag(BiomeTags.HAS_PILLAGER_OUTPOST).add(SoulLandBiomes.ICEBOUND_FOREST);
        this.tag(BiomeTags.HAS_TRAIL_RUINS).add(SoulLandBiomes.ICEBOUND_FOREST);
    }

    /** Stands in for birch forest and old growth birch forest. */
    private void addSunsetForestTags() {
        this.tag(BiomeTags.IS_FOREST).add(SoulLandBiomes.SUNSET_FOREST);
        this.tag(BiomeTags.HAS_TRAIL_RUINS).add(SoulLandBiomes.SUNSET_FOREST);
    }

    /** Stands in for forest. */
    private void addStarDouForestTags() {
        this.tag(BiomeTags.IS_FOREST).add(SoulLandBiomes.STAR_DOU_FOREST);
    }

    /** Stands in for mushroom fields. */
    private void addIslandTags() {
        this.tag(BiomeTags.INCREASED_FIRE_BURNOUT).add(SoulLandBiomes.ISLAND);
        this.tag(BiomeTags.HAS_MINESHAFT).add(SoulLandBiomes.ISLAND);
        this.tag(BiomeTags.HAS_RUINED_PORTAL_STANDARD).add(SoulLandBiomes.ISLAND);
    }

    /** Stands in for deep ocean and deep cold ocean. */
    private void addSoulOceanTags() {
        this.tag(BiomeTags.IS_DEEP_OCEAN).add(SoulLandBiomes.SOUL_OCEAN);
        this.tag(BiomeTags.HAS_OCEAN_RUIN_COLD).add(SoulLandBiomes.SOUL_OCEAN);
    }

    private void addSoulHellTags() {
        this.tag(BiomeTags.IS_NETHER).add(SoulLandBiomes.SOUL_HELL);
        this.tag(BiomeTags.HAS_BASTION_REMNANT).add(SoulLandBiomes.SOUL_HELL);
        this.tag(BiomeTags.HAS_NETHER_FOSSIL).add(SoulLandBiomes.SOUL_HELL);
    }

    private void addSoulEndTags() {
        this.tag(BiomeTags.IS_END).add(SoulLandBiomes.SOUL_END);
        this.tag(BiomeTags.HAS_END_CITY).add(SoulLandBiomes.SOUL_END);
    }
}
