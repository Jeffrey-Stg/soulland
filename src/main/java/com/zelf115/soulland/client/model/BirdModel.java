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
 * Phoenixes and other great birds: an upright body on two thin legs, folded wings that beat harder
 * the faster the bird runs, a crest and a long trailing tail.
 *
 * <p>The skin generator paints to these texture offsets; moving a box means repainting its skins.
 */
public final class BirdModel extends HierarchicalModel<SpiritBeastEntity> {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float LEG_TOP = 17.0F;
    private static final float LEG_SPREAD = 2.0F;
    private static final float WING_TOP = 8.0F;
    private static final float WING_X = 4.0F;
    private static final float TAIL_DROOP = -0.5F;
    private static final float STRIDE_FREQUENCY = 0.6662F;
    private static final float STRIDE_ANGLE = 1.0F;
    private static final float WING_REST_ANGLE = 0.05F;
    private static final float WING_BEAT_FREQUENCY = 0.25F;
    private static final float WING_IDLE_BEAT = 0.1F;
    private static final float WING_RUNNING_BEAT = 1.0F;
    private static final float TAIL_SWAY_FREQUENCY = 0.1F;
    private static final float TAIL_SWAY_ANGLE = 0.1F;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public BirdModel(final ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.tail = root.getChild("tail");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 10.0F, 8.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F)
                        .texOffs(32, 12).addBox(-1.0F, -4.0F, -6.0F, 2.0F, 2.0F, 3.0F)
                        .texOffs(44, 12).addBox(-0.5F, -10.0F, -1.0F, 1.0F, 4.0F, 5.0F),
                PartPose.offset(0.0F, 7.0F, -2.0F));
        root.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, 0.0F, -4.0F, 1.0F, 9.0F, 8.0F),
                PartPose.offset(WING_X, WING_TOP, 0.0F));
        root.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-1.0F, 0.0F, -4.0F, 1.0F, 9.0F, 8.0F),
                PartPose.offset(-WING_X, WING_TOP, 0.0F));
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 36).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 1.0F, 12.0F),
                PartPose.offsetAndRotation(0.0F, 15.0F, 4.0F, TAIL_DROOP, 0.0F, 0.0F));
        addLeg(root, "left_leg", LEG_SPREAD);
        addLeg(root, "right_leg", -LEG_SPREAD);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static void addLeg(final PartDefinition root, final String name, final float x) {
        root.addOrReplaceChild(name, CubeListBuilder.create()
                        .texOffs(36, 36).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F)
                        .texOffs(40, 36).addBox(-1.5F, 6.0F, -2.5F, 3.0F, 1.0F, 3.0F),
                PartPose.offset(x, LEG_TOP, 0.0F));
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

        final float stride = Mth.cos(limbSwing * STRIDE_FREQUENCY) * STRIDE_ANGLE * limbSwingAmount;
        leftLeg.xRot = stride;
        rightLeg.xRot = -stride;

        final float beat = (Mth.sin(ageInTicks * WING_BEAT_FREQUENCY) + 1.0F) / 2.0F
                * (WING_IDLE_BEAT + limbSwingAmount * WING_RUNNING_BEAT);
        leftWing.zRot = -(WING_REST_ANGLE + beat);
        rightWing.zRot = WING_REST_ANGLE + beat;

        tail.yRot = Mth.sin(ageInTicks * TAIL_SWAY_FREQUENCY) * TAIL_SWAY_ANGLE;
    }
}
