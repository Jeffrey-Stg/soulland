package com.zelf115.soulland.qi;

import com.zelf115.soulland.biome.SoulLandBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.Map;
import java.util.Optional;

/**
 * Manages qi amounts for biomes.
 * Biomes from this mod have qi between 4 and 10.
 * All other biomes have qi between 1 and 3.
 */
public class QiManager {

    private static final Map<ResourceKey<Biome>, Integer> MOD_BIOME_QI = Map.of(
            SoulLandBiomes.EXTREME_NORTH,   6,
            SoulLandBiomes.SUNSET_FOREST,   7,
            SoulLandBiomes.ISLAND,          4,
            SoulLandBiomes.SOUL_OCEAN,      8,
            SoulLandBiomes.STAR_DOU_FOREST, 9,
            SoulLandBiomes.ICEBOUND_FOREST, 5,
            SoulLandBiomes.SOUL_HELL,       10,
            SoulLandBiomes.SOUL_END,        9
    );

    private static final int DEFAULT_VANILLA_QI = 2;

    public static int getQiAmount(ResourceKey<Biome> biomeKey) {
        return Optional.ofNullable(MOD_BIOME_QI.get(biomeKey)).orElse(DEFAULT_VANILLA_QI);
    }

    public static boolean isModBiome(ResourceKey<Biome> biomeKey) {
        return MOD_BIOME_QI.containsKey(biomeKey);
    }
}
