package com.zelf115.soulland.client.station;

import com.zelf115.soulland.client.SoulLandStyle;
import com.zelf115.soulland.network.OpenTournamentScreenPayload;
import com.zelf115.soulland.network.TournamentFightPayload;
import com.zelf115.soulland.tournament.SoulMasterStats;
import com.zelf115.soulland.tournament.TournamentManager;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** The tournament registry: the ten rounds and their opponents' levels, the reset countdown, and Fight. */
public final class TournamentScreen extends Screen {

    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 220;
    private static final int PADDING = 10;
    private static final int ROW_HEIGHT = 13;
    private static final int TITLE_GAP = 16;
    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 20;
    private static final int DONE_COLOR = 0xFF6FD08C;
    private static final long MILLIS_PER_SECOND = 1_000L;
    private static final long SECONDS_PER_MINUTE = 60L;
    private static final long MINUTES_PER_HOUR = 60L;

    private final OpenTournamentScreenPayload view;
    /** The countdown runs on locally from what the server measured when the screen opened. */
    private final long openedAtMillis = System.currentTimeMillis();

    private TournamentScreen(final OpenTournamentScreenPayload view) {
        super(Component.translatable("soulland.screen.tournament.title"));
        this.view = view;
    }

    public static void open(final OpenTournamentScreenPayload view) {
        Minecraft.getInstance().setScreen(new TournamentScreen(view));
    }

    @Override
    protected void init() {
        final Button fight = Button.builder(fightLabel(), pressed -> fight())
                .bounds(width / 2 - BUTTON_WIDTH / 2, panelTop() + PANEL_HEIGHT - PADDING - BUTTON_HEIGHT,
                        BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        fight.active = !view.spent() && !view.opponentWaiting();
        addRenderableWidget(fight);
    }

    private Component fightLabel() {
        return Component.translatable("soulland.screen.tournament.fight", Math.max(1, view.round()));
    }

    /** Closes the screen so the player faces the opponent the server sends in. */
    private void fight() {
        PacketDistributor.sendToServer(new TournamentFightPayload(view.pos()));
        onClose();
    }

    @Override
    public void renderBackground(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        SoulLandStyle.drawPanel(graphics, panelLeft(), panelTop(), PANEL_WIDTH, PANEL_HEIGHT);
        int y = panelTop() + PADDING;
        graphics.drawCenteredString(font, title, width / 2, y, SoulLandStyle.HEADING_COLOR);
        y += TITLE_GAP;
        graphics.drawCenteredString(font, statusLine(), width / 2, y, SoulLandStyle.TEXT_COLOR);
        y += TITLE_GAP;
        for (int round = 1; round <= TournamentManager.TOTAL_ROUNDS; round++) {
            drawRound(graphics, round, y);
            y += ROW_HEIGHT;
        }
    }

    private Component statusLine() {
        if (view.spent()) {
            return Component.translatable("soulland.screen.tournament.next_run", countdown());
        }
        if (view.opponentWaiting()) {
            return Component.translatable("soulland.screen.tournament.opponent_waiting");
        }
        if (view.round() == 0) {
            return Component.translatable("soulland.screen.tournament.ready");
        }
        return Component.translatable("soulland.screen.tournament.round", view.round(), TournamentManager.TOTAL_ROUNDS);
    }

    private String countdown() {
        final long remaining = Math.max(0L, view.millisUntilReset() - (System.currentTimeMillis() - openedAtMillis));
        final long totalSeconds = remaining / MILLIS_PER_SECOND;
        final long hours = totalSeconds / (SECONDS_PER_MINUTE * MINUTES_PER_HOUR);
        final long minutes = totalSeconds / SECONDS_PER_MINUTE % MINUTES_PER_HOUR;
        final long seconds = totalSeconds % SECONDS_PER_MINUTE;
        return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    /** Rounds already won in green, the round being fought in gold, the rest muted. */
    private void drawRound(final GuiGraphics graphics, final int round, final int y) {
        final Component label = Component.translatable("soulland.screen.tournament.round_entry", round,
                SoulMasterStats.opponentLevelForRound(round));
        graphics.drawString(font, label, panelLeft() + PADDING, y, roundColor(round), true);
    }

    private int roundColor(final int round) {
        if (view.round() == 0 || view.spent()) return SoulLandStyle.TEXT_COLOR;
        if (round < view.round()) return DONE_COLOR;
        if (round == view.round()) return SoulLandStyle.HEADING_COLOR;
        return SoulLandStyle.MUTED_TEXT_COLOR;
    }

    private int panelLeft() {
        return (width - PANEL_WIDTH) / 2;
    }

    private int panelTop() {
        return (height - PANEL_HEIGHT) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
