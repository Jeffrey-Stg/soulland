package com.zelf115.soulland.worldgen;

import com.zelf115.soulland.biome.SoulLandBiomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;

/**
 * Surface blocks for the mod's biomes. Only biomes that must not fall back to their dimension's
 * default ground appear here; the rest ride on vanilla's defaults.
 */
public final class SoulLandSurfaceRules {

    private static final int NO_WATER_ABOVE = -1;
    private static final int SURFACE_DEPTH_MULTIPLIER = 0;
    private static final double SOUL_SAND_SELECTOR_THRESHOLD = 0.0D;

    private SoulLandSurfaceRules() {
    }

    public static SurfaceRules.RuleSource overworld() {
        return SurfaceRules.sequence(extremeNorth());
    }

    public static SurfaceRules.RuleSource nether() {
        return SurfaceRules.sequence(soulHell());
    }

    private static SurfaceRules.RuleSource extremeNorth() {
        final SurfaceRules.RuleSource snowBlock = SurfaceRules.state(Blocks.SNOW_BLOCK.defaultBlockState());
        return SurfaceRules.ifTrue(
                SurfaceRules.isBiome(SoulLandBiomes.EXTREME_NORTH),
                SurfaceRules.ifTrue(
                        SurfaceRules.abovePreliminarySurface(),
                        SurfaceRules.ifTrue(
                                SurfaceRules.ON_FLOOR,
                                SurfaceRules.ifTrue(
                                        SurfaceRules.waterBlockCheck(NO_WATER_ABOVE, SURFACE_DEPTH_MULTIPLIER),
                                        snowBlock))));
    }

    /** Soul fire only survives on soul sand and soul soil, so Soul Hell cannot keep the netherrack default. */
    private static SurfaceRules.RuleSource soulHell() {
        final SurfaceRules.RuleSource soulGround = SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition(Noises.NETHER_STATE_SELECTOR, SOUL_SAND_SELECTOR_THRESHOLD),
                        SurfaceRules.state(Blocks.SOUL_SAND.defaultBlockState())),
                SurfaceRules.state(Blocks.SOUL_SOIL.defaultBlockState()));
        return SurfaceRules.ifTrue(
                SurfaceRules.isBiome(SoulLandBiomes.SOUL_HELL),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, soulGround),
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, soulGround),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, soulGround)));
    }
}
