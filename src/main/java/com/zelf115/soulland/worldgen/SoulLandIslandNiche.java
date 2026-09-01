package com.zelf115.soulland.worldgen;

import net.minecraft.world.level.biome.Climate;

/**
 * The slice of noise space the Island biome owns. {@link SoulLandDensityFunctions} lifts the sea
 * floor above sea level here and {@link SoulLandOverworldRegion} hands the same slice to the
 * biome, so the two must be read from one place or the biome drifts off its own island.
 */
public final class SoulLandIslandNiche {

    public static final float CONTINENTALNESS_MIN = -0.82F;
    public static final float CONTINENTALNESS_PEAK = -0.76F;
    public static final float CONTINENTALNESS_MAX = -0.70F;

    public static final float EROSION_MIN = -0.45F;
    public static final float EROSION_PLATEAU_MIN = -0.35F;
    public static final float EROSION_PLATEAU_MAX = -0.25F;
    public static final float EROSION_MAX = -0.15F;

    public static final Climate.Parameter CONTINENTALNESS =
            Climate.Parameter.span(CONTINENTALNESS_MIN, CONTINENTALNESS_MAX);
    public static final Climate.Parameter EROSION =
            Climate.Parameter.span(EROSION_MIN, EROSION_MAX);

    private SoulLandIslandNiche() {
    }
}
