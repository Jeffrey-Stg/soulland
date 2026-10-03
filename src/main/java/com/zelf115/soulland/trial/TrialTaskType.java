package com.zelf115.soulland.trial;

/** The kinds of task a god trial can demand. */
public enum TrialTaskType {
    KILL_HUNDRED_THOUSAND_YEAR_BEASTS("kill_beasts"),
    KILL_MILLION_YEAR_BEAST("kill_ancient"),
    FULL_SPIRIT_BONE_SET("bone_set"),
    WIN_TOURNAMENT("tournament"),
    REACH_LEVEL_99("reach_level");

    /** Ages that separate the two beast-hunting tasks, matching the soul ring colour bands. */
    public static final int HUNDRED_THOUSAND_YEARS = 100_000;
    public static final int MILLION_YEARS = 1_000_000;
    public static final int FINAL_TASK_LEVEL = 99;

    private final String name;

    TrialTaskType(final String name) {
        this.name = name;
    }

    public String descriptionKey() {
        return "soulland.trial.task." + name;
    }
}
