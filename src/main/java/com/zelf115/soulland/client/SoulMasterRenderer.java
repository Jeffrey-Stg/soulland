package com.zelf115.soulland.client;

import com.zelf115.soulland.tournament.SoulMasterEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Placeholder look for tournament opponents until a soul master skin is supplied. */
public final class SoulMasterRenderer extends MobRenderer<SoulMasterEntity, HumanoidModel<SoulMasterEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");
    private static final float SHADOW_RADIUS = 0.5F;

    public SoulMasterRenderer(final EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), SHADOW_RADIUS);
    }

    @Override
    public ResourceLocation getTextureLocation(final SoulMasterEntity entity) {
        return TEXTURE;
    }
}
