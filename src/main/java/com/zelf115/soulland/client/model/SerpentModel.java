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
 * Snakes, worms and pythons: a head and a chain of tapering segments lying along the ground.
 * Each segment hangs off the one before, so a small turn per segment adds up to a slither.
 *
 * <p>The skin generator paints to these texture offsets; moving a box means repainting its skins.
 */
public final class SerpentModel extends HierarchicalModel<SpiritBeastEntity> {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float GROUND = 24.0F;
    private static final float FRONT_Z = -15.0F;
    private static final float SEGMENT_LENGTH = 6.0F;
    private static final float[] SEGMENT_SIZES = {6.0F, 6.0F, 6.0F, 5.0F, 4.0F, 3.0F};
    private static final int[][] SEGMENT_TEXTURE_OFFSETS = {{0, 13}, {24, 13}, {0, 25}, {24, 25}, {0, 37}, {24, 37}};
    private static final float SLITHER_STRIDE_FREQUENCY = 0.6F;
    private static final float SLITHER_IDLE_FREQUENCY = 0.05F;
    private static final float SLITHER_WAVE_OFFSET = 0.9F;
    private static final float SLITHER_IDLE_ANGLE = 0.08F;
    private static final float SLITHER_MOVING_ANGLE = 0.35F;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart[] segments = new ModelPart[SEGMENT_SIZES.length];

    public SerpentModel(final ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        ModelPart parent = root;
        for (int index = 0; index < segments.length; index++) {
            segments[index] = parent.getChild(segmentName(index));
            parent = segments[index];
        }
    }

    public static LayerDefinition createBodyLayer() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -6.0F, -7.0F, 7.0F, 6.0F, 7.0F)
                        .texOffs(28, 0).addBox(-0.5F, -1.0F, -10.0F, 1.0F, 0.0F, 3.0F),
                PartPose.offset(0.0F, GROUND, FRONT_Z));
        PartDefinition parent = root;
        PartPose pose = PartPose.offset(0.0F, GROUND, FRONT_Z);
        for (int index = 0; index < SEGMENT_SIZES.length; index++) {
            parent = parent.addOrReplaceChild(segmentName(index), segmentCube(index), pose);
            pose = PartPose.offset(0.0F, 0.0F, SEGMENT_LENGTH);
        }
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static CubeListBuilder segmentCube(final int index) {
        final float size = SEGMENT_SIZES[index];
        final int[] textureOffset = SEGMENT_TEXTURE_OFFSETS[index];
        return CubeListBuilder.create().texOffs(textureOffset[0], textureOffset[1])
                .addBox(-size / 2.0F, -size, 0.0F, size, size, SEGMENT_LENGTH);
    }

    private static String segmentName(final int index) {
        return "segment_" + index;
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

        final float phase = limbSwing * SLITHER_STRIDE_FREQUENCY + ageInTicks * SLITHER_IDLE_FREQUENCY;
        final float angle = SLITHER_IDLE_ANGLE + limbSwingAmount * SLITHER_MOVING_ANGLE;
        for (int index = 0; index < segments.length; index++) {
            segments[index].yRot = Mth.sin(phase - index * SLITHER_WAVE_OFFSET) * angle;
        }
    }
}
