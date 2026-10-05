package com.zelf115.soulland.client.model;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.client.SpiritBeastRenderer;
import com.zelf115.soulland.spirit.SpiritBeastEntity;
import com.zelf115.soulland.spirit.SpiritBeastShape;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Ties each spirit beast shape to the model that draws it, one model layer per shape. */
public final class SpiritBeastModels {
    private static final float PORCUPINE_SCALE = 0.6F;

    private SpiritBeastModels() {
    }

    public static void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (final SpiritBeastShape shape : SpiritBeastShape.values()) {
            event.registerLayerDefinition(layerOf(shape), layerDefinitionFor(shape));
        }
    }

    public static EntityRendererProvider<SpiritBeastEntity> rendererFor(final SpiritBeastShape shape) {
        return context -> new SpiritBeastRenderer(context, lookFor(shape, context.bakeLayer(layerOf(shape))));
    }

    private static ModelLayerLocation layerOf(final SpiritBeastShape shape) {
        return new ModelLayerLocation(
                ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, shape.name().toLowerCase(Locale.ROOT)), "main");
    }

    private static Supplier<LayerDefinition> layerDefinitionFor(final SpiritBeastShape shape) {
        return switch (shape) {
            case BIG_CAT -> BigCatModel::createCatLayer;
            case LION -> BigCatModel::createLionLayer;
            case BIRD -> BirdModel::createBodyLayer;
            case SERPENT -> SerpentModel::createBodyLayer;
            case CANINE -> CanineModel::createBodyLayer;
            case BEAR -> BearModel::createBodyLayer;
            case HEAVY -> HeavyBeastModel::createHippoLayer;
            case PORCUPINE -> HeavyBeastModel::createPorcupineLayer;
            case APE -> ApeModel::createApeLayer;
            case HORNED_APE -> ApeModel::createHornedLayer;
            case DRAGON -> DragonModel::createBodyLayer;
            case SPIDER -> ArachnidModel::createBodyLayer;
            case SCORPION -> ScorpionModel::createBodyLayer;
            case SHARK -> SeaBeastModel::createSharkLayer;
            case ORCA -> SeaBeastModel::createOrcaLayer;
            case OCTOPUS -> OctopusModel::createBodyLayer;
            case FLOATING_EYE -> FloatingEyeModel::createBodyLayer;
        };
    }

    private static BeastLook lookFor(final SpiritBeastShape shape, final ModelPart part) {
        return switch (shape) {
            case BIG_CAT, LION -> new BeastLook(new BigCatModel(part), 0.7F);
            case BIRD -> new BeastLook(new BirdModel(part), 0.4F);
            case SERPENT -> new BeastLook(new SerpentModel(part), 0.6F);
            case CANINE -> new BeastLook(new CanineModel(part), 0.5F);
            case BEAR -> new BeastLook(new BearModel(part), 0.9F);
            case HEAVY -> new BeastLook(new HeavyBeastModel(part), 0.9F);
            case PORCUPINE -> new BeastLook(new HeavyBeastModel(part), 0.5F, PORCUPINE_SCALE);
            case APE, HORNED_APE -> new BeastLook(new ApeModel(part), 0.8F);
            case DRAGON -> new BeastLook(new DragonModel(part), 1.0F);
            case SPIDER -> new BeastLook(new ArachnidModel(part), 0.8F);
            case SCORPION -> new BeastLook(new ScorpionModel(part), 0.8F);
            case SHARK -> new BeastLook(new SeaBeastModel(part, SeaBeastModel.TailStroke.SIDEWAYS), 0.6F);
            case ORCA -> new BeastLook(new SeaBeastModel(part, SeaBeastModel.TailStroke.VERTICAL), 0.6F);
            case OCTOPUS -> new BeastLook(new OctopusModel(part), 0.7F);
            case FLOATING_EYE -> new BeastLook(new FloatingEyeModel(part), 0.5F);
        };
    }
}
