package com.zelf115.soulland.events;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.fluid.SoulLandFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SoulLand.MODID)
public final class ElementalFluidEvents {
    private ElementalFluidEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        BlockPos feetPosition = player.blockPosition();
        BlockPos eyePosition = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        FluidState feetFluid = player.level().getFluidState(feetPosition);
        FluidState eyeFluid = player.level().getFluidState(eyePosition);
        if (isRedWater(feetFluid) || isRedWater(eyeFluid)) {
            player.hurt(player.damageSources().inFire(), 2.0F);
        } else if (isCyanWater(feetFluid) || isCyanWater(eyeFluid)) {
            player.hurt(player.damageSources().freeze(), 2.0F);
        }
    }

    private static boolean isRedWater(FluidState state) {
        return state.is(SoulLandFluids.RED_WATER.get()) || state.is(SoulLandFluids.RED_WATER_FLOWING.get());
    }

    private static boolean isCyanWater(FluidState state) {
        return state.is(SoulLandFluids.CYAN_WATER.get()) || state.is(SoulLandFluids.CYAN_WATER_FLOWING.get());
    }
}
