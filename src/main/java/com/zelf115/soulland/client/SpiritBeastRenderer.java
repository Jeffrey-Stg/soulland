package com.zelf115.soulland.client;

import com.zelf115.soulland.spirit.SpiritBeastEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Placeholder look for every spirit beast until issue #28 supplies models and skins. */
public final class SpiritBeastRenderer extends MobRenderer<SpiritBeastEntity, HumanoidModel<SpiritBeastEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/zombie/zombie.png");
    private static final float SHADOW_RADIUS = 0.5F;

    public SpiritBeastRenderer(final EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), SHADOW_RADIUS);
    }

    @Override
    public ResourceLocation getTextureLocation(final SpiritBeastEntity entity) {
        return TEXTURE;
    }
}
