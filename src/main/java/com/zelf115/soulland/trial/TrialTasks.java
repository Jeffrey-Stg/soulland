package com.zelf115.soulland.trial;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;

/** Rolls the five tasks of a trial. */
public final class TrialTasks {

    public static final int TASK_COUNT = 5;
    private static final int SINGLE = 1;
    private static final int MIN_BEAST_KILLS = 3;
    private static final int MAX_BEAST_KILLS = 8;

    /** Winning the tournament means a full ten-round run, not a single round. */
    private static final List<TrialTaskType> ROLLABLE_TYPES = List.of(
            TrialTaskType.KILL_HUNDRED_THOUSAND_YEAR_BEASTS,
            TrialTaskType.KILL_MILLION_YEAR_BEAST,
            TrialTaskType.FULL_SPIRIT_BONE_SET,
            TrialTaskType.WIN_TOURNAMENT);

    private TrialTasks() {
    }

    /** Four tasks drawn with repetition, then the level 99 climb the trial always ends on. */
    public static List<TrialTask> roll(final RandomSource random) {
        final List<TrialTask> tasks = new ArrayList<>(TASK_COUNT);
        for (int index = 0; index < TASK_COUNT - 1; index++) {
            tasks.add(rollOne(random));
        }
        tasks.add(new TrialTask(TrialTaskType.REACH_LEVEL_99, SINGLE));
        return tasks;
    }

    private static TrialTask rollOne(final RandomSource random) {
        final TrialTaskType type = ROLLABLE_TYPES.get(random.nextInt(ROLLABLE_TYPES.size()));
        return new TrialTask(type, targetFor(type, random));
    }

    private static int targetFor(final TrialTaskType type, final RandomSource random) {
        if (type != TrialTaskType.KILL_HUNDRED_THOUSAND_YEAR_BEASTS) {
            return SINGLE;
        }
        return MIN_BEAST_KILLS + random.nextInt(MAX_BEAST_KILLS - MIN_BEAST_KILLS + 1);
    }
}
