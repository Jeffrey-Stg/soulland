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
import com.zelf115.soulland.cultivation.skill.Skill;
import com.zelf115.soulland.cultivation.skill.SkillTag;
import com.zelf115.soulland.cultivation.technique.LearnedTechniques;
import com.zelf115.soulland.cultivation.technique.Technique;
import com.zelf115.soulland.item.SoulRingItem;
import com.zelf115.soulland.item.SpiritBoneItem;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import com.zelf115.soulland.tournament.TournamentManager;
import com.zelf115.soulland.trial.GodTrial;
import com.zelf115.soulland.trial.GodTrialManager;
import com.zelf115.soulland.trial.TrialTasks;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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
 *   <li>{@code /cultivation trial status} — shows the running god trial and the relics already earned</li>
 *   <li>{@code /cultivation trial advance|reset|grant <god>} — operator tools for testing trials</li>
 *   <li>{@code /cultivation tournament status|reset} — shows or reopens the daily tournament run</li>
 *   <li>{@code /cultivation techniques [setlevel <technique> <level>]} — shows learned techniques; operators can set a level</li>
 *   <li>{@code /cultivation skillring <skill>} / {@code skillbone <skill>} — operator tools: a ring or bone carrying the named skill</li>
 * </ul>
 */
public class CultivationCommands {

