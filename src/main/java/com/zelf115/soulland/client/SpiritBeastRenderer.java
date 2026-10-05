package com.zelf115.soulland.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zelf115.soulland.client.model.BeastLook;
import com.zelf115.soulland.spirit.SpiritBeastEntity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

/** Draws each spirit beast with its shape's model in its own skin, or a zombie's until that skin is supplied. */
public final class SpiritBeastRenderer extends MobRenderer<SpiritBeastEntity, EntityModel<SpiritBeastEntity>> {
    private static final ResourceLocation FALLBACK_SKIN =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/zombie/zombie.png");
    private static final String SKIN_FOLDER = "spirit_beast/";

    private final EntitySkins skins;
    private final float scale;
    private final Map<EntityType<?>, ResourceLocation> skinsByType = new HashMap<>();

    public SpiritBeastRenderer(final EntityRendererProvider.Context context, final BeastLook look) {
        super(context, look.model(), look.shadowRadius());
        this.skins = new EntitySkins(context.getResourceManager());
        this.scale = look.scale();
    }

    @Override
    protected void scale(final SpiritBeastEntity entity, final PoseStack poseStack, final float partialTick) {
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(final SpiritBeastEntity entity) {
        return skinsByType.computeIfAbsent(entity.getType(), this::skinFor);
    }

    private ResourceLocation skinFor(final EntityType<?> type) {
        return skins.skinOrFallback(SKIN_FOLDER + EntityType.getKey(type).getPath(), FALLBACK_SKIN);
    }
}
