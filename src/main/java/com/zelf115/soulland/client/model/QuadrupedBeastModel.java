package com.zelf115.soulland.client.model;

import com.zelf115.soulland.spirit.SpiritBeastEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** A four-legged beast: the head follows its gaze and the legs step in diagonal pairs. */
public abstract class QuadrupedBeastModel extends HierarchicalModel<SpiritBeastEntity> {
    private static final float STRIDE_FREQUENCY = 0.6662F;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart backLeftLeg;
    private final ModelPart backRightLeg;
    private final float strideAngle;

    protected QuadrupedBeastModel(final ModelPart root, final ModelPart head, final float strideAngle) {
        this.root = root;
        this.head = head;
        this.frontLeftLeg = root.getChild("front_left_leg");
        this.frontRightLeg = root.getChild("front_right_leg");
        this.backLeftLeg = root.getChild("back_left_leg");
        this.backRightLeg = root.getChild("back_right_leg");
        this.strideAngle = strideAngle;
    }

    /** Where the four legs stand. The same cubes serve a left and a right leg. */
    protected record LegLayout(float spread, float top, float frontZ, float backZ,
                               CubeListBuilder frontLeg, CubeListBuilder backLeg) {
    }

    protected static void addLegs(final PartDefinition root, final LegLayout layout) {
        root.addOrReplaceChild("front_left_leg", layout.frontLeg(), PartPose.offset(layout.spread(), layout.top(), layout.frontZ()));
        root.addOrReplaceChild("front_right_leg", layout.frontLeg(), PartPose.offset(-layout.spread(), layout.top(), layout.frontZ()));
        root.addOrReplaceChild("back_left_leg", layout.backLeg(), PartPose.offset(layout.spread(), layout.top(), layout.backZ()));
        root.addOrReplaceChild("back_right_leg", layout.backLeg(), PartPose.offset(-layout.spread(), layout.top(), layout.backZ()));
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public final void setupAnim(final SpiritBeastEntity entity, final float limbSwing, final float limbSwingAmount,
                                final float ageInTicks, final float netHeadYaw, final float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;

        final float stride = Mth.cos(limbSwing * STRIDE_FREQUENCY) * strideAngle * limbSwingAmount;
        frontLeftLeg.xRot = stride;
        backRightLeg.xRot = stride;
        frontRightLeg.xRot = -stride;
        backLeftLeg.xRot = -stride;

        animateBody(limbSwingAmount, ageInTicks);
    }

    /** Moves whatever the beast has beyond head and legs, such as a tail. */
    protected void animateBody(final float limbSwingAmount, final float ageInTicks) {
    }
}
