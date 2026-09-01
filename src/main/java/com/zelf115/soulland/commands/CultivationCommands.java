package com.zelf115.soulland.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.zelf115.soulland.cultivation.BreakthroughManager;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.events.CultivationEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Registers all {@code /cultivation} sub-commands.
 *
 * <ul>
 *   <li>{@code /cultivation status} — shows current level, XP, bottleneck state</li>
 *   <li>{@code /cultivation breakthrough} — attempts a regular breakthrough</li>
 *   <li>{@code /cultivation specialbreakthrough} — begins a special lightning breakthrough (level 95+)</li>
 *   <li>{@code /cultivation settitle <title>} — sets the player's title (requires level ≥ 90)</li>
 *   <li>{@code /cultivation absorbring} — DEBUG: simulate absorbing a soul ring</li>
 *   <li>{@code /cultivation addxp <amount>} — DEBUG: add raw XP (op-only)</li>
 * </ul>
 */
public class CultivationCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cultivation")
                .then(Commands.literal("status")
                        .executes(CultivationCommands::status))
                .then(Commands.literal("breakthrough")
                        .executes(CultivationCommands::breakthrough))
                .then(Commands.literal("specialbreakthrough")
                        .executes(CultivationCommands::specialBreakthrough))
                .then(Commands.literal("settitle")
                        .then(Commands.argument("title", StringArgumentType.greedyString())
                                .executes(CultivationCommands::setTitle)))
                .then(Commands.literal("absorbring")
                        .executes(CultivationCommands::absorbRing))
                .then(Commands.literal("addxp")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("amount", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg(0))
                                .executes(CultivationCommands::addXp)))
        );
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA);
        String bottleneckStr = data.isInBottleneck()
                ? " §e(BOTTLENECK)"
                : "";
        String titleStr = data.getTitle().isEmpty() ? "none" : data.getTitle();

        player.sendSystemMessage(Component.literal(
                "§6=== Cultivation Status ===\n" +
                "§fLevel: §b" + data.getLevel() + bottleneckStr + "\n" +
                "§fXP: §b" + String.format("%.1f", data.getXp()) + " / " +
                        String.format("%.1f", CultivationManager.xpRequiredForLevel(data.getLevel())) + "\n" +
                "§fSoul Rings: §b" + data.getSoulRingCount() + "\n" +
                "§fTier: §b" + data.getPlayerTier() + "\n" +
                "§fBreakthrough Failures: §b" + data.getBreakthroughFailures() + "\n" +
                "§fRebirth Count: §b" + data.getRebirthCount() + "\n" +
                "§fTitle: §b" + titleStr
        ));
        return 1;
    }

    private static int breakthrough(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA);
        if (CultivationManager.requiresSpecialBreakthrough(data.getLevel())) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.breakthrough.use_special"));
            return 0;
        }

        long gameTick = player.level().getGameTime();
        BreakthroughManager.attemptRegularBreakthrough(player, data, gameTick);
        return 1;
    }

    private static int specialBreakthrough(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA);
        if (!CultivationManager.requiresSpecialBreakthrough(data.getLevel())) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.breakthrough.not_special_level"));
            return 0;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) return 0;

        // Record the tick of the lightning strike for survival-confirmation timing
        player.getPersistentData().putLong("soulland_special_bt_strike_tick", player.level().getGameTime());
        BreakthroughManager.beginSpecialBreakthrough(player, data, serverLevel);
        return 1;
    }

    private static int setTitle(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA);
        if (data.getLevel() < 90) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.title.locked"));
            return 0;
        }

        String title = StringArgumentType.getString(ctx, "title");
        data.setTitle(title);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.title.set", title));
        return 1;
    }

    private static int absorbRing(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA);
        int level = data.getLevel();
        int required = (level / 10) + 1;

        if (data.getSoulRingCount() >= required) {
            player.sendSystemMessage(Component.literal("§cYou already have enough soul rings for your current level."));
            return 0;
        }

        data.setSoulRingCount(data.getSoulRingCount() + 1);
        player.sendSystemMessage(Component.literal("§aSoul ring absorbed! Total rings: " + data.getSoulRingCount()));
        return 1;
    }

    private static int addXp(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        double amount = com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(ctx, "amount");
        CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA);
        CultivationEvents.addXpAndCheckLevelUp(player, data, amount);
        player.sendSystemMessage(Component.literal("§aAdded " + amount + " cultivation XP."));
        return 1;
    }
}
