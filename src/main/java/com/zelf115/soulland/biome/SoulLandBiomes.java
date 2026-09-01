package com.zelf115.soulland.biome;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class SoulLandBiomes {

    // Overworld
    public static final ResourceKey<Biome> EXTREME_NORTH = key("extreme_north");
    public static final ResourceKey<Biome> SUNSET_FOREST = key("sunset_forest");
    public static final ResourceKey<Biome> ISLAND = key("island");
    public static final ResourceKey<Biome> SOUL_OCEAN = key("soul_ocean");
    public static final ResourceKey<Biome> STAR_DOU_FOREST = key("star_dou_forest");
    public static final ResourceKey<Biome> ICEBOUND_FOREST = key("icebound_forest");

    // Nether
    public static final ResourceKey<Biome> SOUL_HELL = key("soul_hell");

    // End
    public static final ResourceKey<Biome> SOUL_END = key("soul_end");

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, name));
    }
}
