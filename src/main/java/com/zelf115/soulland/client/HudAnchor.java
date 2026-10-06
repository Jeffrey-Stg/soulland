package com.zelf115.soulland.client;

import java.util.Locale;

/** Where on the screen the cultivation HUD panel sits: a corner, or the middle of an edge. */
public enum HudAnchor {
    TOP_LEFT(Align.START, Align.START),
    TOP_CENTER(Align.CENTER, Align.START),
    TOP_RIGHT(Align.END, Align.START),
    CENTER_LEFT(Align.START, Align.CENTER),
    CENTER_RIGHT(Align.END, Align.CENTER),
    BOTTOM_LEFT(Align.START, Align.END),
    BOTTOM_CENTER(Align.CENTER, Align.END),
    BOTTOM_RIGHT(Align.END, Align.END);

    private enum Align {
        START,
        CENTER,
        END;

        int place(final int screenSize, final int panelSize, final int margin) {
            return switch (this) {
                case START -> margin;
                case CENTER -> (screenSize - panelSize) / 2;
                case END -> screenSize - panelSize - margin;
            };
        }
    }

    private final Align horizontal;
    private final Align vertical;

    HudAnchor(final Align horizontal, final Align vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public int x(final int screenWidth, final int panelWidth, final int margin) {
        return horizontal.place(screenWidth, panelWidth, margin);
    }

    public int y(final int screenHeight, final int panelHeight, final int margin) {
        return vertical.place(screenHeight, panelHeight, margin);
    }

    public String translationKey() {
        return "soulland.hud_anchor." + name().toLowerCase(Locale.ROOT);
    }
}
