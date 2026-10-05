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
 * Sharks and orcas: a streamlined body with a dorsal fin, side fins and a tail that drives it.
 * A shark's tail fin stands upright and beats side to side; an orca's flukes lie flat and beat up and down.
 *
 * <p>The skin generator paints to these texture offsets; moving a box means repainting its skins.
 */
public final class SeaBeastModel extends HierarchicalModel<SpiritBeastEntity> {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float BODY_Y = 17.0F;
    private static final float FIN_DROOP = 0.4F;
    private static final float STROKE_FREQUENCY = 0.3F;
    private static final float STROKE_IDLE_ANGLE = 0.1F;
    private static final float STROKE_MOVING_ANGLE = 0.4F;

    /** Which way the tail beats. */
    public enum TailStroke { SIDEWAYS, VERTICAL }

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart tailFin;
    private final TailStroke stroke;

    public SeaBeastModel(final ModelPart root, final TailStroke stroke) {
        this.root = root;
        this.head = root.getChild("head");
        this.tail = root.getChild("tail");
        this.tailFin = tail.getChild("tail_fin");
        this.stroke = stroke;
    }

    public static LayerDefinition createSharkLayer() {
        final MeshDefinition mesh = createBodyMesh();
        mesh.getRoot().getChild("tail").addOrReplaceChild("tail_fin",
                CubeListBuilder.create().texOffs(50, 12).addBox(-0.5F, -7.0F, 0.0F, 1.0F, 14.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 8.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createOrcaLayer() {
        final MeshDefinition mesh = createBodyMesh();
        mesh.getRoot().getChild("tail").addOrReplaceChild("tail_fin",
                CubeListBuilder.create().texOffs(26, 41).addBox(-6.0F, -0.5F, 0.0F, 12.0F, 1.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 8.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createBodyMesh() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -5.0F, -8.0F, 9.0F, 9.0F, 16.0F)
                        .texOffs(50, 0).addBox(-0.5F, -11.0F, -2.0F, 1.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, BODY_Y, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 25).addBox(-4.0F, -4.5F, -8.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 25).addBox(-3.0F, -3.0F, -11.0F, 6.0F, 5.0F, 3.0F),
                PartPose.offset(0.0F, BODY_Y, -8.0F));
        root.addOrReplaceChild("left_fin",
                CubeListBuilder.create().texOffs(32, 33).addBox(0.0F, 0.0F, 0.0F, 6.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(4.5F, BODY_Y + 3.0F, -4.0F, 0.0F, 0.0F, FIN_DROOP));
        root.addOrReplaceChild("right_fin",
                CubeListBuilder.create().texOffs(32, 33).mirror().addBox(-6.0F, 0.0F, 0.0F, 6.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(-4.5F, BODY_Y + 3.0F, -4.0F, 0.0F, 0.0F, -FIN_DROOP));
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 41).addBox(-2.5F, -3.0F, 0.0F, 5.0F, 6.0F, 8.0F),
                PartPose.offset(0.0F, BODY_Y - 1.0F, 8.0F));
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

        final float beat = Mth.sin(ageInTicks * STROKE_FREQUENCY)
                * (STROKE_IDLE_ANGLE + limbSwingAmount * STROKE_MOVING_ANGLE);
        if (stroke == TailStroke.SIDEWAYS) {
            tail.yRot = beat;
            tailFin.yRot = beat;
        } else {
            tail.xRot = beat;
            tailFin.xRot = beat;
        }
    }
}
