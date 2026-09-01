package com.zelf115.soulland.datagen;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.biome.SoulLandBiomeBuilder;
import com.zelf115.soulland.feature.SoulLandConfiguredFeatures;
import com.zelf115.soulland.feature.SoulLandPlacedFeatures;
import com.zelf115.soulland.worldgen.SoulLandDensityFunctions;
import java.util.Set;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = SoulLand.MODID)
public final class SoulLandDataGenerators {

    private static final String VANILLA_NAMESPACE = "minecraft";

    private SoulLandDataGenerators() {
    }

    @SubscribeEvent
    static void gatherData(final GatherDataEvent event) {
        final PackOutput output = event.getGenerator().getPackOutput();
        final RegistrySetBuilder datapackEntries = new RegistrySetBuilder()
                .add(Registries.DENSITY_FUNCTION, SoulLandDensityFunctions::bootstrap)
                .add(Registries.CONFIGURED_FEATURE, SoulLandConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, SoulLandPlacedFeatures::bootstrap)
                .add(Registries.BIOME, SoulLandBiomeBuilder::bootstrap);

        // The island terrain overrides minecraft:overworld/depth, so that namespace has to be written too.
        final DatapackBuiltinEntriesProvider datapackProvider = event.addProvider(new DatapackBuiltinEntriesProvider(
                output,
                RegistryPatchGenerator.createLookup(event.getLookupProvider(), datapackEntries),
                Set.of(SoulLand.MODID, VANILLA_NAMESPACE)));

        event.addProvider(new SoulLandBiomeTagsProvider(
                output, datapackProvider.getRegistryProvider(), event.getExistingFileHelper()));
    }
}
