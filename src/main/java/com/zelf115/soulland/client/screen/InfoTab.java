package com.zelf115.soulland.client.screen;

import com.zelf115.soulland.Stats;
import com.zelf115.soulland.client.GaugeBar;
import com.zelf115.soulland.client.SoulLandStyle;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/** The player's level, XP and spirit energy bars, innate stat and the stats cultivation raises. */
final class InfoTab implements CultivationTab {

    private static final int ROW_HEIGHT = 14;
    private static final GaugeBar BAR = new GaugeBar(80, 160);
    private static final double PERCENT = 100.0;

    private record StatLine(Component label, String value) {
    }

    @Override
    public Component title() {
        return Component.translatable("soulland.screen.cultivation.tab.info");
    }

    @Override
    public void render(final GuiGraphics graphics, final TabArea area, final int mouseX, final int mouseY) {
        final Font font = Minecraft.getInstance().font;
        final LocalPlayer player = ClientCultivation.player();
        final CultivationData data = ClientCultivation.data();
        int y = area.top();

        graphics.drawString(font, Component.translatable("soulland.screen.info.level", data.getLevel()),
                area.left(), y, SoulLandStyle.HEADING_COLOR, true);
        y += ROW_HEIGHT;

        BAR.draw(graphics, font, area.left(), y, xpFill(data));
        y += ROW_HEIGHT;
        BAR.draw(graphics, font, area.left(), y, spiritFill(player, data));
        y += ROW_HEIGHT + ROW_HEIGHT / 2;

        for (final StatLine line : statLines(player, data)) {
            graphics.drawString(font, line.label(), area.left(), y, SoulLandStyle.TEXT_COLOR, true);
            graphics.drawString(font, line.value(), area.right() - font.width(line.value()), y,
                    SoulLandStyle.MARTIAL_SOUL_COLOR, true);
            y += ROW_HEIGHT;
        }
    }

    private static GaugeBar.Fill xpFill(final CultivationData data) {
        final double required = CultivationManager.xpRequiredForLevel(data.getLevel());
        final double progress = required > 0.0 ? data.getXp() / required : 0.0;
        final int color = data.isInBottleneck() ? SoulLandStyle.XP_BAR_BOTTLENECK_COLOR : SoulLandStyle.XP_BAR_COLOR;
        return new GaugeBar.Fill(Component.translatable("soulland.hud.xp_label"), progress, progress * PERCENT, color);
    }

    private static GaugeBar.Fill spiritFill(final LocalPlayer player, final CultivationData data) {
        final double max = Stats.getMaxSpiritEnergy(player);
        final double progress = max > 0.0 ? data.getSpiritEnergy() / max : 0.0;
        return new GaugeBar.Fill(Component.translatable("soulland.screen.info.spirit_energy"), progress,
                progress * PERCENT, SoulLandStyle.SPIRIT_BAR_COLOR);
    }

    /** The same figures as {@code /cultivation status}, formatted the same way. */
    private static List<StatLine> statLines(final LocalPlayer player, final CultivationData data) {
        return List.of(
                new StatLine(Component.translatable("soulland.screen.info.innate"), String.valueOf(data.getEffectiveInnateStat())),
                new StatLine(Component.translatable("soulland.tooltip.stat.health"), oneDecimal(Stats.getHealth(player))),
                new StatLine(Component.translatable("soulland.tooltip.stat.damage"), oneDecimal(Stats.getDamage(player))),
                new StatLine(Component.translatable("soulland.tooltip.stat.defense"), oneDecimal(Stats.getDefense(player))),
                new StatLine(Component.translatable("soulland.tooltip.stat.spirit"), oneDecimal(Stats.getSpirit(player))),
                new StatLine(Component.translatable("soulland.tooltip.stat.cultivation_speed"),
                        "+" + oneDecimal(Stats.getCultivationSpeed(player)) + "%"));
    }

    private static String oneDecimal(final double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
