package com.zelf115.soulland.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Tigers, leopards and lions: a long low body on four legs and a drooping tail. Lions add a mane.
 *
 * <p>The skin generator paints to these texture offsets; moving a box means repainting its skins.
 */
public final class BigCatModel extends QuadrupedBeastModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float STRIDE_ANGLE = 1.2F;
    private static final float TAIL_DROOP = -0.9F;
    private static final float TAIL_SWAY_FREQUENCY = 0.08F;
    private static final float TAIL_SWAY_ANGLE = 0.2F;
    private static final LegLayout LEGS = new LegLayout(3.0F, 15.0F, -8.0F, 8.0F,
            leg(32), leg(48));

    private final ModelPart tail;

    public BigCatModel(final ModelPart root) {
        super(root, root.getChild("head"), STRIDE_ANGLE);
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createCatLayer() {
        return LayerDefinition.create(createCatMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createLionLayer() {
        final MeshDefinition mesh = createCatMesh();
        mesh.getRoot().getChild("head").addOrReplaceChild("mane",
                CubeListBuilder.create().texOffs(64, 18).addBox(-6.0F, -7.0F, -3.0F, 12.0F, 13.0F, 5.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createCatMesh() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -5.0F, -11.0F, 10.0F, 9.0F, 22.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(64, 0).addBox(-4.5F, -5.0F, -8.0F, 9.0F, 9.0F, 8.0F)
                        .texOffs(98, 0).addBox(-2.5F, 0.0F, -11.0F, 5.0F, 4.0F, 3.0F)
                        .texOffs(98, 8).addBox(-4.0F, -7.0F, -4.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(98, 8).mirror().addBox(2.0F, -7.0F, -4.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 10.0F, -11.0F));
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 32).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 14.0F),
                PartPose.offsetAndRotation(0.0F, 9.0F, 11.0F, TAIL_DROOP, 0.0F, 0.0F));
        addLegs(root, LEGS);
        return mesh;
    }

    private static CubeListBuilder leg(final int textureX) {
        return CubeListBuilder.create().texOffs(textureX, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 9.0F, 4.0F);
    }

    @Override
    protected void animateBody(final float limbSwingAmount, final float ageInTicks) {
        tail.yRot = Mth.sin(ageInTicks * TAIL_SWAY_FREQUENCY) * TAIL_SWAY_ANGLE;
    }
}
