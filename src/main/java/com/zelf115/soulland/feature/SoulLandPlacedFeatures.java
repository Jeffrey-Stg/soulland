package com.zelf115.soulland.feature;

import com.zelf115.soulland.SoulLand;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public final class SoulLandPlacedFeatures {

    public static final ResourceKey<PlacedFeature> ROCK_SPIKE = key("rock_spike");

    private static final int ROCK_SPIKE_CHUNKS_BETWEEN = 6;

    private SoulLandPlacedFeatures() {
    }

    public static void bootstrap(final BootstrapContext<PlacedFeature> context) {
        final HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        context.register(ROCK_SPIKE, new PlacedFeature(
                configuredFeatures.getOrThrow(SoulLandConfiguredFeatures.ROCK_SPIKE),
                List.of(
                        RarityFilter.onAverageOnceEvery(ROCK_SPIKE_CHUNKS_BETWEEN),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                        BiomeFilter.biome())));
    }

    private static ResourceKey<PlacedFeature> key(final String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, name));
    }
}
