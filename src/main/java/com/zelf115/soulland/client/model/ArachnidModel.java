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
 * Spiders: a fanged head, a small thorax and a swollen abdomen on eight splayed legs that scuttle
 * in the vanilla spider's gait.
 *
 * <p>The skin generator paints to these texture offsets; moving a box means repainting its skins.
 */
public final class ArachnidModel extends HierarchicalModel<SpiritBeastEntity> {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float BODY_Y = 15.0F;
    private static final float LEG_X = 4.0F;
    private static final float[] LEG_Z = {2.0F, 1.0F, 0.0F, -1.0F};
    /** Per leg pair, back to front: how far the legs droop toward the ground and fan along the body. */
    private static final float[] LEG_DROOP = {Mth.HALF_PI / 2.0F, 0.58F, 0.58F, Mth.HALF_PI / 2.0F};
    private static final float[] LEG_FAN = {Mth.HALF_PI / 2.0F, 0.3927F, -0.3927F, -Mth.HALF_PI / 2.0F};
    private static final float[] GAIT_PHASE = {0.0F, Mth.PI, Mth.HALF_PI, Mth.PI * 1.5F};
    private static final float GAIT_FREQUENCY = 0.6662F;
    private static final float GAIT_SWING = 0.4F;
    private static final float GAIT_LIFT = 0.4F;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart[] leftLegs = new ModelPart[LEG_Z.length];
    private final ModelPart[] rightLegs = new ModelPart[LEG_Z.length];

    public ArachnidModel(final ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        for (int pair = 0; pair < LEG_Z.length; pair++) {
            leftLegs[pair] = root.getChild(legName("left", pair));
            rightLegs[pair] = root.getChild(legName("right", pair));
        }
    }

    public static LayerDefinition createBodyLayer() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, BODY_Y, -3.0F));
        root.addOrReplaceChild("thorax",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, BODY_Y, 0.0F));
        root.addOrReplaceChild("abdomen",
                CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F),
                PartPose.offset(0.0F, BODY_Y, 9.0F));
        final CubeListBuilder leftLeg = CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
        final CubeListBuilder rightLeg = CubeListBuilder.create().texOffs(0, 36).mirror().addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
        for (int pair = 0; pair < LEG_Z.length; pair++) {
            root.addOrReplaceChild(legName("left", pair), leftLeg, PartPose.offset(LEG_X, BODY_Y, LEG_Z[pair]));
            root.addOrReplaceChild(legName("right", pair), rightLeg, PartPose.offset(-LEG_X, BODY_Y, LEG_Z[pair]));
        }
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static String legName(final String side, final int pair) {
        return side + "_leg_" + pair;
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(final SpiritBeastEntity entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;

        for (int pair = 0; pair < LEG_Z.length; pair++) {
            final float swing = -Mth.cos(limbSwing * GAIT_FREQUENCY * 2.0F + GAIT_PHASE[pair]) * GAIT_SWING * limbSwingAmount;
            final float lift = Math.abs(Mth.sin(limbSwing * GAIT_FREQUENCY + GAIT_PHASE[pair]) * GAIT_LIFT) * limbSwingAmount;
            leftLegs[pair].zRot = LEG_DROOP[pair] - lift;
            rightLegs[pair].zRot = -LEG_DROOP[pair] + lift;
            leftLegs[pair].yRot = -LEG_FAN[pair] - swing;
            rightLegs[pair].yRot = LEG_FAN[pair] + swing;
        }
    }
}
