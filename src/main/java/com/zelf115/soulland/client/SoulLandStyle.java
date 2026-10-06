package com.zelf115.soulland.client;

import net.minecraft.client.gui.GuiGraphics;

/** The look the HUD and every cultivation screen share: its colours and its framed panel. */
public final class SoulLandStyle {

    public static final int PANEL_BACKGROUND_COLOR = 0xC0161022;
    public static final int PANEL_BORDER_COLOR = 0xFF4B2E7D;
    // Dimmed rather than pure white so a white-tier (tier 1) name still reads as "colored" against it.
    public static final int TEXT_COLOR = 0xFFB8B8C0;
    public static final int HEADING_COLOR = 0xFFFFAA00;
    public static final int MARTIAL_SOUL_COLOR = 0xD9B3FF;
    public static final int MUTED_TEXT_COLOR = 0xFF6E6A78;
    public static final int XP_BAR_COLOR = 0xFF55DDFF;
    public static final int XP_BAR_BOTTLENECK_COLOR = 0xFFFFC94D;
    public static final int SPIRIT_BAR_COLOR = 0xFF9B6FE0;
    public static final int SLOT_BACKGROUND_COLOR = 0xFF2A2A32;
    public static final int OUTLINE_COLOR = 0xFF000000;
    /** fill(), unlike drawString(), takes a color's alpha literally; ChatFormatting's colors carry none. */
    public static final int OPAQUE_ALPHA_MASK = 0xFF000000;

    private SoulLandStyle() {
    }

    public static void drawPanel(final GuiGraphics graphics, final int left, final int top, final int width, final int height) {
        graphics.fill(left, top, left + width, top + height, PANEL_BORDER_COLOR);
        graphics.fill(left + 1, top + 1, left + width - 1, top + height - 1, PANEL_BACKGROUND_COLOR);
    }
}
