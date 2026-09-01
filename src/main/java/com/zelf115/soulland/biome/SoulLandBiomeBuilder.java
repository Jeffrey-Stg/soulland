package com.zelf115.soulland.biome;

import com.zelf115.soulland.feature.SoulLandPlacedFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.EndPlacements;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.NetherPlacements;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Datapack source of truth for the biomes added by this mod. The {@code data} run configuration
 * writes the registered entries out to {@code src/generated/resources}.
 */
public final class SoulLandBiomeBuilder {

    private static final int OVERWORLD_FOG_COLOR = 12638463;
    private static final int NORMAL_WATER_COLOR = 4159204;
    private static final int NORMAL_WATER_FOG_COLOR = 329011;
    private static final int FROZEN_WATER_COLOR = 3750089;
    private static final int WARM_WATER_COLOR = 4445678;
    private static final int WARM_WATER_FOG_COLOR = 270131;
    private static final int SOUL_WATER_COLOR = 1787717;
    private static final int SUNSET_FOG_COLOR = 14459498;
    private static final int SUNSET_SKY_COLOR = 15245418;
    private static final int SUNSET_FOLIAGE_COLOR = 14251311;
    private static final int SUNSET_GRASS_COLOR = 13214542;
    private static final int STAR_DOU_FOLIAGE_COLOR = 9752675;
    private static final int STAR_DOU_GRASS_COLOR = 11065977;
    private static final int SOUL_HELL_FOG_COLOR = 2902098;
    private static final int SOUL_END_FOG_COLOR = 2759229;
    private static final int SOUL_END_SKY_COLOR = 0;

    private static final float SKY_COLOR_TEMPERATURE_SCALE = 3.0F;
    private static final float SKY_HUE_BASE = 0.62222224F;
    private static final float SKY_HUE_SPREAD = 0.05F;
    private static final float SKY_SATURATION_BASE = 0.5F;
    private static final float SKY_SATURATION_SPREAD = 0.1F;

    private static final float EXTREME_NORTH_TEMPERATURE = -0.7F;
    private static final float ICEBOUND_FOREST_TEMPERATURE = -0.4F;
    private static final float SUNSET_FOREST_TEMPERATURE = 0.7F;
    private static final float STAR_DOU_FOREST_TEMPERATURE = 0.6F;
    private static final float ISLAND_TEMPERATURE = 0.95F;
    private static final float SOUL_OCEAN_TEMPERATURE = 0.5F;
    private static final float SOUL_HELL_TEMPERATURE = 2.0F;
    private static final float SOUL_END_TEMPERATURE = 0.5F;

    private static final float WHITE_ASH_PROBABILITY = 0.0625F;
    private static final double SOUL_SAND_VALLEY_ADDITIONS_CHANCE = 0.0111D;

    private SoulLandBiomeBuilder() {
    }

