package com.zelf115.soulland.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Hippos and porcupines: a barrel body on stubby legs and a broad head with a wide muzzle.
 * The porcupine adds rows of quills swept back along its spine.
 *
 * <p>The skin generator paints to these texture offsets; moving a box means repainting its skins.
 */
public final class HeavyBeastModel extends QuadrupedBeastModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float STRIDE_ANGLE = 0.6F;
    private static final LegLayout LEGS = new LegLayout(4.5F, 18.0F, -7.0F, 8.0F, leg(0), leg(24));
    private static final float QUILL_SWEEP = 0.7F;
    private static final float[] QUILL_X = {-5.0F, -2.0F, 1.0F, 4.0F};
    private static final float[] QUILL_Z = {-8.0F, -3.0F, 2.0F, 7.0F};

    public HeavyBeastModel(final ModelPart root) {
        super(root, root.getChild("head"), STRIDE_ANGLE);
    }

    public static LayerDefinition createHippoLayer() {
        return LayerDefinition.create(createHeavyMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createPorcupineLayer() {
        final MeshDefinition mesh = createHeavyMesh();
        final PartDefinition body = mesh.getRoot().getChild("body");
        for (int row = 0; row < QUILL_Z.length; row++) {
            for (int column = 0; column < QUILL_X.length; column++) {
                body.addOrReplaceChild("quill_" + row + "_" + column,
                        CubeListBuilder.create().texOffs(52, 34).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                        PartPose.offsetAndRotation(QUILL_X[column], -6.0F, QUILL_Z[row], -QUILL_SWEEP, 0.0F, 0.0F));
            }
        }
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createHeavyMesh() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7.0F, -6.0F, -11.0F, 14.0F, 12.0F, 22.0F)
                        .texOffs(48, 34).addBox(-0.5F, -3.0F, 11.0F, 1.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(72, 0).addBox(-5.5F, -5.0F, -7.0F, 11.0F, 9.0F, 7.0F)
                        .texOffs(72, 16).addBox(-6.0F, -1.0F, -11.0F, 12.0F, 6.0F, 4.0F)
                        .texOffs(108, 0).addBox(-5.0F, -7.0F, -2.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(108, 0).mirror().addBox(3.0F, -7.0F, -2.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 11.0F, -11.0F));
        addLegs(root, LEGS);
        return mesh;
    }

    private static CubeListBuilder leg(final int textureX) {
        return CubeListBuilder.create().texOffs(textureX, 34).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F);
    }
}
