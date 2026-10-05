package com.zelf115.soulland.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Dragons: a four-legged body, a neck rising to a horned head, folded wings that open and beat as
 * the dragon runs, and a two-part tail that sways.
 *
 * <p>The skin generator paints to these texture offsets; moving a box means repainting its skins.
 */
public final class DragonModel extends QuadrupedBeastModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float STRIDE_ANGLE = 1.0F;
    private static final float NECK_RISE = -0.5F;
    private static final float TAIL_DROOP = -0.3F;
    private static final float TAIL_TIP_DROOP = -0.2F;
    private static final float WING_FOLD = 0.8F;
    private static final float WING_BEAT_FREQUENCY = 0.2F;
    private static final float WING_IDLE_BEAT = 0.05F;
    private static final float WING_RUNNING_BEAT = 0.8F;
    private static final float TAIL_SWAY_FREQUENCY = 0.1F;
    private static final float TAIL_SWAY_ANGLE = 0.25F;
    private static final LegLayout LEGS = new LegLayout(4.0F, 15.0F, -7.0F, 7.0F, leg(), leg());

    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;
    private final ModelPart tailTip;

    public DragonModel(final ModelPart root) {
        super(root, root.getChild("neck").getChild("head_mount").getChild("head"), STRIDE_ANGLE);
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.tail = root.getChild("tail");
        this.tailTip = tail.getChild("tail_tip");
    }

    public static LayerDefinition createBodyLayer() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.0F, -10.0F, 12.0F, 10.0F, 20.0F),
                PartPose.offset(0.0F, 10.0F, 0.0F));
        addNeckAndHead(root);
        root.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(64, 0).addBox(0.0F, -1.0F, 0.0F, 16.0F, 1.0F, 12.0F),
                PartPose.offsetAndRotation(6.0F, 6.0F, -4.0F, 0.0F, 0.0F, -WING_FOLD));
        root.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-16.0F, -1.0F, 0.0F, 16.0F, 1.0F, 12.0F),
                PartPose.offsetAndRotation(-6.0F, 6.0F, -4.0F, 0.0F, 0.0F, WING_FOLD));
        root.addOrReplaceChild("tail",
                        CubeListBuilder.create().texOffs(26, 30).addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 10.0F),
                        PartPose.offsetAndRotation(0.0F, 8.0F, 10.0F, TAIL_DROOP, 0.0F, 0.0F))
                .addOrReplaceChild("tail_tip",
                        CubeListBuilder.create().texOffs(56, 30).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 10.0F, TAIL_TIP_DROOP, 0.0F, 0.0F));
        addLegs(root, LEGS);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    /** The head rides the neck's tip. Its mount levels it back out, leaving the head itself free to look around. */
    private static void addNeckAndHead(final PartDefinition root) {
        final PartDefinition neck = root.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 30).addBox(-2.5F, -2.5F, -8.0F, 5.0F, 5.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 8.0F, -10.0F, NECK_RISE, 0.0F, 0.0F));
        final PartDefinition mount = neck.addOrReplaceChild("head_mount", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 0.0F, -8.0F, -NECK_RISE, 0.0F, 0.0F));
        mount.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(64, 13).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 7.0F, 8.0F)
                        .texOffs(96, 13).addBox(-3.0F, -1.0F, -13.0F, 6.0F, 4.0F, 5.0F)
                        .texOffs(118, 13).addBox(-3.5F, -8.0F, -2.0F, 2.0F, 5.0F, 2.0F)
                        .texOffs(118, 13).mirror().addBox(1.5F, -8.0F, -2.0F, 2.0F, 5.0F, 2.0F),
                PartPose.ZERO);
    }

    private static CubeListBuilder leg() {
        return CubeListBuilder.create().texOffs(82, 30).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F);
    }

    @Override
    protected void animateBody(final float limbSwingAmount, final float ageInTicks) {
        final float beat = (Mth.sin(ageInTicks * WING_BEAT_FREQUENCY) + 1.0F) / 2.0F
                * (WING_IDLE_BEAT + limbSwingAmount * WING_RUNNING_BEAT);
        leftWing.zRot = -(WING_FOLD + beat);
        rightWing.zRot = WING_FOLD + beat;

        final float sway = Mth.sin(ageInTicks * TAIL_SWAY_FREQUENCY) * TAIL_SWAY_ANGLE;
        tail.yRot = sway;
        tailTip.yRot = sway;
    }
}
