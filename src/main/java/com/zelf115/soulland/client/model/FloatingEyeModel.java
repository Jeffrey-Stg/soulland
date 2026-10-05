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
 * The Evileye Tyrant: a hovering orb, one great eye in its face and a crown of eye stalks that writhe
 * while it bobs in the air. The whole body turns to stare at its target.
 *
 * <p>The skin generator paints to these texture offsets; moving a box means repainting its skins.
 */
public final class FloatingEyeModel extends HierarchicalModel<SpiritBeastEntity> {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float HOVER_Y = 9.0F;
    private static final float BODY_HALF_SIZE = 7.0F;
    private static final int STALKS = 6;
    private static final float STALK_RING_RADIUS = 4.0F;
    private static final float STALK_LEAN = 0.5F;
    private static final float BOB_FREQUENCY = 0.1F;
    private static final float BOB_HEIGHT = 1.0F;
    private static final float WRITHE_FREQUENCY = 0.15F;
    private static final float WRITHE_OFFSET = 1.1F;
    private static final float WRITHE_ANGLE = 0.2F;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart[] stalks = new ModelPart[STALKS];

    public FloatingEyeModel(final ModelPart root) {
        this.root = root;
        this.body = root.getChild("head");
        for (int index = 0; index < STALKS; index++) {
            stalks[index] = body.getChild(stalkName(index));
        }
    }

    public static LayerDefinition createBodyLayer() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition body = mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-BODY_HALF_SIZE, -BODY_HALF_SIZE, -BODY_HALF_SIZE, 14.0F, 14.0F, 14.0F),
                PartPose.offset(0.0F, HOVER_Y, 0.0F));
        final CubeListBuilder stalk = CubeListBuilder.create()
                .texOffs(0, 28).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F)
                .texOffs(4, 28).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 2.0F, 2.0F);
        for (int index = 0; index < STALKS; index++) {
            final float angle = index * Mth.TWO_PI / STALKS;
            body.addOrReplaceChild(stalkName(index), stalk, PartPose.offsetAndRotation(
                    Mth.cos(angle) * STALK_RING_RADIUS, -BODY_HALF_SIZE, Mth.sin(angle) * STALK_RING_RADIUS,
                    -Mth.sin(angle) * STALK_LEAN, 0.0F, Mth.cos(angle) * STALK_LEAN));
        }
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static String stalkName(final int index) {
        return "stalk_" + index;
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(final SpiritBeastEntity entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        body.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        body.xRot = headPitch * Mth.DEG_TO_RAD;
        body.y = HOVER_Y + Mth.sin(ageInTicks * BOB_FREQUENCY) * BOB_HEIGHT;

        for (int index = 0; index < STALKS; index++) {
            final float angle = index * Mth.TWO_PI / STALKS;
            final float writhe = Mth.sin(ageInTicks * WRITHE_FREQUENCY + index * WRITHE_OFFSET) * WRITHE_ANGLE;
            stalks[index].xRot = -Mth.sin(angle) * STALK_LEAN + writhe;
            stalks[index].zRot = Mth.cos(angle) * STALK_LEAN - writhe;
        }
    }
}
