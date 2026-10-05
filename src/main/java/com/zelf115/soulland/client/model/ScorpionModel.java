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
 * Scorpions: a flat armoured body on six legs, two pincered arms held forward, and a segmented tail
 * curling over the back to a stinger that sways as if ready to strike.
 *
 * <p>The skin generator paints to these texture offsets; moving a box means repainting its skins.
 */
public final class ScorpionModel extends HierarchicalModel<SpiritBeastEntity> {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float BODY_Y = 19.0F;
    private static final float LEG_X = 5.0F;
    private static final float[] LEG_Z = {-4.0F, 0.0F, 4.0F};
    private static final float LEG_DROOP = 0.5F;
    private static final float ARM_SPREAD = 0.4F;
    private static final float PINCER_TURN_IN = 0.6F;
    private static final int TAIL_SEGMENTS = 4;
    private static final float TAIL_SEGMENT_LENGTH = 6.0F;
    private static final float TAIL_BASE_CURL = 0.9F;
    private static final float TAIL_CURL = 0.6F;
    private static final float GAIT_FREQUENCY = 0.6662F;
    private static final float GAIT_SWING = 0.4F;
    private static final float PINCER_SNAP_FREQUENCY = 0.15F;
    private static final float PINCER_SNAP_ANGLE = 0.15F;
    private static final float TAIL_SWAY_FREQUENCY = 0.1F;
    private static final float TAIL_SWAY_ANGLE = 0.15F;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart leftPincer;
    private final ModelPart rightPincer;
    private final ModelPart[] leftLegs = new ModelPart[LEG_Z.length];
    private final ModelPart[] rightLegs = new ModelPart[LEG_Z.length];

    public ScorpionModel(final ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.tail = root.getChild(tailSegmentName(0));
        this.leftPincer = root.getChild("left_arm").getChild("pincer");
        this.rightPincer = root.getChild("right_arm").getChild("pincer");
        for (int index = 0; index < LEG_Z.length; index++) {
            leftLegs[index] = root.getChild(legName("left", index));
            rightLegs[index] = root.getChild(legName("right", index));
        }
    }

    public static LayerDefinition createBodyLayer() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -3.0F, -8.0F, 10.0F, 5.0F, 16.0F),
                PartPose.offset(0.0F, BODY_Y, 0.0F));
        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(52, 0).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 4.0F, 5.0F),
                PartPose.offset(0.0F, BODY_Y, -8.0F));
        addArm(root, "left_arm", 1.0F);
        addArm(root, "right_arm", -1.0F);
        addLegs(root);
        addTail(root);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    /** {@code side} is +1 for the left arm and -1 for the right, mirroring the angles across the spine. */
    private static void addArm(final PartDefinition root, final String name, final float side) {
        root.addOrReplaceChild(name,
                        CubeListBuilder.create().texOffs(78, 0).addBox(-1.5F, -1.5F, -8.0F, 3.0F, 3.0F, 8.0F),
                        PartPose.offsetAndRotation(side * 5.0F, BODY_Y - 1.0F, -6.0F, 0.0F, -side * ARM_SPREAD, 0.0F))
                .addOrReplaceChild("pincer",
                        CubeListBuilder.create().texOffs(100, 0).addBox(-2.5F, -2.0F, -6.0F, 5.0F, 4.0F, 6.0F),
                        PartPose.offsetAndRotation(0.0F, 0.0F, -8.0F, 0.0F, side * PINCER_TURN_IN, 0.0F));
    }

    private static void addLegs(final PartDefinition root) {
        final CubeListBuilder leftLeg = CubeListBuilder.create().texOffs(52, 9).addBox(-1.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F);
        final CubeListBuilder rightLeg = CubeListBuilder.create().texOffs(52, 9).mirror().addBox(-9.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F);
        for (int index = 0; index < LEG_Z.length; index++) {
            root.addOrReplaceChild(legName("left", index), leftLeg,
                    PartPose.offsetAndRotation(LEG_X, BODY_Y, LEG_Z[index], 0.0F, 0.0F, LEG_DROOP));
            root.addOrReplaceChild(legName("right", index), rightLeg,
                    PartPose.offsetAndRotation(-LEG_X, BODY_Y, LEG_Z[index], 0.0F, 0.0F, -LEG_DROOP));
        }
    }

    /** Each tail segment hangs off the one before and curls a little further, ending in the stinger. */
    private static void addTail(final PartDefinition root) {
        final CubeListBuilder segment = CubeListBuilder.create().texOffs(0, 21).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, TAIL_SEGMENT_LENGTH);
        PartDefinition parent = root.addOrReplaceChild(tailSegmentName(0), segment,
                PartPose.offsetAndRotation(0.0F, BODY_Y - 2.0F, 8.0F, TAIL_BASE_CURL, 0.0F, 0.0F));
        for (int index = 1; index < TAIL_SEGMENTS; index++) {
            parent = parent.addOrReplaceChild(tailSegmentName(index), segment,
                    PartPose.offsetAndRotation(0.0F, 0.0F, TAIL_SEGMENT_LENGTH, TAIL_CURL, 0.0F, 0.0F));
        }
        parent.addOrReplaceChild("stinger",
                CubeListBuilder.create().texOffs(18, 21).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, TAIL_SEGMENT_LENGTH, TAIL_CURL, 0.0F, 0.0F));
    }

    private static String legName(final String side, final int index) {
        return side + "_leg_" + index;
    }

    private static String tailSegmentName(final int index) {
        return "tail_" + index;
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(final SpiritBeastEntity entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;

        for (int index = 0; index < LEG_Z.length; index++) {
            final float swing = Mth.cos(limbSwing * GAIT_FREQUENCY + index * Mth.HALF_PI) * GAIT_SWING * limbSwingAmount;
            leftLegs[index].yRot = swing;
            rightLegs[index].yRot = swing;
        }

        final float snap = Mth.sin(ageInTicks * PINCER_SNAP_FREQUENCY) * PINCER_SNAP_ANGLE;
        leftPincer.yRot = PINCER_TURN_IN + snap;
        rightPincer.yRot = -PINCER_TURN_IN - snap;

        tail.yRot = Mth.sin(ageInTicks * TAIL_SWAY_FREQUENCY) * TAIL_SWAY_ANGLE;
    }
}
