package com.zelf115.soulland.client;

import com.zelf115.soulland.tournament.SoulMasterEntity;
import java.util.List;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Gives each tournament opponent one of the soul master skins, or Steve until one is supplied. */
public final class SoulMasterRenderer extends MobRenderer<SoulMasterEntity, HumanoidModel<SoulMasterEntity>> {

    private static final ResourceLocation FALLBACK_SKIN =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");
    private static final String SKIN_FOLDER = "soul_master";
    private static final float SHADOW_RADIUS = 0.5F;

    private final List<ResourceLocation> skins;

    public SoulMasterRenderer(final EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), SHADOW_RADIUS);
        this.skins = new EntitySkins(context.getResourceManager()).numberedSkins(SKIN_FOLDER);
    }

    @Override
    public ResourceLocation getTextureLocation(final SoulMasterEntity entity) {
        if (skins.isEmpty()) {
            return FALLBACK_SKIN;
        }
        return skins.get(Math.floorMod(entity.getUUID().hashCode(), skins.size()));
    }
}
