package com.zelf115.soulland.trial;

import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.network.OpenAltarScreenPayload;
import com.zelf115.soulland.spirit.SpiritBeastEntity;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import com.zelf115.soulland.cultivation.AbsorbedBone;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Runs a player's god trial: starting it, tracking its tasks, and paying out at the altar. */
public final class GodTrialManager {

    private static final int NO_PROGRESS = 0;
    private static final int FIRST_TASK = 0;

    private GodTrialManager() {
    }

    /**
     * The altar right-click: settles any task the player now meets by what they have, then shows
     * the altar screen. The screen reads the trial from the player's synced record, so it is synced first.
     */
    public static void open(final ServerPlayer player, final CultivationData data, final GodTrial trial,
                            final BlockPos pos) {
        if (data.getGodTrial() == trial && !data.isGodTrialFinished()) {
            evaluateStateTasks(player, data);
        }
        player.syncData(CultivationAttachment.CULTIVATION_DATA);
        PacketDistributor.sendToPlayer(player, new OpenAltarScreenPayload(pos, trial.ordinal()));
    }

    /** The altar's Begin button: starts this god's trial when no trial is under way yet. */
    public static void begin(final ServerPlayer player, final CultivationData data, final GodTrial trial) {
        if (data.hasStartedGodTrial()) {
            return;
        }
        if (!trial.acceptsMartialSoulOf(data)) {
            player.displayClientMessage(Component.translatable("soulland.trial.requires_martial_soul",
                    trial.getRequiredMartialSoul().displayName()), true);
            return;
        }

        data.setGodTrial(trial);
        data.setGodTrialTasks(TrialTasks.roll(player.getRandom()));
        data.setGodTrialTaskIndex(FIRST_TASK);
        data.setGodTrialProgress(NO_PROGRESS);
        data.setGodTrialPendingRewards(NO_PROGRESS);
        player.sendSystemMessage(Component.translatable("soulland.trial.started", trial.displayName(),
                describeCurrentTask(data)));
        evaluateStateTasks(player, data);
    }

    /** A kill only counts while the trial is waiting on a beast of that age. */
    public static void recordBeastKill(final ServerPlayer player, final CultivationData data,
                                       final SpiritBeastEntity beast) {
        final TrialTask task = data.getCurrentTrialTask();
        if (task == null || !countsAsKillFor(task, SpiritBeastManager.getYears(beast))) {
            return;
        }

        data.setGodTrialProgress(data.getGodTrialProgress() + 1);
        completeIfDone(player, data, task);
    }

    /** A finished ten-round tournament run, reported by the tournament itself. */
    public static void recordTournamentWin(final ServerPlayer player, final CultivationData data) {
        final TrialTask task = data.getCurrentTrialTask();
        if (task == null || task.type() != TrialTaskType.WIN_TOURNAMENT) {
            return;
        }

        data.setGodTrialProgress(data.getGodTrialProgress() + 1);
        completeIfDone(player, data, task);
    }

    /** Puts the player back to having no trial at all, ready to face a god again. */
    public static void clearTrial(final CultivationData data) {
        data.setGodTrial(null);
        data.setGodTrialTasks(List.of());
        data.setGodTrialTaskIndex(FIRST_TASK);
        data.setGodTrialProgress(NO_PROGRESS);
        data.setGodTrialPendingRewards(NO_PROGRESS);
    }

    /** Finishes the current task outright, for testing the tasks that follow it. */
    public static void forceCompleteCurrentTask(final ServerPlayer player, final CultivationData data) {
        if (data.getCurrentTrialTask() == null) {
            return;
        }
        completeCurrentTask(player, data);
    }

    private static boolean countsAsKillFor(final TrialTask task, final int years) {
        return switch (task.type()) {
            case KILL_HUNDRED_THOUSAND_YEAR_BEASTS -> years >= TrialTaskType.HUNDRED_THOUSAND_YEARS;
            case KILL_MILLION_YEAR_BEAST -> years >= TrialTaskType.MILLION_YEARS;
            default -> false;
        };
    }

