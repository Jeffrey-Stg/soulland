package com.zelf115.soulland.client;

import com.zelf115.soulland.spirit.SpiritBeastEntity;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class SpiritBeastRenderer extends MobRenderer<SpiritBeastEntity, ZombieModel<SpiritBeastEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/zombie/zombie.png");

    public SpiritBeastRenderer(final EntityRendererProvider.Context context) {
        super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(final SpiritBeastEntity entity) {
        return TEXTURE;
    }
}
