package com.zelf115.soulland.client;

import com.zelf115.soulland.network.HudSyncPayload;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;

/** Corner panel showing cultivation level, equipped-ring pips, XP and spirit energy bars, regional qi and the current soul beast. */
public final class CultivationHudLayer implements LayeredDraw.Layer {

    private static final int PANEL_X = 6;
    private static final int PANEL_Y = 6;
    private static final int PANEL_PADDING = 6;
    private static final int ROW_HEIGHT = 13;

    private static final int PANEL_BACKGROUND_COLOR = 0xC0161022;
    private static final int PANEL_BORDER_COLOR = 0xFF4B2E7D;
    // Dimmed rather than pure white so a white-tier (tier 1) name still reads as "colored" against it.
    private static final int TEXT_COLOR = 0xFFB8B8C0;
    private static final int LEVEL_COLOR = 0xFFFFAA00;

    private static final int LABEL_WIDTH = 34;
    private static final int BAR_WIDTH = 70;
    private static final int BAR_HEIGHT = 7;
    private static final int BAR_GAP = 4;
    private static final int PERCENT_RESERVE_WIDTH = 26;
    private static final int CONTENT_WIDTH = LABEL_WIDTH + BAR_GAP + BAR_WIDTH + BAR_GAP + PERCENT_RESERVE_WIDTH;

    private static final int BAR_BACKGROUND_COLOR = 0xFF2A2A32;
    private static final int BAR_BORDER_COLOR = 0xFF000000;
    private static final int XP_BAR_COLOR = 0xFF55DDFF;
    private static final int XP_BAR_BOTTLENECK_COLOR = 0xFFFFC94D;
    private static final int SPIRIT_BAR_COLOR = 0xFF9B6FE0;
    private static final double PERCENT = 100.0;

    private static final int PIP_RADIUS = 4;
    private static final int PIP_GAP = 2;
    /** fill(), unlike drawString(), takes a color's alpha literally; ChatFormatting's colors carry none. */
    private static final int OPAQUE_ALPHA_MASK = 0xFF000000;

    /** Ticks per second, for converting {@link DeltaTracker}'s realtime delta into seconds. */
    private static final float TICKS_PER_SECOND = 20.0F;
    /** How much of the remaining distance to target a bar's fill closes per second of real time. */
    private static final double BAR_SMOOTHING_SPEED = 10.0;

    private static volatile boolean hidden = false;

    private double xpDisplayedFraction = 0.0;
    private double spiritDisplayedFraction = 0.0;

    public static void toggleVisibility() {
        hidden = !hidden;
    }

    @Override
    public void render(final GuiGraphics graphics, final DeltaTracker deltaTracker) {
        final HudSyncPayload hud = HudClientData.latest();
        if (hidden || hud == null) {
            return;
        }

        final float dtSeconds = deltaTracker.getRealtimeDeltaTicks() / TICKS_PER_SECOND;
        final Font font = Minecraft.getInstance().font;
        final boolean showSpiritBar = hud.spiritEnergy().max() > 0.0;
        final boolean showSoulBeast = !hud.soulBeast().name().isEmpty();

        final Component levelText = Component.translatable("soulland.hud.level", hud.level());
        final Component qiText = Component.translatable("soulland.hud.qi", hud.qi());
        final Component soulBeastLabel = Component.translatable("soulland.hud.soul_beast_label");
        final Component soulBeastName = showSoulBeast ? Component.literal(hud.soulBeast().name()) : null;

        final int rowCount = 4 + (showSpiritBar ? 1 : 0) + (showSoulBeast ? 1 : 0);
        final int panelHeight = PANEL_PADDING * 2 + rowCount * ROW_HEIGHT;
        final int panelWidth = panelWidthFor(font, levelText, qiText, soulBeastLabel, soulBeastName);

        drawPanel(graphics, panelWidth, panelHeight);

        final int contentX = PANEL_X + PANEL_PADDING;
        int y = PANEL_Y + PANEL_PADDING;

        graphics.drawString(font, levelText, contentX, y, LEVEL_COLOR, true);
        y += ROW_HEIGHT;

        drawRingPips(graphics, contentX, y, hud.ringTiers());
        y += ROW_HEIGHT;

        final double rawXpProgress = safeRatio(hud.xp());
        xpDisplayedFraction = smoothTowards(xpDisplayedFraction, clampToBar(rawXpProgress), dtSeconds);
        final int xpBarColor = hud.inBottleneck() ? XP_BAR_BOTTLENECK_COLOR : XP_BAR_COLOR;
        drawBar(graphics, font, contentX, y, Component.translatable("soulland.hud.xp_label"),
                xpDisplayedFraction, rawXpProgress * PERCENT, xpBarColor);
        y += ROW_HEIGHT;

        if (showSpiritBar) {
            final double rawSpiritProgress = safeRatio(hud.spiritEnergy());
            spiritDisplayedFraction = smoothTowards(spiritDisplayedFraction, clampToBar(rawSpiritProgress), dtSeconds);
            drawBar(graphics, font, contentX, y, Component.translatable("soulland.hud.spirit_label"),
                    spiritDisplayedFraction, rawSpiritProgress * PERCENT, SPIRIT_BAR_COLOR);
            y += ROW_HEIGHT;
        }

        graphics.drawString(font, qiText, contentX, y, TEXT_COLOR, true);
        y += ROW_HEIGHT;

        if (soulBeastName != null) {
            graphics.drawString(font, soulBeastLabel, contentX, y, TEXT_COLOR, true);
            final int beastColor = SpiritBeastManager.tierTextColor(hud.soulBeast().tier());
            graphics.drawString(font, soulBeastName, contentX + font.width(soulBeastLabel), y, beastColor, true);
        }
    }

