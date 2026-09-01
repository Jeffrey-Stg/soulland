package com.zelf115.soulland.qi;

import com.zelf115.soulland.biome.SoulLandBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

import java.util.Set;

/**
 * Manages qi amounts for biomes.
 * Mod biomes yield qi between 4 and 10.
 * All other biomes yield qi between 1 and 3.
 * Values are deterministic per world seed and chunk position.
 */
public class QiManager {

    private static final int MOD_QI_MIN = 4;
    private static final int MOD_QI_MAX = 10;
    private static final int VANILLA_QI_MIN = 1;
    private static final int VANILLA_QI_MAX = 3;

    private static final Set<ResourceKey<Biome>> MOD_BIOMES = Set.of(
            SoulLandBiomes.EXTREME_NORTH,
            SoulLandBiomes.SUNSET_FOREST,
            SoulLandBiomes.ISLAND,
            SoulLandBiomes.SOUL_OCEAN,
            SoulLandBiomes.STAR_DOU_FOREST,
            SoulLandBiomes.ICEBOUND_FOREST,
            SoulLandBiomes.SOUL_HELL,
            SoulLandBiomes.SOUL_END
    );

    public static int getQiAmount(ResourceKey<Biome> biomeKey, long worldSeed, int chunkX, int chunkZ) {
        long seed = worldSeed ^ ((long) chunkX * 341873128712L) ^ ((long) chunkZ * 132897987541L)
                ^ (long) biomeKey.location().hashCode();
        RandomSource random = RandomSource.create(seed);
        if (isModBiome(biomeKey)) {
            return MOD_QI_MIN + random.nextInt(MOD_QI_MAX - MOD_QI_MIN + 1);
        }
        return VANILLA_QI_MIN + random.nextInt(VANILLA_QI_MAX - VANILLA_QI_MIN + 1);
    }

    public static boolean isModBiome(ResourceKey<Biome> biomeKey) {
        return MOD_BIOMES.contains(biomeKey);
    }
}
