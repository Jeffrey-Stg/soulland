package com.zelf115.soulland.client.model;

import com.zelf115.soulland.spirit.SpiritBeastEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Octopuses: a great mantle carried on a ring of eight splayed tentacles that ripple as it walks.
 *
 * <p>The skin generator paints to these texture offsets; moving a box means repainting its skins.
 */
public final class OctopusModel extends HierarchicalModel<SpiritBeastEntity> {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;
    private static final int TENTACLES = 8;
    private static final float TENTACLE_TOP = 10.0F;
    private static final float TENTACLE_RING_RADIUS = 4.0F;
    private static final float TENTACLE_SPLAY = 0.35F;
    private static final float RIPPLE_FREQUENCY = 0.15F;
    private static final float RIPPLE_OFFSET = 0.8F;
    private static final float RIPPLE_IDLE_ANGLE = 0.08F;
    private static final float RIPPLE_MOVING_ANGLE = 0.3F;

    private final ModelPart root;
    private final ModelPart mantle;
    private final ModelPart[] tentacles = new ModelPart[TENTACLES];

    public OctopusModel(final ModelPart root) {
        this.root = root;
        this.mantle = root.getChild("head");
        for (int index = 0; index < TENTACLES; index++) {
            tentacles[index] = root.getChild(tentacleName(index));
        }
    }

    public static LayerDefinition createBodyLayer() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -12.0F, -6.0F, 12.0F, 12.0F, 12.0F),
                PartPose.offset(0.0F, TENTACLE_TOP, 0.0F));
        final CubeListBuilder tentacle = CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F);
        for (int index = 0; index < TENTACLES; index++) {
            final float angle = index * Mth.TWO_PI / TENTACLES;
            root.addOrReplaceChild(tentacleName(index), tentacle, PartPose.offsetAndRotation(
                    Mth.cos(angle) * TENTACLE_RING_RADIUS, TENTACLE_TOP, Mth.sin(angle) * TENTACLE_RING_RADIUS,
                    TENTACLE_SPLAY, outwardYaw(angle), 0.0F));
        }
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    /** The yaw that turns a tentacle's local +z, the way its splay swings its tip, to point away from the centre. */
    private static float outwardYaw(final float angle) {
        return Mth.HALF_PI - angle;
    }

    private static String tentacleName(final int index) {
        return "tentacle_" + index;
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(final SpiritBeastEntity entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        mantle.yRot = netHeadYaw * Mth.DEG_TO_RAD;

        final float amplitude = RIPPLE_IDLE_ANGLE + limbSwingAmount * RIPPLE_MOVING_ANGLE;
        for (int index = 0; index < TENTACLES; index++) {
            tentacles[index].xRot = TENTACLE_SPLAY
                    + Mth.sin(ageInTicks * RIPPLE_FREQUENCY + limbSwing + index * RIPPLE_OFFSET) * amplitude;
        }
    }
}
