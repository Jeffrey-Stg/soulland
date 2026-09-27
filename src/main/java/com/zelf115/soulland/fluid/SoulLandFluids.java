package com.zelf115.soulland.fluid;

import com.zelf115.soulland.SoulLand;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class SoulLandFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, SoulLand.MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.FLUID, SoulLand.MODID);

    public static final DeferredHolder<FluidType, FluidType> RED_WATER_TYPE = FLUID_TYPES.register(
            "red_water", () -> new FluidType(FluidType.Properties.create()
                .descriptionId("block.soulland.red_water")
                .canConvertToSource(false)));
    public static final DeferredHolder<FluidType, FluidType> CYAN_WATER_TYPE = FLUID_TYPES.register(
            "cyan_water", () -> new FluidType(FluidType.Properties.create()
                .descriptionId("block.soulland.cyan_water")
                .canConvertToSource(false)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid> RED_WATER = FLUIDS.register(
            "red_water", () -> new ElementalFluid.Source(SoulLandFluids::redWater, SoulLandFluids::redWaterFlowing,
                SoulLandFluids::redWaterBlock, SoulLandFluids::redWaterType, SoulLandFluids::redWaterBucket));
    public static final DeferredHolder<Fluid, BaseFlowingFluid> RED_WATER_FLOWING = FLUIDS.register(
            "red_water_flowing", () -> new ElementalFluid.Flowing(SoulLandFluids::redWater, SoulLandFluids::redWaterFlowing,
                SoulLandFluids::redWaterBlock, SoulLandFluids::redWaterType, SoulLandFluids::redWaterBucket));
    public static final DeferredHolder<Fluid, BaseFlowingFluid> CYAN_WATER = FLUIDS.register(
            "cyan_water", () -> new ElementalFluid.Source(SoulLandFluids::cyanWater, SoulLandFluids::cyanWaterFlowing,
                SoulLandFluids::cyanWaterBlock, SoulLandFluids::cyanWaterType, SoulLandFluids::cyanWaterBucket));
    public static final DeferredHolder<Fluid, BaseFlowingFluid> CYAN_WATER_FLOWING = FLUIDS.register(
            "cyan_water_flowing", () -> new ElementalFluid.Flowing(SoulLandFluids::cyanWater, SoulLandFluids::cyanWaterFlowing,
                SoulLandFluids::cyanWaterBlock, SoulLandFluids::cyanWaterType, SoulLandFluids::cyanWaterBucket));

    public static final net.neoforged.neoforge.registries.DeferredBlock<LiquidBlock> RED_WATER_BLOCK =
            SoulLand.BLOCKS.register("red_water", () -> new LiquidBlock(RED_WATER.get(),
                    net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.COLOR_RED)));
    public static final net.neoforged.neoforge.registries.DeferredBlock<LiquidBlock> CYAN_WATER_BLOCK =
            SoulLand.BLOCKS.register("cyan_water", () -> new LiquidBlock(CYAN_WATER.get(),
                    net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.COLOR_CYAN)));

    public static final DeferredItem<BucketItem> RED_WATER_BUCKET = SoulLand.ITEMS.register("red_water_bucket",
            () -> new BucketItem(RED_WATER.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<BucketItem> CYAN_WATER_BUCKET = SoulLand.ITEMS.register("cyan_water_bucket",
            () -> new BucketItem(CYAN_WATER.get(), new Item.Properties().stacksTo(1)));

    private SoulLandFluids() {
    }

    private static Fluid redWater() {
        return RED_WATER.get();
    }

    private static Fluid redWaterFlowing() {
        return RED_WATER_FLOWING.get();
    }

    private static LiquidBlock redWaterBlock() {
        return RED_WATER_BLOCK.get();
    }

    private static FluidType redWaterType() {
        return RED_WATER_TYPE.get();
    }

    private static Item redWaterBucket() {
        return RED_WATER_BUCKET.get();
    }

    private static Fluid cyanWater() {
        return CYAN_WATER.get();
    }

    private static Fluid cyanWaterFlowing() {
        return CYAN_WATER_FLOWING.get();
    }

    private static LiquidBlock cyanWaterBlock() {
        return CYAN_WATER_BLOCK.get();
    }

    private static FluidType cyanWaterType() {
        return CYAN_WATER_TYPE.get();
    }

    private static Item cyanWaterBucket() {
        return CYAN_WATER_BUCKET.get();
    }

    private static abstract class ElementalFluid extends BaseFlowingFluid {
        private ElementalFluid(Supplier<? extends Fluid> source, Supplier<? extends Fluid> flowing,
                               Supplier<? extends LiquidBlock> block, Supplier<? extends FluidType> fluidType,
                               Supplier<? extends Item> bucket) {
                        super(new BaseFlowingFluid.Properties(fluidType, source, flowing)
                            .bucket(bucket)
                            .block(block)
                            .levelDecreasePerBlock(1)
                            .slopeFindDistance(4)
                            .explosionResistance(100.0F)
                            .tickRate(5));
        }

        private static final class Source extends ElementalFluid {
            private Source(Supplier<? extends Fluid> source, Supplier<? extends Fluid> flowing,
                           Supplier<? extends LiquidBlock> block, Supplier<? extends FluidType> fluidType,
                           Supplier<? extends Item> bucket) {
                super(source, flowing, block, fluidType, bucket);
            }

            @Override
            public int getAmount(FluidState state) {
                return 8;
            }

            @Override
            public boolean isSource(FluidState state) {
                return true;
            }
        }

        private static final class Flowing extends ElementalFluid {
            private Flowing(Supplier<? extends Fluid> source, Supplier<? extends Fluid> flowing,
                            Supplier<? extends LiquidBlock> block, Supplier<? extends FluidType> fluidType,
                            Supplier<? extends Item> bucket) {
                super(source, flowing, block, fluidType, bucket);
            }

            @Override
            protected void createFluidStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, FluidState> builder) {
                super.createFluidStateDefinition(builder);
                builder.add(LEVEL);
            }

            @Override
            public int getAmount(FluidState state) {
                return state.getValue(LEVEL);
            }

            @Override
            public boolean isSource(FluidState state) {
                return false;
            }
        }
    }
}
