package com.zelf115.soulland.worldgen;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouterData;

/**
 * Raises land out of the open ocean so the Island biome has a landmass of its own, rather than
 * borrowing the one niche vanilla reserves for mushroom fields.
 * <p>
 * Terrain height is settled before any biome is chosen, so this cannot be done from a biome or
 * from TerraBlender. Instead {@code minecraft:overworld/depth} is overridden to add one more term
 * on top of vanilla's untouched offset, lifting the sea floor above sea level in a narrow window
 * of continentalness and erosion. {@link SoulLandOverworldRegion} hands that same window to the
 * Island biome.
 */
public final class SoulLandDensityFunctions {

    public static final ResourceKey<DensityFunction> ISLAND_OFFSET = key("island_offset");

    /** Vanilla's terrain offset everywhere in the deep ocean band. */
    private static final float OCEAN_FLOOR_OFFSET = -0.2222F;
    /** The offset vanilla gives a mushroom island, which is the height these islands copy. */
    private static final float MUSHROOM_ISLAND_OFFSET = 0.044F;
    private static final float ISLAND_CREST = MUSHROOM_ISLAND_OFFSET - OCEAN_FLOOR_OFFSET;

    private static final float NO_LIFT = 0.0F;
    private static final float FULL_LIFT = 1.0F;

    private static final int WORLD_BOTTOM_Y = -64;
    private static final int WORLD_TOP_Y = 320;
    private static final double DEPTH_AT_WORLD_BOTTOM = 1.5D;
    private static final double DEPTH_AT_WORLD_TOP = -1.5D;

    private SoulLandDensityFunctions() {
    }

    public static void bootstrap(final BootstrapContext<DensityFunction> context) {
        final HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);

        context.register(ISLAND_OFFSET, islandOffset(functions));
        context.register(NoiseRouterData.DEPTH, depthWithIslands(functions));
    }

    private static DensityFunction islandOffset(final HolderGetter<DensityFunction> functions) {
        final DensityFunctions.Spline.Coordinate continentalness =
                new DensityFunctions.Spline.Coordinate(functions.getOrThrow(NoiseRouterData.CONTINENTS));
        final DensityFunctions.Spline.Coordinate erosion =
                new DensityFunctions.Spline.Coordinate(functions.getOrThrow(NoiseRouterData.EROSION));

        final CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> crest =
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(continentalness)
                        .addPoint(SoulLandIslandNiche.CONTINENTALNESS_MIN, NO_LIFT)
                        .addPoint(SoulLandIslandNiche.CONTINENTALNESS_PEAK, ISLAND_CREST)
                        .addPoint(SoulLandIslandNiche.CONTINENTALNESS_MAX, NO_LIFT)
                        .build();

        // Erosion gates how often the crest is reached at all, and so how rare these islands are.
        final CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> gate =
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(erosion)
                        .addPoint(SoulLandIslandNiche.EROSION_MIN, NO_LIFT)
                        .addPoint(SoulLandIslandNiche.EROSION_PLATEAU_MIN, FULL_LIFT)
                        .addPoint(SoulLandIslandNiche.EROSION_PLATEAU_MAX, FULL_LIFT)
                        .addPoint(SoulLandIslandNiche.EROSION_MAX, NO_LIFT)
                        .build();

        return DensityFunctions.flatCache(DensityFunctions.cache2d(
                DensityFunctions.mul(DensityFunctions.spline(crest), DensityFunctions.spline(gate))));
    }

    private static DensityFunction depthWithIslands(final HolderGetter<DensityFunction> functions) {
        final DensityFunction vanillaDepth = DensityFunctions.add(
                DensityFunctions.yClampedGradient(
                        WORLD_BOTTOM_Y, WORLD_TOP_Y, DEPTH_AT_WORLD_BOTTOM, DEPTH_AT_WORLD_TOP),
                reference(functions, NoiseRouterData.OFFSET));
        return DensityFunctions.add(vanillaDepth, reference(functions, ISLAND_OFFSET));
    }

    private static DensityFunction reference(final HolderGetter<DensityFunction> functions, final ResourceKey<DensityFunction> key) {
        final Holder<DensityFunction> holder = functions.getOrThrow(key);
        return new DensityFunctions.HolderHolder(holder);
    }

    private static ResourceKey<DensityFunction> key(final String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, name));
    }
}