    /** Tasks that ask what the player has, rather than what they did, are answered at the altar. */
    private static void evaluateStateTasks(final ServerPlayer player, final CultivationData data) {
        final TrialTask task = data.getCurrentTrialTask();
        if (task == null || !isSatisfiedByState(player, data, task)) {
            return;
        }

        data.setGodTrialProgress(task.target());
        completeIfDone(player, data, task);
    }

    private static boolean isSatisfiedByState(final ServerPlayer player, final CultivationData data,
                                              final TrialTask task) {
        return switch (task.type()) {
            case FULL_SPIRIT_BONE_SET -> hasFullInternalBoneSet(data);
            case REACH_LEVEL_99 -> data.getLevel() >= TrialTaskType.FINAL_TASK_LEVEL;
            default -> false;
        };
    }

    private static boolean hasFullInternalBoneSet(final CultivationData data) {
        return SpiritBeastManager.INTERNAL_BONE_SLOTS.stream()
                .allMatch(slot -> data.getSpiritBones().containsKey(AbsorbedBone.slotKey(slot)));
    }

    private static void completeIfDone(final ServerPlayer player, final CultivationData data, final TrialTask task) {
        if (data.getGodTrialProgress() < task.target()) {
            return;
        }
        completeCurrentTask(player, data);
    }

    private static void completeCurrentTask(final ServerPlayer player, final CultivationData data) {
        data.setGodTrialTaskIndex(data.getGodTrialTaskIndex() + 1);
        data.setGodTrialProgress(NO_PROGRESS);
        if (data.isGodTrialFinished()) {
            finishTrial(player, data);
            return;
        }

        data.setGodTrialPendingRewards(data.getGodTrialPendingRewards() + 1);
        player.sendSystemMessage(Component.translatable("soulland.trial.task_complete"));
    }

    /** The last reward is the god's own relic, and with it the right to climb past level 99. */
    private static void finishTrial(final ServerPlayer player, final CultivationData data) {
        final GodTrial trial = data.getGodTrial();
        data.setHasGodInheritance(true);
        data.addCompletedGodTrial(trial);
        player.getInventory().placeItemBackInInventory(new ItemStack(trial.relicItem()));
        player.sendSystemMessage(Component.translatable("soulland.trial.complete", trial.displayName()));
    }

    /** The altar's Claim button: hands out one reward for a finished task, at the altar of the god owed it. */
    public static void claimOneReward(final ServerPlayer player, final CultivationData data, final GodTrial trial) {
        if (data.getGodTrial() != trial || data.getGodTrialPendingRewards() <= 0) {
            return;
        }
        data.setGodTrialPendingRewards(data.getGodTrialPendingRewards() - 1);
        player.sendSystemMessage(Component.translatable("soulland.trial.reward.claimed",
                GodTrialRewards.grantRandom(player, data)));
    }

    /** The current task written out for the player, with its progress where the task counts. */
    public static Component describeCurrentTask(final CultivationData data) {
        final TrialTask task = data.getCurrentTrialTask();
        if (task == null) {
            return Component.translatable("soulland.trial.task.none");
        }
        return describeTask(task, data.getGodTrialProgress());
    }

    /** One task written out for the player, with the given progress where the task counts. */
    public static Component describeTask(final TrialTask task, final int progress) {
        if (task.type() == TrialTaskType.KILL_HUNDRED_THOUSAND_YEAR_BEASTS) {
            return Component.translatable(task.type().descriptionKey(), task.target(), progress, task.target());
        }
        if (task.type() == TrialTaskType.REACH_LEVEL_99) {
            return Component.translatable(task.type().descriptionKey(), TrialTaskType.FINAL_TASK_LEVEL);
        }
        return Component.translatable(task.type().descriptionKey());
    }
}