    public static void bootstrap(final BootstrapContext<Biome> context) {
        final HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);
        final HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);

        context.register(SoulLandBiomes.EXTREME_NORTH, extremeNorth(features, carvers));
        context.register(SoulLandBiomes.SUNSET_FOREST, sunsetForest(features, carvers));
        context.register(SoulLandBiomes.ISLAND, island(features, carvers));
        context.register(SoulLandBiomes.SOUL_OCEAN, soulOcean(features, carvers));
        context.register(SoulLandBiomes.STAR_DOU_FOREST, starDouForest(features, carvers));
        context.register(SoulLandBiomes.ICEBOUND_FOREST, iceboundForest(features, carvers));
        context.register(SoulLandBiomes.SOUL_HELL, soulHell(features, carvers));
        context.register(SoulLandBiomes.SOUL_END, soulEnd(features, carvers));
    }

    private static Biome extremeNorth(final HolderGetter<PlacedFeature> features, final HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        final MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.snowySpawns(spawns);

        final BiomeGenerationSettings.Builder generation = overworldGeneration(features, carvers);
        generation.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_SPIKE);
        generation.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_PATCH);

        final BiomeSpecialEffects effects = overworldEffects(EXTREME_NORTH_TEMPERATURE)
                .waterColor(FROZEN_WATER_COLOR)
                .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FROZEN_PEAKS))
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(EXTREME_NORTH_TEMPERATURE)
                .downfall(0.4F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome iceboundForest(final HolderGetter<PlacedFeature> features, final HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        final MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.snowySpawns(spawns);
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));

        final BiomeGenerationSettings.Builder generation = overworldGeneration(features, carvers);
        generation.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_PATCH);
        BiomeDefaultFeatures.addFerns(generation);
        BiomeDefaultFeatures.addTaigaTrees(generation);
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        BiomeDefaultFeatures.addTaigaGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);
        BiomeDefaultFeatures.addRareBerryBushes(generation);

        final BiomeSpecialEffects effects = overworldEffects(ICEBOUND_FOREST_TEMPERATURE)
                .waterColor(FROZEN_WATER_COLOR)
                .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_GROVE))
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(ICEBOUND_FOREST_TEMPERATURE)
                .downfall(0.5F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome sunsetForest(final HolderGetter<PlacedFeature> features, final HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        final MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(spawns);
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 5, 4, 4));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
        BiomeDefaultFeatures.commonSpawns(spawns);

        final BiomeGenerationSettings.Builder generation = overworldGeneration(features, carvers);
        generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, SoulLandPlacedFeatures.ROCK_SPIKE);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_BIRCH_AND_OAK);
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        BiomeDefaultFeatures.addForestGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);

        final BiomeSpecialEffects effects = overworldEffects(SUNSET_FOREST_TEMPERATURE)
                .fogColor(SUNSET_FOG_COLOR)
                .skyColor(SUNSET_SKY_COLOR)
                .foliageColorOverride(SUNSET_FOLIAGE_COLOR)
                .grassColorOverride(SUNSET_GRASS_COLOR)
                .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FOREST))
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(SUNSET_FOREST_TEMPERATURE)
                .downfall(0.8F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome starDouForest(final HolderGetter<PlacedFeature> features, final HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        final MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(spawns);
        BiomeDefaultFeatures.commonSpawns(spawns);

        final BiomeGenerationSettings.Builder generation = overworldGeneration(features, carvers);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_PLAINS);
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        BiomeDefaultFeatures.addForestGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);

        final BiomeSpecialEffects effects = overworldEffects(STAR_DOU_FOREST_TEMPERATURE)
                .foliageColorOverride(STAR_DOU_FOLIAGE_COLOR)
                .grassColorOverride(STAR_DOU_GRASS_COLOR)
                .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FOREST))
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(STAR_DOU_FOREST_TEMPERATURE)
                .downfall(0.7F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome island(final HolderGetter<PlacedFeature> features, final HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        final MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(spawns);
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.TURTLE, 5, 2, 5));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 10, 1, 2));
        BiomeDefaultFeatures.commonSpawns(spawns);

        final BiomeGenerationSettings.Builder generation = overworldGeneration(features, carvers);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_PLAINS);
        BiomeDefaultFeatures.addWarmFlowers(generation);
        BiomeDefaultFeatures.addDefaultGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);
        BiomeDefaultFeatures.addDefaultSeagrass(generation);

        final BiomeSpecialEffects effects = overworldEffects(ISLAND_TEMPERATURE)
                .waterColor(WARM_WATER_COLOR)
                .waterFogColor(WARM_WATER_FOG_COLOR)
                .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SPARSE_JUNGLE))
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(ISLAND_TEMPERATURE)
                .downfall(0.5F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome soulOcean(final HolderGetter<PlacedFeature> features, final HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        final MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.oceanSpawns(spawns, 10, 4, 10);
        spawns.addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GLOW_SQUID, 10, 4, 6));

        final BiomeGenerationSettings.Builder generation = overworldGeneration(features, carvers);
        BiomeDefaultFeatures.addDefaultSeagrass(generation);
        BiomeDefaultFeatures.addColdOceanExtraVegetation(generation);

        final BiomeSpecialEffects effects = overworldEffects(SOUL_OCEAN_TEMPERATURE)
                .waterColor(SOUL_WATER_COLOR)
                .waterFogColor(SOUL_WATER_COLOR)
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(SOUL_OCEAN_TEMPERATURE)
                .downfall(0.5F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome soulHell(final HolderGetter<PlacedFeature> features, final HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        final MobSpawnSettings spawns = new MobSpawnSettings.Builder()
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 20, 5, 5))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 5, 5, 5))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 15, 4, 4))
                .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
                .build();

        final BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers)
                .addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_OPEN)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_SOUL_FIRE)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE_EXTRA)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.BASALT_PILLAR)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_MAGMA)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED);
        BiomeDefaultFeatures.addNetherDefaultOres(generation);

        final BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .fogColor(SOUL_HELL_FOG_COLOR)
                .waterColor(SOUL_WATER_COLOR)
                .waterFogColor(SOUL_WATER_COLOR)
                .skyColor(skyColorForTemperature(SOUL_HELL_TEMPERATURE))
                .ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, WHITE_ASH_PROBABILITY))
                .ambientLoopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, SOUL_SAND_VALLEY_ADDITIONS_CHANCE))
                .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY))
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(SOUL_HELL_TEMPERATURE)
                .downfall(0.0F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns)
                .generationSettings(generation.build())
                .build();
    }

    private static Biome soulEnd(final HolderGetter<PlacedFeature> features, final HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        final MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.endSpawns(spawns);

        final BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers)
                .addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, EndPlacements.END_GATEWAY_RETURN)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, EndPlacements.CHORUS_PLANT);

        final BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .fogColor(SOUL_END_FOG_COLOR)
                .waterColor(SOUL_WATER_COLOR)
                .waterFogColor(SOUL_WATER_COLOR)
                .skyColor(SOUL_END_SKY_COLOR)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(SOUL_END_TEMPERATURE)
                .downfall(0.5F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    private static BiomeGenerationSettings.Builder overworldGeneration(
            final HolderGetter<PlacedFeature> features, final HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        final BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultSprings(generation);
        BiomeDefaultFeatures.addSurfaceFreezing(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        return generation;
    }

    private static BiomeSpecialEffects.Builder overworldEffects(final float temperature) {
        return new BiomeSpecialEffects.Builder()
                .fogColor(OVERWORLD_FOG_COLOR)
                .waterColor(NORMAL_WATER_COLOR)
                .waterFogColor(NORMAL_WATER_FOG_COLOR)
                .skyColor(skyColorForTemperature(temperature))
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS);
    }

    private static int skyColorForTemperature(final float temperature) {
        final float scaled = Mth.clamp(temperature / SKY_COLOR_TEMPERATURE_SCALE, -1.0F, 1.0F);
        return Mth.hsvToRgb(
                SKY_HUE_BASE - scaled * SKY_HUE_SPREAD,
                SKY_SATURATION_BASE + scaled * SKY_SATURATION_SPREAD,
                1.0F);
    }
}
