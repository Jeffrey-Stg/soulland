package com.zelf115.soulland.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.util.RandomSource;

import java.util.function.Supplier;

public final class CrystalBuddingBlock extends BuddingAmethystBlock {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final Supplier<? extends Block> smallBud;
    private final Supplier<? extends Block> mediumBud;
    private final Supplier<? extends Block> largeBud;
    private final Supplier<? extends Block> cluster;

    public CrystalBuddingBlock(Properties properties, Supplier<? extends Block> smallBud,
            Supplier<? extends Block> mediumBud, Supplier<? extends Block> largeBud,
            Supplier<? extends Block> cluster) {
        super(properties);
        this.smallBud = smallBud;
        this.mediumBud = mediumBud;
        this.largeBud = largeBud;
        this.cluster = cluster;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) {
            return;
        }

        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);
        Block nextStage = null;

        if (canClusterGrowAtState(targetState)) {
            nextStage = smallBud.get();
        } else if (targetState.is(smallBud.get()) && growsInDirection(targetState, direction)) {
            nextStage = mediumBud.get();
        } else if (targetState.is(mediumBud.get()) && growsInDirection(targetState, direction)) {
            nextStage = largeBud.get();
        } else if (targetState.is(largeBud.get()) && growsInDirection(targetState, direction)) {
            nextStage = cluster.get();
        }

        if (nextStage != null) {
            BlockState nextState = nextStage.defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, direction)
                    .setValue(AmethystClusterBlock.WATERLOGGED,
                            targetState.getFluidState().getType() == Fluids.WATER);
            level.setBlockAndUpdate(targetPos, nextState);
        }
    }

    private static boolean growsInDirection(BlockState state, Direction direction) {
        return state.hasProperty(AmethystClusterBlock.FACING)
                && state.getValue(AmethystClusterBlock.FACING) == direction;
    }
}
