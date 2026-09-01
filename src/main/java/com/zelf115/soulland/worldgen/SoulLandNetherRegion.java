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
import terrablender.api.Region;
import terrablender.api.RegionType;

/**
 * Restates vanilla's nether climate points so this region stays complete, and claims the
 * cold/dry corner left unused by vanilla for Soul Hell.
 */
public class SoulLandNetherRegion extends Region {

    private static final Climate.ParameterPoint NETHER_WASTES_CLIMATE =
            Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    private static final Climate.ParameterPoint SOUL_SAND_VALLEY_CLIMATE =
            Climate.parameters(0.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    private static final Climate.ParameterPoint CRIMSON_FOREST_CLIMATE =
            Climate.parameters(0.4F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    private static final Climate.ParameterPoint WARPED_FOREST_CLIMATE =
            Climate.parameters(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.375F);
    private static final Climate.ParameterPoint BASALT_DELTAS_CLIMATE =
            Climate.parameters(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.175F);
    private static final Climate.ParameterPoint SOUL_HELL_CLIMATE =
            Climate.parameters(-0.5F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

    public SoulLandNetherRegion(final ResourceLocation name, final int weight) {
        super(name, RegionType.NETHER, weight);
    }

    @Override
    public void addBiomes(final Registry<Biome> registry, final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        this.addBiome(mapper, NETHER_WASTES_CLIMATE, Biomes.NETHER_WASTES);
        this.addBiome(mapper, SOUL_SAND_VALLEY_CLIMATE, Biomes.SOUL_SAND_VALLEY);
        this.addBiome(mapper, CRIMSON_FOREST_CLIMATE, Biomes.CRIMSON_FOREST);
        this.addBiome(mapper, WARPED_FOREST_CLIMATE, Biomes.WARPED_FOREST);
        this.addBiome(mapper, BASALT_DELTAS_CLIMATE, Biomes.BASALT_DELTAS);
        this.addBiome(mapper, SOUL_HELL_CLIMATE, SoulLandBiomes.SOUL_HELL);
    }
}
