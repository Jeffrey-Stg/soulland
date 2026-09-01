package com.zelf115.soulland.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.zelf115.soulland.cultivation.BreakthroughManager;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Registers all {@code /cultivation} sub-commands.
 *
 * <ul>
 *   <li>{@code /cultivation status} — shows current level, XP, bottleneck state</li>
 *   <li>{@code /cultivation breakthrough} — automatically attempts the required breakthrough type</li>
 *   <li>{@code /cultivation settitle <title>} — sets the player's title (requires level ≥ 90)</li>
 * </ul>
 */
public class CultivationCommands {

    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cultivation")
                .then(Commands.literal("status")
                        .executes(CultivationCommands::status))
                .then(Commands.literal("breakthrough")
                        .executes(CultivationCommands::breakthrough))
                .then(Commands.literal("settitle")
                        .then(Commands.argument("title", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                                .executes(CultivationCommands::setTitle)))
        );
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        final String bottleneckStr = data.isInBottleneck()
                ? " §e(BOTTLENECK)"
                : "";
        final String titleStr = data.getTitle().isEmpty() ? "none" : data.getTitle();

        player.sendSystemMessage(Component.literal(
                "§6=== Cultivation Status ===\n" +
                "§fLevel: §b" + data.getLevel() + bottleneckStr + "\n" +
                "§fXP: §b" + String.format("%.1f", data.getXp()) + " / " +
                        String.format("%.1f", CultivationManager.xpRequiredForLevel(data.getLevel())) + "\n" +
                "§fSoul Rings: §b" + data.getSoulRingCount() + "\n" +
                "§fTier: §b" + data.getPlayerTier() + "\n" +
                "§fBreakthrough Failures: §b" + data.getBreakthroughFailures() + "\n" +
                "§fSpirit Energy: §b" + String.format("%.1f", data.getSpiritEnergy()) + " / " + String.format("%.1f", com.zelf115.soulland.Stats.getMaxSpiritEnergy(player)) + "\n" +
                "§fMovement Usage: §b" + data.getMovementUsagePercent() + "%\n" +
                "§fRebirth Count: §b" + data.getRebirthCount() + "\n" +
                "§fTitle: §b" + titleStr
        ));
        return 1;
    }

    private static int breakthrough(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        if (CultivationManager.requiresSpecialBreakthrough(data.getLevel())) {
            if (!(player.level() instanceof ServerLevel serverLevel)) {
                return 0;
            }
            player.getPersistentData().putLong(com.zelf115.soulland.Cultivation.SPECIAL_BREAKTHROUGH_STRIKE_TICK_KEY, player.level().getGameTime());
            BreakthroughManager.beginSpecialBreakthrough(player, data, serverLevel);
            return 1;
        }

        final long gameTick = player.level().getGameTime();
        BreakthroughManager.attemptRegularBreakthrough(player, data, gameTick);
        return 1;
    }

    private static int setTitle(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        if (data.getLevel() < 90) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.title.locked"));
            return 0;
        }

        final String title = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "title");
        data.setTitle(title);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.title.set", title));
        return 1;
    }
}
