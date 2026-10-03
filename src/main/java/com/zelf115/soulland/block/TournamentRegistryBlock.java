package com.zelf115.soulland.block;

import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.tournament.TournamentManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Where a cultivator signs up for the day's tournament and meets each opponent in turn. */
public class TournamentRegistryBlock extends Block {

    public TournamentRegistryBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos,
                                               final Player player, final BlockHitResult hit) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        TournamentManager.interact(serverPlayer, data, pos);
        return InteractionResult.CONSUME;
    }
}
