package com.zelf115.soulland.client;

import com.zelf115.soulland.ClientConfig;
import com.zelf115.soulland.network.HudSyncPayload;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Panel showing cultivation level, the active soul's ring pips, XP and spirit energy bars, regional
 * qi, the active martial soul and the ring and bone its cast keys use. Where it sits, and whether it
 * shows at all, is the player's client setting.
 */
public final class CultivationHudLayer implements LayeredDraw.Layer {

    private static final int SCREEN_MARGIN = 6;
    private static final int PANEL_PADDING = 6;
    private static final int ROW_HEIGHT = 13;
    private static final GaugeBar BAR = new GaugeBar(34, 70);
    private static final double PERCENT = 100.0;

    private static final int PIP_RADIUS = 4;
    private static final int PIP_GAP = 2;

    /** Ticks per second, for converting {@link DeltaTracker}'s realtime delta into seconds. */
    private static final float TICKS_PER_SECOND = 20.0F;
    /** How much of the remaining distance to target a bar's fill closes per second of real time. */
    private static final double BAR_SMOOTHING_SPEED = 10.0;

    private double xpDisplayedFraction = 0.0;
    private double spiritDisplayedFraction = 0.0;

    @Override
    public void render(final GuiGraphics graphics, final DeltaTracker deltaTracker) {
        final HudSyncPayload hud = HudClientData.latest();
        if (!ClientConfig.HUD_VISIBLE.getAsBoolean() || hud == null) {
            return;
        }

        final float dtSeconds = deltaTracker.getRealtimeDeltaTicks() / TICKS_PER_SECOND;
        final Font font = Minecraft.getInstance().font;
        final boolean showSpiritBar = hud.spiritEnergy().max() > 0.0;

        final Component levelText = Component.translatable("soulland.hud.level", hud.level());
        final Component qiText = Component.translatable("soulland.hud.qi", hud.qi());
        final List<Component> soulLines = soulLines(hud);

        final int rowCount = 4 + (showSpiritBar ? 1 : 0) + soulLines.size();
        final int panelHeight = PANEL_PADDING * 2 + rowCount * ROW_HEIGHT;
        final int panelWidth = panelWidthFor(font, levelText, qiText, soulLines);
        final HudAnchor anchor = ClientConfig.HUD_ANCHOR.get();
        final int panelX = anchor.x(graphics.guiWidth(), panelWidth, SCREEN_MARGIN);
        final int panelY = anchor.y(graphics.guiHeight(), panelHeight, SCREEN_MARGIN);

        SoulLandStyle.drawPanel(graphics, panelX, panelY, panelWidth, panelHeight);

        final int contentX = panelX + PANEL_PADDING;
        int y = panelY + PANEL_PADDING;

        graphics.drawString(font, levelText, contentX, y, SoulLandStyle.HEADING_COLOR, true);
        y += ROW_HEIGHT;

        drawRingPips(graphics, contentX, y, hud.ringTiers());
        y += ROW_HEIGHT;

        final double rawXpProgress = safeRatio(hud.xp());
        xpDisplayedFraction = smoothTowards(xpDisplayedFraction, clampToBar(rawXpProgress), dtSeconds);
        final int xpBarColor = hud.inBottleneck() ? SoulLandStyle.XP_BAR_BOTTLENECK_COLOR : SoulLandStyle.XP_BAR_COLOR;
        BAR.draw(graphics, font, contentX, y, new GaugeBar.Fill(Component.translatable("soulland.hud.xp_label"),
                xpDisplayedFraction, rawXpProgress * PERCENT, xpBarColor));
        y += ROW_HEIGHT;

        if (showSpiritBar) {
            final double rawSpiritProgress = safeRatio(hud.spiritEnergy());
            spiritDisplayedFraction = smoothTowards(spiritDisplayedFraction, clampToBar(rawSpiritProgress), dtSeconds);
            BAR.draw(graphics, font, contentX, y, new GaugeBar.Fill(Component.translatable("soulland.hud.spirit_label"),
                    spiritDisplayedFraction, rawSpiritProgress * PERCENT, SoulLandStyle.SPIRIT_BAR_COLOR));
            y += ROW_HEIGHT;
        }

        graphics.drawString(font, qiText, contentX, y, SoulLandStyle.TEXT_COLOR, true);
        y += ROW_HEIGHT;

        for (final Component line : soulLines) {
            graphics.drawString(font, line, contentX, y, SoulLandStyle.TEXT_COLOR, true);
            y += ROW_HEIGHT;
        }
    }

    /** The active martial soul, then the ring and the bone its cast keys use; a line only for what exists. */
    private static List<Component> soulLines(final HudSyncPayload hud) {
        final List<Component> lines = new ArrayList<>();
        if (!hud.martialSoulName().isEmpty()) {
            lines.add(Component.translatable("soulland.hud.martial_soul_label")
                    .append(Component.literal(hud.martialSoulName())
                            .withStyle(style -> style.withColor(SoulLandStyle.MARTIAL_SOUL_COLOR))));
        }
        if (!hud.ring().isEmpty()) {
            lines.add(selectionLine(Component.translatable("soulland.hud.ring_slot", hud.ring().slot()), hud.ring()));
        }
        if (!hud.bone().isEmpty()) {
            lines.add(selectionLine(Component.translatable("soulland.hud.bone_slot", hud.bone().slot()), hud.bone()));
        }
        return lines;
    }

    /** {@code <slot>: <beast in its tier colour> - <ability>}, the ability left off when there is none. */
    private static Component selectionLine(final MutableComponent slotLabel, final HudSyncPayload.Selection selection) {
        final MutableComponent line = slotLabel.append(Component.literal(selection.sourceName())
                .withStyle(style -> style.withColor(SpiritBeastManager.tierTextColor(selection.tier()))));
        if (!selection.abilityKey().isEmpty()) {
            line.append(Component.translatable("soulland.hud.ability", Component.translatable(selection.abilityKey())));
        }
        return line;
    }

    /** Wide enough for the bars, or for the longest line of text, whichever asks for more. */
    private static int panelWidthFor(final Font font, final Component levelText, final Component qiText,
                                     final List<Component> soulLines) {
        int textWidth = Math.max(font.width(levelText), font.width(qiText));
        for (final Component line : soulLines) {
            textWidth = Math.max(textWidth, font.width(line));
        }
        return Math.max(BAR.totalWidth(), textWidth) + PANEL_PADDING * 2;
    }

    /** One pip per equipped soul ring; nothing drawn for the slots not yet filled. */
    private static void drawRingPips(final GuiGraphics graphics, final int x, final int y, final List<Integer> ringTiers) {
        final int centerY = y + PIP_RADIUS;
        for (int slot = 0; slot < ringTiers.size(); slot++) {
            final int centerX = x + PIP_RADIUS + slot * (PIP_RADIUS * 2 + PIP_GAP);
            final int color = SpiritBeastManager.tierTextColor(ringTiers.get(slot)) | SoulLandStyle.OPAQUE_ALPHA_MASK;
            drawCircle(graphics, centerX, centerY, PIP_RADIUS + 1, SoulLandStyle.OUTLINE_COLOR);
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
