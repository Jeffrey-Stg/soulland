package com.zelf115.soulland.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

/** A spike of rock rising out of the ground, widest at its base and tapering to a point. */
public class RockSpikeFeature extends Feature<BlockStateConfiguration> {

    private static final int MIN_HEIGHT = 4;
    private static final int MAX_HEIGHT = 11;
    private static final int BASE_RADIUS = 2;

    public RockSpikeFeature(final Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(final FeaturePlaceContext<BlockStateConfiguration> context) {
        final WorldGenLevel level = context.level();
        final BlockPos base = context.origin();
        final BlockPos ground = base.below();
        if (!level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) {
            return false;
        }

        final RandomSource random = context.random();
        final int height = Mth.nextInt(random, MIN_HEIGHT, MAX_HEIGHT);
        final BlockState rock = context.config().state;
        for (int layer = 0; layer < height; layer++) {
            placeLayer(level, base.above(layer), radiusAt(layer, height), rock);
        }
        return true;
    }

    private static int radiusAt(final int layer, final int height) {
        return Math.round(BASE_RADIUS * (1.0F - (float) layer / (float) height));
    }

    private static void placeLayer(final WorldGenLevel level, final BlockPos center, final int radius, final BlockState rock) {
        for (int offsetX = -radius; offsetX <= radius; offsetX++) {
            for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                if (offsetX * offsetX + offsetZ * offsetZ > radius * radius) {
                    continue;
                }

                final BlockPos pos = center.offset(offsetX, 0, offsetZ);
                if (level.getBlockState(pos).canBeReplaced()) {
                    level.setBlock(pos, rock, Block.UPDATE_CLIENTS);
                }
            }
        }
    }
}