    private static final int OPERATOR_PERMISSION_LEVEL = 2;
    private static final int SKILL_ITEM_TIER = 1;
    private static final String SKILL_BONE_SLOT = "Torso Bone";

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
                .then(Commands.literal("trial")
                        .then(Commands.literal("status")
                                .executes(CultivationCommands::trialStatus))
                        .then(Commands.literal("advance")
                                .requires(source -> source.hasPermission(OPERATOR_PERMISSION_LEVEL))
                                .executes(CultivationCommands::advanceTrialTask))
                        .then(Commands.literal("reset")
                                .requires(source -> source.hasPermission(OPERATOR_PERMISSION_LEVEL))
                                .executes(CultivationCommands::resetTrial))
                        .then(Commands.literal("grant")
                                .requires(source -> source.hasPermission(OPERATOR_PERMISSION_LEVEL))
                                .then(Commands.argument("god", StringArgumentType.word())
                                        .executes(CultivationCommands::grantRelic))))
                .then(Commands.literal("tournament")
                        .then(Commands.literal("status")
                                .executes(CultivationCommands::tournamentStatus))
                        .then(Commands.literal("reset")
                                .requires(source -> source.hasPermission(OPERATOR_PERMISSION_LEVEL))
                                .executes(CultivationCommands::resetTournament)))
                .then(Commands.literal("setinnate")
                        .requires(source -> source.hasPermission(OPERATOR_PERMISSION_LEVEL))
                        .then(Commands.argument("value",
                                        IntegerArgumentType.integer(CultivationData.MIN_INNATE_STAT, CultivationData.MAX_INNATE_STAT))
                                .executes(CultivationCommands::setInnateStat)))
                .then(Commands.literal("techniques")
                        .executes(CultivationCommands::techniqueStatus)
                        .then(Commands.literal("setlevel")
                                .requires(source -> source.hasPermission(OPERATOR_PERMISSION_LEVEL))
                                .then(Commands.argument("technique", StringArgumentType.word())
                                        .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                                .executes(CultivationCommands::setTechniqueLevel)))))
                .then(Commands.literal("skillring")
                        .requires(source -> source.hasPermission(OPERATOR_PERMISSION_LEVEL))
                        .then(Commands.argument("skill", StringArgumentType.word())
                                .executes(CultivationCommands::giveSkillRing)))
                .then(Commands.literal("skillbone")
                        .requires(source -> source.hasPermission(OPERATOR_PERMISSION_LEVEL))
                        .then(Commands.argument("skill", StringArgumentType.word())
                                .executes(CultivationCommands::giveSkillBone)))
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
                .append(Component.literal("\nHealth: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.format("%.1f", Stats.getHealth(player))).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nDamage: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.format("%.1f", Stats.getDamage(player))).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nDefense: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.format("%.1f", Stats.getDefense(player))).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nSpirit: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.format("%.1f", Stats.getSpirit(player))).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nCultivation Speed: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.format("+%.1f%%", Stats.getCultivationSpeed(player))).withStyle(ChatFormatting.AQUA))
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
                .append(Component.literal("\nGod Trial: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(describeTrial(data)).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nRelics: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(describeRelics(data)).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nTitle: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(titleStr).withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(statusMessage);
        return 1;
    }

    private static int trialStatus(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        if (!data.hasStartedGodTrial()) {
            player.sendSystemMessage(Component.translatable("soulland.command.trial.none"));
        } else {
            player.sendSystemMessage(Component.translatable("soulland.command.trial.status",
                    data.getGodTrial().displayName(), data.getGodTrialTaskIndex() + 1, TrialTasks.TASK_COUNT,
                    GodTrialManager.describeCurrentTask(data), data.getGodTrialPendingRewards()));
        }
        player.sendSystemMessage(Component.translatable("soulland.command.trial.relics", describeRelics(data)));
        return 1;
    }

    private static String describeTrial(final CultivationData data) {
        if (!data.hasStartedGodTrial()) {
            return "none";
        }
        if (data.isGodTrialFinished()) {
            return data.getGodTrial().displayName().getString() + " (passed)";
        }
        return data.getGodTrial().displayName().getString()
                + " (task " + (data.getGodTrialTaskIndex() + 1) + " of " + TrialTasks.TASK_COUNT + ")";
    }

    private static String describeRelics(final CultivationData data) {
        if (data.getCompletedGodTrials().isEmpty()) {
            return "none";
        }
        return data.getCompletedGodTrials().stream()
                .map(trial -> trial.displayName().getString())
                .collect(Collectors.joining(", "));
    }

    private static int advanceTrialTask(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        GodTrialManager.forceCompleteCurrentTask(player, data);
        return 1;
    }

    private static int resetTrial(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        GodTrialManager.clearTrial(player.getData(CultivationAttachment.CULTIVATION_DATA.get()));
        player.sendSystemMessage(Component.translatable("soulland.command.trial.reset"));
        return 1;
    }

    private static int grantRelic(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final GodTrial trial = readGodTrial(StringArgumentType.getString(ctx, "god"));
        if (trial == null) {
            player.sendSystemMessage(Component.translatable("soulland.command.trial.unknown_god"));
            return 0;
        }

        player.getData(CultivationAttachment.CULTIVATION_DATA.get()).addCompletedGodTrial(trial);
        player.sendSystemMessage(Component.translatable("soulland.command.trial.granted", trial.displayName()));
        return 1;
    }

    private static int techniqueStatus(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final LearnedTechniques techniques = player.getData(CultivationAttachment.CULTIVATION_DATA.get()).getTechniques();
        if (techniques.learned().isEmpty()) {
            player.sendSystemMessage(Component.translatable("soulland.command.techniques.none"));
            return 1;
        }
        for (final Technique technique : techniques.learned()) {
            player.sendSystemMessage(Component.translatable("soulland.command.techniques.entry", technique.displayName(),
                    techniques.level(technique), technique.maxLevel(),
                    technique.percentToNextLevel(techniques.getProgress(technique))));
        }
        return 1;
    }

    private static int setTechniqueLevel(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final Technique technique = readTechnique(StringArgumentType.getString(ctx, "technique"));
        if (technique == null) {
            player.sendSystemMessage(Component.translatable("soulland.command.techniques.unknown"));
            return 0;
        }

        final int level = Math.clamp(IntegerArgumentType.getInteger(ctx, "level"), technique.minLevel(), technique.maxLevel());
        player.getData(CultivationAttachment.CULTIVATION_DATA.get()).getTechniques()
                .setProgress(technique, technique.progressForLevel(level));
        player.sendSystemMessage(Component.translatable("soulland.command.techniques.set", technique.displayName(), level));
        return 1;
    }

    private static int giveSkillRing(CommandContext<CommandSourceStack> ctx) {
        return giveSkillItem(ctx, CultivationCommands::youngestTestRing);
    }

    private static int giveSkillBone(CommandContext<CommandSourceStack> ctx) {
        return giveSkillItem(ctx, CultivationCommands::youngestTestBone);
    }

    /** Hands out a ring or bone carrying the named skill, so every skill can be tried without farming drops. */
    private static int giveSkillItem(CommandContext<CommandSourceStack> ctx, final Supplier<ItemStack> carrier) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final Optional<Skill> skill = Skill.byName(StringArgumentType.getString(ctx, "skill").toUpperCase(Locale.ROOT));
        if (skill.isEmpty()) {
            player.sendSystemMessage(Component.translatable("soulland.command.skillitem.unknown"));
            return 0;
        }

        final ItemStack item = carrier.get();
        SkillTag.attach(item, skill.get());
        player.getInventory().placeItemBackInInventory(item);
        player.sendSystemMessage(Component.translatable("soulland.command.skillitem.given",
                item.getHoverName(), skill.get().displayName()));
        return 1;
    }

    private static ItemStack youngestTestRing() {
        final int years = SpiritBeastManager.oldestYearsOfTier(SKILL_ITEM_TIER);
        return SoulRingItem.create(testSourceName(), SKILL_ITEM_TIER, years,
                SpiritBeastManager.soulRingBonusForAge(SKILL_ITEM_TIER, years), Set.of());
    }

    private static ItemStack youngestTestBone() {
        final int years = SpiritBeastManager.oldestYearsOfTier(SKILL_ITEM_TIER);
        return SpiritBoneItem.create(testSourceName(), SKILL_BONE_SLOT, SKILL_ITEM_TIER, years,
                SpiritBeastManager.spiritBoneBonusForAge(SKILL_ITEM_TIER, years));
    }

    private static String testSourceName() {
        return Component.translatable("soulland.command.skillitem.source").getString();
    }

    private static Technique readTechnique(final String name) {
        try {
            return Technique.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static GodTrial readGodTrial(final String name) {
        try {
            return GodTrial.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static int tournamentStatus(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        player.sendSystemMessage(Component.translatable("soulland.command.tournament.status",
                data.getTournamentRound(), TournamentManager.TOTAL_ROUNDS, data.isTournamentRunSpent()));
        return 1;
    }

    private static int resetTournament(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        data.setTournamentRunStartedAt(0L);
        data.setTournamentRunSpent(false);
        data.setTournamentRound(0);
        player.sendSystemMessage(Component.translatable("soulland.command.tournament.reset"));
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
