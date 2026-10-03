package com.zelf115.soulland.block;

import com.mojang.serialization.MapCodec;
import com.zelf115.soulland.SoulLand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A wild spirit herb. Which herb it yields is decided only when it is broken, by a loot table that
 * knows the biome it grew in.
 */
public final class SpiritHerbBlock extends BushBlock {

    public static final MapCodec<SpiritHerbBlock> CODEC = simpleCodec(SpiritHerbBlock::new);
    public static final TagKey<Block> HERB_SOIL =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "herb_soil"));
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 10.0, 13.0);

    public SpiritHerbBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<SpiritHerbBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return state.is(HERB_SOIL);
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos,
                                  final CollisionContext context) {
        final Vec3 offset = state.getOffset(level, pos);
        return SHAPE.move(offset.x, offset.y, offset.z);
    }
}
