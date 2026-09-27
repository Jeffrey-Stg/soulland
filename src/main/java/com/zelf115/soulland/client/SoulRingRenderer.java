package com.zelf115.soulland.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.joml.Matrix4f;

/** Draws a player's absorbed soul rings as glowing halos stacked around them. */
@EventBusSubscriber(modid = SoulLand.MODID, value = Dist.CLIENT)
public final class SoulRingRenderer {

    private static final int HALO_BUFFER_BYTES = 1536;
    /** Translucent, two-sided and depth-read-only, so stacked halos blend instead of clipping each other. */
    private static final RenderType HALO = RenderType.create(
            "soulland:soul_ring_halo",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            HALO_BUFFER_BYTES,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    private static final int RING_SEGMENTS = 32;
    private static final float RING_INNER_RADIUS = 0.55F;
    private static final float RING_OUTER_RADIUS = 0.75F;
    private static final float RING_BASE_HEIGHT = 0.15F;
    private static final float RING_SPACING = 0.32F;

    private static final float SPIN_DEGREES_PER_TICK = 1.5F;
    private static final float SPIN_PHASE_DEGREES_PER_RING = 25.0F;
    private static final float BOB_AMPLITUDE = 0.05F;
    private static final float BOB_PERIOD_TICKS = 70.0F;
    private static final float BOB_PHASE_RADIANS_PER_RING = 0.6F;
    private static final float FULL_TURN_RADIANS = (float) Math.TAU;

    /** The tier colour table carries no alpha of its own, so the halo supplies one. */
    private static final int RING_ALPHA_MASK = 0xAA000000;

    private SoulRingRenderer() {
    }

    @SubscribeEvent
    public static void onRenderPlayer(final RenderPlayerEvent.Post event) {
        final Player player = event.getEntity();
        final List<Integer> ringTiers = RingDisplayClientData.ringsFor(player.getUUID());
        if (ringTiers.isEmpty()) {
            return;
        }

        final Player viewer = Minecraft.getInstance().player;
        if (viewer == null || player.isInvisibleTo(viewer)) {
            return;
        }

        renderRingStack(event.getPoseStack(), event.getMultiBufferSource(), ringTiers,
                player.tickCount + event.getPartialTick());
    }

    private static void renderRingStack(final PoseStack poseStack, final MultiBufferSource buffers,
                                        final List<Integer> ringTiers, final float time) {
        final VertexConsumer consumer = buffers.getBuffer(HALO);
        for (int index = 0; index < ringTiers.size(); index++) {
            poseStack.pushPose();
            poseStack.translate(0.0F, ringHeight(index, time), 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(ringSpin(index, time)));
            renderRing(poseStack, consumer, SpiritBeastManager.tierTextColor(ringTiers.get(index)));
            poseStack.popPose();
        }
    }

    private static float ringHeight(final int index, final float time) {
        final float bobPhase = time / BOB_PERIOD_TICKS * FULL_TURN_RADIANS + index * BOB_PHASE_RADIANS_PER_RING;
        return RING_BASE_HEIGHT + index * RING_SPACING + (float) Math.sin(bobPhase) * BOB_AMPLITUDE;
    }

    private static float ringSpin(final int index, final float time) {
        return time * SPIN_DEGREES_PER_TICK + index * SPIN_PHASE_DEGREES_PER_RING;
    }

    private static void renderRing(final PoseStack poseStack, final VertexConsumer consumer, final int tierColor) {
        final Matrix4f pose = poseStack.last().pose();
        final int color = RING_ALPHA_MASK | tierColor;
        final double segmentAngle = Math.TAU / RING_SEGMENTS;

        for (int segment = 0; segment < RING_SEGMENTS; segment++) {
            final double start = segmentAngle * segment;
            final double end = start + segmentAngle;
            addSegmentVertex(consumer, pose, start, RING_OUTER_RADIUS, color);
            addSegmentVertex(consumer, pose, end, RING_OUTER_RADIUS, color);
            addSegmentVertex(consumer, pose, end, RING_INNER_RADIUS, color);
            addSegmentVertex(consumer, pose, start, RING_INNER_RADIUS, color);
        }
    }

    private static void addSegmentVertex(final VertexConsumer consumer, final Matrix4f pose, final double angle,
                                         final float radius, final int color) {
        consumer.addVertex(pose, (float) Math.cos(angle) * radius, 0.0F, (float) Math.sin(angle) * radius)
                .setColor(color);
    }
}
