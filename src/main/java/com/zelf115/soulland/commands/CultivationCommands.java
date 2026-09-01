package com.zelf115.soulland.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.zelf115.soulland.cultivation.BreakthroughManager;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import net.minecraft.ChatFormatting;
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
        final String titleStr = data.getTitle().isEmpty() ? "none" : data.getTitle();
        final Component statusMessage = Component.empty()
                .append(Component.literal("=== Cultivation Status ===\n").withStyle(ChatFormatting.GOLD))
                .append(Component.literal("Level: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf(data.getLevel())).withStyle(ChatFormatting.AQUA))
                .append(data.isInBottleneck() ? Component.literal(" (BOTTLENECK)\n").withStyle(ChatFormatting.YELLOW) : Component.literal("\n"))
                .append(Component.literal("XP: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.format("%.1f / %.1f", data.getXp(), CultivationManager.xpRequiredForLevel(data.getLevel()))).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nSoul Rings: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf(data.getSoulRingCount())).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nTier: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf(data.getPlayerTier())).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nBreakthrough Failures: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf(data.getBreakthroughFailures())).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nSpirit Energy: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.format("%.1f / %.1f", data.getSpiritEnergy(), com.zelf115.soulland.Stats.getMaxSpiritEnergy(player))).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nMovement Usage: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(data.getMovementUsagePercent() + "%").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nRebirth Count: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf(data.getRebirthCount())).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nTitle: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(titleStr).withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(statusMessage);
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