    /** Wide enough for the bars, or for the longest line of text, whichever asks for more. */
    private static int panelWidthFor(final Font font, final Component levelText, final Component qiText,
                                     final Component soulBeastLabel, final Component soulBeastName) {
        int textWidth = Math.max(font.width(levelText), font.width(qiText));
        if (soulBeastName != null) {
            textWidth = Math.max(textWidth, font.width(soulBeastLabel) + font.width(soulBeastName));
        }
        return Math.max(CONTENT_WIDTH, textWidth) + PANEL_PADDING * 2;
    }

    private static void drawPanel(final GuiGraphics graphics, final int panelWidth, final int panelHeight) {
        graphics.fill(PANEL_X, PANEL_Y, PANEL_X + panelWidth, PANEL_Y + panelHeight, PANEL_BORDER_COLOR);
        graphics.fill(PANEL_X + 1, PANEL_Y + 1, PANEL_X + panelWidth - 1, PANEL_Y + panelHeight - 1, PANEL_BACKGROUND_COLOR);
    }

    /** One pip per equipped soul ring; nothing drawn for the slots not yet filled. */
    private static void drawRingPips(final GuiGraphics graphics, final int x, final int y, final List<Integer> ringTiers) {
        final int centerY = y + PIP_RADIUS;
        for (int slot = 0; slot < ringTiers.size(); slot++) {
            final int centerX = x + PIP_RADIUS + slot * (PIP_RADIUS * 2 + PIP_GAP);
            final int color = SpiritBeastManager.tierTextColor(ringTiers.get(slot)) | OPAQUE_ALPHA_MASK;
            drawCircle(graphics, centerX, centerY, PIP_RADIUS + 1, BAR_BORDER_COLOR);
            drawCircle(graphics, centerX, centerY, PIP_RADIUS, color);
        }
    }

    /** Fills a circle one horizontal span per row, since {@link GuiGraphics} only offers rectangle fills. */
    private static void drawCircle(final GuiGraphics graphics, final int centerX, final int centerY, final int radius, final int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            final int halfWidth = (int) Math.round(Math.sqrt((double) (radius * radius) - (double) (dy * dy)));
            graphics.fill(centerX - halfWidth, centerY + dy, centerX + halfWidth + 1, centerY + dy + 1, color);
        }
    }

    /** The percentage is left uncapped past 100%, since a bottleneck banks XP above the requirement on purpose. */
    private static void drawBar(final GuiGraphics graphics, final Font font, final int x, final int y,
                                final Component label, final double displayedFraction, final double truePercent, final int fillColor) {
        graphics.drawString(font, label, x, y, TEXT_COLOR, true);

        final int barLeft = x + LABEL_WIDTH + BAR_GAP;
        final int barRight = barLeft + BAR_WIDTH;
        final int fillWidth = (int) Math.round(BAR_WIDTH * displayedFraction);

        graphics.fill(barLeft - 1, y - 1, barRight + 1, y + BAR_HEIGHT + 1, BAR_BORDER_COLOR);
        graphics.fill(barLeft, y, barRight, y + BAR_HEIGHT, BAR_BACKGROUND_COLOR);
        if (fillWidth > 0) {
            graphics.fill(barLeft, y, barLeft + fillWidth, y + BAR_HEIGHT, fillColor);
        }

        final Component percentText = Component.literal(Math.round(truePercent) + "%");
        final int textY = y + (BAR_HEIGHT - font.lineHeight) / 2;
        graphics.drawString(font, percentText, barRight + BAR_GAP, textY, TEXT_COLOR, true);
    }

    private static double safeRatio(final HudSyncPayload.Gauge gauge) {
        return gauge.max() > 0.0 ? gauge.current() / gauge.max() : 0.0;
    }

    private static double clampToBar(final double progress) {
        return Math.max(0.0, Math.min(1.0, progress));
    }

    /** Closes a fraction of the remaining distance each frame, so the bar eases into place instead of snapping on every sync. */
    private static double smoothTowards(final double current, final double target, final float dtSeconds) {
        final double step = Math.min(1.0, BAR_SMOOTHING_SPEED * dtSeconds);
        return current + (target - current) * step;
    }
}
