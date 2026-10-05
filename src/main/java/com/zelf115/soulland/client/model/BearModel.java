package com.zelf115.soulland.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Bears: a heavy hump-backed body on thick legs, a broad head with round ears and a stub of a tail.
 *
 * <p>The skin generator paints to these texture offsets; moving a box means repainting its skins.
 */
public final class BearModel extends QuadrupedBeastModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float STRIDE_ANGLE = 0.8F;
    private static final LegLayout LEGS = new LegLayout(4.0F, 14.0F, -8.0F, 8.0F, leg(0), leg(24));

    public BearModel(final ModelPart root) {
        super(root, root.getChild("head"), STRIDE_ANGLE);
    }

    public static LayerDefinition createBodyLayer() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7.0F, -7.0F, -12.0F, 14.0F, 13.0F, 24.0F)
                        .texOffs(48, 37).addBox(-1.5F, -5.5F, 12.0F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 10.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(76, 0).addBox(-5.0F, -5.0F, -8.0F, 10.0F, 9.0F, 8.0F)
                        .texOffs(76, 17).addBox(-3.0F, 0.0F, -11.0F, 6.0F, 4.0F, 3.0F)
                        .texOffs(94, 17).addBox(-5.0F, -7.0F, -4.0F, 3.0F, 2.0F, 1.0F)
                        .texOffs(94, 17).mirror().addBox(2.0F, -7.0F, -4.0F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 8.0F, -12.0F));
        addLegs(root, LEGS);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static CubeListBuilder leg(final int textureX) {
        return CubeListBuilder.create().texOffs(textureX, 37).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 10.0F, 6.0F);
    }
}
