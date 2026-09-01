package com.zelf115.soulland.feature;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SoulLandFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE, SoulLand.MODID);

    public static final DeferredHolder<Feature<?>, RockSpikeFeature> ROCK_SPIKE =
            FEATURES.register("rock_spike", () -> new RockSpikeFeature(BlockStateConfiguration.CODEC));

    private SoulLandFeatures() {
    }
}
