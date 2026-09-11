package com.zelf115.soulland.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.BreakthroughManager;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.cultivation.Rebirth;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Registers all {@code /cultivation} sub-commands.
 *
 * <ul>
 *   <li>{@code /cultivation status} — shows current level, XP, bottleneck state</li>
 *   <li>{@code /cultivation breakthrough} — attempts whichever breakthrough the level calls for</li>
 *   <li>{@code /cultivation settitle <title>} — sets the player's title (requires level ≥ 90)</li>
 *   <li>{@code /cultivation setinnate <1-20>} — operator stand-in until martial souls roll it</li>
 *   <li>{@code /cultivation rebirth} — restarts cultivation at level 1 and re-opens the soul picker</li>
 * </ul>
 */
public class CultivationCommands {

    private static final int OPERATOR_PERMISSION_LEVEL = 2;

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
                        .then(Commands.argument("title", StringArgumentType.greedyString())
                                .executes(CultivationCommands::setTitle)))
                .then(Commands.literal("rebirth")
                        .executes(CultivationCommands::rebirth))
                .then(Commands.literal("setinnate")
                        .requires(source -> source.hasPermission(OPERATOR_PERMISSION_LEVEL))
                        .then(Commands.argument("value",
                                        IntegerArgumentType.integer(CultivationData.MIN_INNATE_STAT, CultivationData.MAX_INNATE_STAT))
                                .executes(CultivationCommands::setInnateStat)))
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
                .append(Component.literal(data.getSoulRingCount() + " / " + CultivationManager.maxSoulRingCountForLevel(data.getLevel())).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nTier: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf(data.getPlayerTier())).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nInnate Stat: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf(data.getEffectiveInnateStat())).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nAbsorbable Ring Tier: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf(CultivationManager.maxAbsorbableTier(Stats.getSpirit(player)))).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nSpirit Bones: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(describeBones(data)).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nBreakthrough Failures: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf(data.getBreakthroughFailures())).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nSpirit Energy: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.format("%.1f / %.1f", data.getSpiritEnergy(), Stats.getMaxSpiritEnergy(player))).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nMovement Usage: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(data.getMovementUsagePercent() + "%").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nRebirth Count: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf(data.getRebirthCount())).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nTitle: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(titleStr).withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(statusMessage);
        return 1;
    }

    /** Lists the equipped bones, marking an external one as shown or hidden. */
    private static String describeBones(final CultivationData data) {
        if (data.getSpiritBones().isEmpty()) {
            return "none";
        }

        return data.getSpiritBones().values().stream()
                .map(bone -> SpiritBeastManager.isExternalBoneSlot(bone.slot())
                        ? bone.slot() + (data.isExternalBoneVisible() ? " (shown)" : " (hidden)")
                        : bone.slot())
                .collect(Collectors.joining(", "));
    }

    private static int breakthrough(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        return BreakthroughManager.attemptBreakthrough(player, data, player.level().getGameTime()) ? 1 : 0;
    }

    private static int rebirth(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        return Rebirth.perform(player, data) ? 1 : 0;
    }

    private static int setTitle(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        if (data.getLevel() < CultivationManager.TITLE_LEVEL) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.title.locked"));
            return 0;
        }

        final String title = StringArgumentType.getString(ctx, "title");
        data.setTitle(title);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.title.set", title));
        return 1;
    }

    private static int setInnateStat(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        data.setInnateStat(IntegerArgumentType.getInteger(ctx, "value"));
        player.sendSystemMessage(Component.translatable("soulland.cultivation.innate.set", data.getEffectiveInnateStat()));
        return 1;
    }
}
