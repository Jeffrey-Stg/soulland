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
 * Apes and giants: a hunched torso on short legs, arms long enough to knuckle-walk, and a heavy
 * head with a jutting muzzle. Titans add a pair of horns.
 *
 * <p>The skin generator paints to these texture offsets; moving a box means repainting its skins.
 */
public final class ApeModel extends HierarchicalModel<SpiritBeastEntity> {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float HUNCH = 0.25F;
    private static final float SHOULDER_X = 9.0F;
    private static final float HIP_X = 3.5F;
    private static final float STRIDE_FREQUENCY = 0.6662F;
    private static final float LEG_STRIDE_ANGLE = 1.0F;
    private static final float ARM_STRIDE_ANGLE = 0.8F;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public ApeModel(final ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createApeLayer() {
        return LayerDefinition.create(createApeMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createHornedLayer() {
        final MeshDefinition mesh = createApeMesh();
        mesh.getRoot().getChild("head").addOrReplaceChild("horns", CubeListBuilder.create()
                        .texOffs(82, 7).addBox(-4.5F, -12.0F, -2.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(82, 7).mirror().addBox(2.5F, -12.0F, -2.0F, 2.0F, 4.0F, 2.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createApeMesh() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -16.0F, -4.5F, 14.0F, 16.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, HUNCH, 0.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(46, 0).addBox(-4.5F, -8.0F, -4.5F, 9.0F, 9.0F, 9.0F)
                        .texOffs(82, 0).addBox(-3.0F, -3.0F, -7.5F, 6.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, -1.0F, -5.0F));
        final CubeListBuilder arm = CubeListBuilder.create().texOffs(0, 25)
                .addBox(-2.5F, -2.0F, -2.5F, 5.0F, 22.0F, 5.0F);
        root.addOrReplaceChild("left_arm", arm, PartPose.offset(SHOULDER_X, 1.0F, -3.0F));
        root.addOrReplaceChild("right_arm", arm, PartPose.offset(-SHOULDER_X, 1.0F, -3.0F));
        final CubeListBuilder leg = CubeListBuilder.create().texOffs(20, 25)
                .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F);
        root.addOrReplaceChild("left_leg", leg, PartPose.offset(HIP_X, 14.0F, 0.0F));
        root.addOrReplaceChild("right_leg", leg, PartPose.offset(-HIP_X, 14.0F, 0.0F));
        return mesh;
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

        final float swing = Mth.cos(limbSwing * STRIDE_FREQUENCY) * limbSwingAmount;
        leftLeg.xRot = swing * LEG_STRIDE_ANGLE;
        rightLeg.xRot = -swing * LEG_STRIDE_ANGLE;
        leftArm.xRot = -swing * ARM_STRIDE_ANGLE;
        rightArm.xRot = swing * ARM_STRIDE_ANGLE;
    }
}
