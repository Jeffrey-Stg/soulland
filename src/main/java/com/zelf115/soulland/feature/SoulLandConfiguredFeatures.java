package com.zelf115.soulland.feature;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public final class SoulLandConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> ROCK_SPIKE = key("rock_spike");

    private SoulLandConfiguredFeatures() {
    }

    public static void bootstrap(final BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(ROCK_SPIKE, new ConfiguredFeature<>(
                SoulLandFeatures.ROCK_SPIKE.get(),
                new BlockStateConfiguration(Blocks.STONE.defaultBlockState())));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> key(final String name) {
        return ResourceKey.create(
                Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, name));
    }
}
