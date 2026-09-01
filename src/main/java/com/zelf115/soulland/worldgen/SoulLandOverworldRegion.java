package com.zelf115.soulland.worldgen;

import com.mojang.datafixers.util.Pair;
import com.zelf115.soulland.biome.SoulLandBiomes;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ModifiedVanillaOverworldBuilder;
import terrablender.api.Region;
import terrablender.api.RegionType;

/**
 * Keeps vanilla's overworld layout inside this region but hands the climate niches the mod
 * has its own take on to the mod's biomes.
 */
public class SoulLandOverworldRegion extends Region {

    private static final float CLIMATE_MIN = -1.0F;
    private static final float CLIMATE_MAX = 1.0F;
    private static final float DEEP_OCEAN_CONTINENTALNESS_MIN = -1.05F;
    private static final float DEEP_OCEAN_CONTINENTALNESS_MAX = -0.455F;

    private static final Climate.Parameter FULL_RANGE = Climate.Parameter.span(CLIMATE_MIN, CLIMATE_MAX);
    private static final Climate.Parameter DEEP_OCEAN_CONTINENTALNESS =
            Climate.Parameter.span(DEEP_OCEAN_CONTINENTALNESS_MIN, DEEP_OCEAN_CONTINENTALNESS_MAX);

    /** Vanilla's five temperature bands, mirrored from {@code OverworldBiomeBuilder}. */
    private static final Climate.Parameter[] TEMPERATURES = {
            Climate.Parameter.span(-1.0F, -0.45F),
            Climate.Parameter.span(-0.45F, -0.15F),
            Climate.Parameter.span(-0.15F, 0.2F),
            Climate.Parameter.span(0.2F, 0.55F),
            Climate.Parameter.span(0.55F, 1.0F)
    };

    /** Vanilla registers every surface biome at both of these depths. */
    private static final Climate.Parameter[] SURFACE_DEPTHS = {
            Climate.Parameter.point(0.0F),
            Climate.Parameter.point(1.0F)
    };

    private static final Climate.Parameter DEEP_OCEAN_BELOW_ISLANDS =
            Climate.Parameter.span(DEEP_OCEAN_CONTINENTALNESS_MIN, SoulLandIslandNiche.CONTINENTALNESS_MIN);
    private static final Climate.Parameter DEEP_OCEAN_ABOVE_ISLANDS =
            Climate.Parameter.span(SoulLandIslandNiche.CONTINENTALNESS_MAX, DEEP_OCEAN_CONTINENTALNESS_MAX);
    private static final Climate.Parameter EROSION_BELOW_ISLANDS =
            Climate.Parameter.span(CLIMATE_MIN, SoulLandIslandNiche.EROSION_MIN);
    private static final Climate.Parameter EROSION_ABOVE_ISLANDS =
            Climate.Parameter.span(SoulLandIslandNiche.EROSION_MAX, CLIMATE_MAX);

    private static final float NO_OFFSET = 0.0F;

    public SoulLandOverworldRegion(final ResourceLocation name, final int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(final Registry<Biome> registry, final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        this.addModifiedVanillaOverworldBiomes(mapper, (final ModifiedVanillaOverworldBuilder builder) -> {
            builder.replaceBiome(Biomes.SNOWY_PLAINS, SoulLandBiomes.EXTREME_NORTH);
            builder.replaceBiome(Biomes.ICE_SPIKES, SoulLandBiomes.EXTREME_NORTH);
            builder.replaceBiome(Biomes.SNOWY_SLOPES, SoulLandBiomes.EXTREME_NORTH);
            builder.replaceBiome(Biomes.FROZEN_PEAKS, SoulLandBiomes.EXTREME_NORTH);

            builder.replaceBiome(Biomes.SNOWY_TAIGA, SoulLandBiomes.ICEBOUND_FOREST);
            builder.replaceBiome(Biomes.GROVE, SoulLandBiomes.ICEBOUND_FOREST);

            builder.replaceBiome(Biomes.BIRCH_FOREST, SoulLandBiomes.SUNSET_FOREST);
            builder.replaceBiome(Biomes.OLD_GROWTH_BIRCH_FOREST, SoulLandBiomes.SUNSET_FOREST);

            builder.replaceBiome(Biomes.FOREST, SoulLandBiomes.STAR_DOU_FOREST);

            removeVanillaDeepOcean(builder);
        });

        addSoulOceanAroundIslands(mapper);
        addIslands(mapper);
    }

    /**
     * The deep ocean points span the island niche with a full erosion range, so they would tie with
     * the island points and win at random. They are dropped here and re-added around the niche.
     */
    private void removeVanillaDeepOcean(final ModifiedVanillaOverworldBuilder builder) {
        for (final Climate.Parameter temperature : TEMPERATURES) {
            for (final Climate.Parameter depth : SURFACE_DEPTHS) {
                builder.removeParameter(Climate.parameters(
                        temperature, FULL_RANGE, DEEP_OCEAN_CONTINENTALNESS, FULL_RANGE, depth, FULL_RANGE, NO_OFFSET));
            }
        }
    }

    private void addSoulOceanAroundIslands(final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        for (final Climate.Parameter temperature : TEMPERATURES) {
            for (final Climate.Parameter depth : SURFACE_DEPTHS) {
                addSoulOcean(mapper, temperature, DEEP_OCEAN_BELOW_ISLANDS, FULL_RANGE, depth);
                addSoulOcean(mapper, temperature, DEEP_OCEAN_ABOVE_ISLANDS, FULL_RANGE, depth);
                addSoulOcean(mapper, temperature, SoulLandIslandNiche.CONTINENTALNESS, EROSION_BELOW_ISLANDS, depth);
                addSoulOcean(mapper, temperature, SoulLandIslandNiche.CONTINENTALNESS, EROSION_ABOVE_ISLANDS, depth);
            }
        }
    }

    private void addSoulOcean(
            final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper,
            final Climate.Parameter temperature,
            final Climate.Parameter continentalness,
            final Climate.Parameter erosion,
            final Climate.Parameter depth) {
        this.addBiome(
                mapper,
                Climate.parameters(temperature, FULL_RANGE, continentalness, erosion, depth, FULL_RANGE, NO_OFFSET),
                SoulLandBiomes.SOUL_OCEAN);
    }

    private void addIslands(final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        for (final Climate.Parameter depth : SURFACE_DEPTHS) {
            this.addBiome(
                    mapper,
                    Climate.parameters(
                            FULL_RANGE,
                            FULL_RANGE,
                            SoulLandIslandNiche.CONTINENTALNESS,
                            SoulLandIslandNiche.EROSION,
                            depth,
                            FULL_RANGE,
                            NO_OFFSET),
                    SoulLandBiomes.ISLAND);
        }
    }
}
