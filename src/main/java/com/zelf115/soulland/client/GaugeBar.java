package com.zelf115.soulland.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** A labelled progress bar with its percentage after it, as the HUD and the cultivation screen draw XP and spirit. */
public final class GaugeBar {

    /** What one bar shows: its label, how far it is filled (0 to 1), the percentage printed and its colour. */
    public record Fill(Component label, double fraction, double percent, int color) {
    }

    public static final int HEIGHT = 7;
    private static final int GAP = 4;
    private static final int PERCENT_RESERVE_WIDTH = 26;

    private final int labelWidth;
    private final int barWidth;

    public GaugeBar(final int labelWidth, final int barWidth) {
        this.labelWidth = labelWidth;
        this.barWidth = barWidth;
    }

    /** Label, bar and percentage side by side. */
    public int totalWidth() {
        return labelWidth + GAP + barWidth + GAP + PERCENT_RESERVE_WIDTH;
    }

    /** The percentage is left uncapped past 100%, since a bottleneck banks XP above the requirement on purpose. */
    public void draw(final GuiGraphics graphics, final Font font, final int x, final int y, final Fill fill) {
        graphics.drawString(font, fill.label(), x, y, SoulLandStyle.TEXT_COLOR, true);

        final int barLeft = x + labelWidth + GAP;
        final int barRight = barLeft + barWidth;
        final int fillWidth = (int) Math.round(barWidth * Math.max(0.0, Math.min(1.0, fill.fraction())));

        graphics.fill(barLeft - 1, y - 1, barRight + 1, y + HEIGHT + 1, SoulLandStyle.OUTLINE_COLOR);
        graphics.fill(barLeft, y, barRight, y + HEIGHT, SoulLandStyle.SLOT_BACKGROUND_COLOR);
        if (fillWidth > 0) {
            graphics.fill(barLeft, y, barLeft + fillWidth, y + HEIGHT, fill.color());
        }

        final Component percentText = Component.literal(Math.round(fill.percent()) + "%");
        final int textY = y + (HEIGHT - font.lineHeight) / 2;
        graphics.drawString(font, percentText, barRight + GAP, textY, SoulLandStyle.TEXT_COLOR, true);
    }
}
