package com.zelf115.soulland.qi;

import com.zelf115.soulland.biome.SoulLandBiomes;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

/**
 * Manages qi amounts for biomes.
 * Mod biomes yield qi between 4 and 10.
 * All other biomes (vanilla, third-party mods) yield qi between 1 and 3.
 * Values are deterministic per world seed and chunk position.
 */
public class QiManager {

    private static final int MOD_QI_MIN = 4;
    private static final int MOD_QI_MAX = 10;
    private static final int DEFAULT_QI_MIN = 1;
    private static final int DEFAULT_QI_MAX = 3;

    private static final long CHUNK_X_SCRAMBLE = 341873128712L;
    private static final long CHUNK_Z_SCRAMBLE = 132897987541L;

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

    public static int getQiAmount(final ResourceKey<Biome> biomeKey, final long worldSeed, final int chunkX, final int chunkZ) {
        final long seed = worldSeed ^ ((long) chunkX * CHUNK_X_SCRAMBLE) ^ ((long) chunkZ * CHUNK_Z_SCRAMBLE)
                ^ (long) biomeKey.location().hashCode();
        final RandomSource random = RandomSource.create(seed);
        if (isModBiome(biomeKey)) {
            return MOD_QI_MIN + random.nextInt(MOD_QI_MAX - MOD_QI_MIN + 1);
        }
        return DEFAULT_QI_MIN + random.nextInt(DEFAULT_QI_MAX - DEFAULT_QI_MIN + 1);
    }

    public static boolean isModBiome(final ResourceKey<Biome> biomeKey) {
        return MOD_BIOMES.contains(biomeKey);
    }
}
