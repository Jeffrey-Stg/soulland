package com.zelf115.soulland.block;

import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.trial.GodTrial;
import com.zelf115.soulland.trial.GodTrialManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The altar of one god: the only place that god's trial can be begun, advanced or claimed. */
public class GodAltarBlock extends Block {

    private final GodTrial trial;

    public GodAltarBlock(final Properties properties, final GodTrial trial) {
        super(properties);
        this.trial = trial;
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos,
                                               final Player player, final BlockHitResult hit) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        GodTrialManager.interact(serverPlayer, data, trial);
        return InteractionResult.CONSUME;
    }
}
