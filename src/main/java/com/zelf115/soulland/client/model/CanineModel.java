package com.zelf115.soulland.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Wolves and hounds: a lean body on long legs, pointed ears, a long muzzle and a bushy tail that
 * wags harder as the beast runs.
 *
 * <p>The skin generator paints to these texture offsets; moving a box means repainting its skins.
 */
public final class CanineModel extends QuadrupedBeastModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float STRIDE_ANGLE = 1.3F;
    private static final float TAIL_DROOP = -0.6F;
    private static final float TAIL_WAG_FREQUENCY = 0.2F;
    private static final float TAIL_IDLE_WAG = 0.1F;
    private static final float TAIL_RUNNING_WAG = 0.5F;
    private static final LegLayout LEGS = new LegLayout(2.5F, 14.0F, -7.0F, 7.0F, leg(26), leg(38));

    private final ModelPart tail;

    public CanineModel(final ModelPart root) {
        super(root, root.getChild("head"), STRIDE_ANGLE);
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -9.0F, 8.0F, 8.0F, 18.0F),
                PartPose.offset(0.0F, 11.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(52, 0).addBox(-3.5F, -4.0F, -7.0F, 7.0F, 7.0F, 7.0F)
                        .texOffs(80, 0).addBox(-2.0F, 0.0F, -11.0F, 4.0F, 3.0F, 4.0F)
                        .texOffs(96, 0).addBox(-3.5F, -7.0F, -3.0F, 2.0F, 3.0F, 1.0F)
                        .texOffs(96, 0).mirror().addBox(1.5F, -7.0F, -3.0F, 2.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 9.0F, -9.0F));
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 26).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 8.0F, 9.0F, TAIL_DROOP, 0.0F, 0.0F));
        addLegs(root, LEGS);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static CubeListBuilder leg(final int textureX) {
        return CubeListBuilder.create().texOffs(textureX, 26).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F);
    }

    @Override
    protected void animateBody(final float limbSwingAmount, final float ageInTicks) {
        tail.yRot = Mth.sin(ageInTicks * TAIL_WAG_FREQUENCY) * (TAIL_IDLE_WAG + limbSwingAmount * TAIL_RUNNING_WAG);
    }
}
