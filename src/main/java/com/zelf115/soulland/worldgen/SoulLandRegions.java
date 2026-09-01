package com.zelf115.soulland.worldgen;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.biome.SoulLandBiomes;
import net.minecraft.resources.ResourceLocation;
import terrablender.api.EndBiomeRegistry;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

/**
 * Hands the mod's biomes to TerraBlender so they generate alongside vanilla's.
 * Vanilla regions carry a weight of 10, so these weights read as a share of the world.
 */
public final class SoulLandRegions {

    private static final int OVERWORLD_REGION_WEIGHT = 5;
    private static final int NETHER_REGION_WEIGHT = 5;
    private static final int SOUL_END_HIGHLANDS_WEIGHT = 5;

    private SoulLandRegions() {
    }

    public static void register() {
        Regions.register(new SoulLandOverworldRegion(id("overworld"), OVERWORLD_REGION_WEIGHT));
        Regions.register(new SoulLandNetherRegion(id("nether"), NETHER_REGION_WEIGHT));
        EndBiomeRegistry.registerHighlandsBiome(SoulLandBiomes.SOUL_END, SOUL_END_HIGHLANDS_WEIGHT);
        SurfaceRuleManager.addSurfaceRules(
                SurfaceRuleManager.RuleCategory.OVERWORLD, SoulLand.MODID, SoulLandSurfaceRules.overworld());
        SurfaceRuleManager.addSurfaceRules(
                SurfaceRuleManager.RuleCategory.NETHER, SoulLand.MODID, SoulLandSurfaceRules.nether());
    }

    private static ResourceLocation id(final String path) {
        return ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, path);
    }
}
