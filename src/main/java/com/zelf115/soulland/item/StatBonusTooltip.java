package com.zelf115.soulland.item;

import com.zelf115.soulland.StatBonus;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Shared tooltip layout for soul rings and spirit bones: where the drop came from, then one
 * coloured line per stat absorbing it would grant.
 *
 * <p>The tier is deliberately absent — the item's own name already carries it, in its colour.
 */
public final class StatBonusTooltip {

    private static final String DAMAGE_KEY = "soulland.tooltip.stat.damage";
    private static final String HEALTH_KEY = "soulland.tooltip.stat.health";
    private static final String DEFENSE_KEY = "soulland.tooltip.stat.defense";
    private static final String SPEED_KEY = "soulland.tooltip.stat.speed";
    private static final String SPIRIT_KEY = "soulland.tooltip.stat.spirit";
    private static final String CULTIVATION_SPEED_KEY = "soulland.tooltip.stat.cultivation_speed";

    private StatBonusTooltip() {
    }

    /** The beast the drop came from and how old it was, on one line. */
    public static void appendOrigin(final List<Component> lines, final String sourceName, final int years) {
        lines.add(Component.translatable("soulland.tooltip.origin", sourceName, formatYears(years))
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    public static void appendStats(final List<Component> lines, final StatBonus bonus) {
        lines.add(Component.empty());
        lines.add(Component.translatable("soulland.tooltip.grants").withStyle(ChatFormatting.GRAY));
        addStat(lines, DAMAGE_KEY, bonus.damage(), ChatFormatting.RED);
        addStat(lines, HEALTH_KEY, bonus.health(), ChatFormatting.GREEN);
        addStat(lines, DEFENSE_KEY, bonus.defense(), ChatFormatting.BLUE);
        addStat(lines, SPEED_KEY, bonus.speed(), ChatFormatting.AQUA);
        addStat(lines, SPIRIT_KEY, bonus.spirit(), ChatFormatting.LIGHT_PURPLE);
        addPercentStat(lines, CULTIVATION_SPEED_KEY, bonus.cultivationSpeed(), ChatFormatting.GOLD);
    }

    private static void addStat(final List<Component> lines, final String nameKey, final double value,
                                final ChatFormatting color) {
        if (value == 0.0) {
            return;
        }
        addEntry(lines, nameKey, formatAmount(value), color);
    }

    private static void addPercentStat(final List<Component> lines, final String nameKey, final double value,
                                       final ChatFormatting color) {
        if (value == 0.0) {
            return;
        }
        addEntry(lines, nameKey, formatAmount(value) + "%", color);
    }

    private static void addEntry(final List<Component> lines, final String nameKey, final String amount,
                                 final ChatFormatting color) {
        lines.add(Component.translatable("soulland.tooltip.stat_entry", amount, Component.translatable(nameKey))
                .withStyle(color));
    }

    private static String formatAmount(final double value) {
        if (value == Math.rint(value)) {
            return String.format(Locale.ROOT, "%,.0f", value);
        }
        return String.format(Locale.ROOT, "%,.1f", value);
    }

    public static String formatYears(final int years) {
        return String.format(Locale.ROOT, "%,d", years);
    }
}
