package com.zelf115.soulland.client.screen;

import com.zelf115.soulland.client.SoulLandStyle;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;

/**
 * A vertical list of cards, drawn the way the alchemy furnace lists its recipes, that scrolls with
 * the mouse wheel once it outgrows its area and shows a scrollbar while it does.
 */
final class CardList {

    /** One entry of the list: how tall it is, how it draws, and what a click on it does. */
    interface Card {
        int height();

        void draw(GuiGraphics graphics, int x, int y, int width, boolean hovered);

        default boolean click() {
            return false;
        }

        default void drawTooltip(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        }
    }

    static final int CARD_BACKGROUND = 0x30FFFFFF;
    static final int CARD_BACKGROUND_HOVERED = 0x55FFFFFF;
    private static final int GAP = 3;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLLBAR_GAP = 3;
    private static final int MIN_THUMB_HEIGHT = 12;
    private static final int SCROLL_STEP = 20;
    private static final int TRACK_COLOR = 0x40000000;
    private static final int THUMB_COLOR = 0xFF8A6FC0;

    private int scrollOffset;

    void render(final GuiGraphics graphics, final TabArea area, final List<? extends Card> cards,
                final int mouseX, final int mouseY) {
        scrollOffset = clampScroll(scrollOffset, area, cards);
        final int cardWidth = cardWidth(area, cards);
        graphics.enableScissor(area.left(), area.top(), area.right(), area.bottom());
        int y = area.top() - scrollOffset;
        for (final Card card : cards) {
            final boolean hovered = area.contains(mouseX, mouseY) && isOver(card, area.left(), y, cardWidth, mouseX, mouseY);
            card.draw(graphics, area.left(), y, cardWidth, hovered);
            y += card.height() + GAP;
        }
        graphics.disableScissor();
        if (overflows(area, cards)) {
            drawScrollbar(graphics, area, cards);
        }
    }

    Optional<Card> cardAt(final TabArea area, final List<? extends Card> cards, final double mouseX, final double mouseY) {
        if (!area.contains(mouseX, mouseY)) {
            return Optional.empty();
        }
        final int cardWidth = cardWidth(area, cards);
        int y = area.top() - scrollOffset;
        for (final Card card : cards) {
            if (isOver(card, area.left(), y, cardWidth, mouseX, mouseY)) {
                return Optional.of(card);
            }
            y += card.height() + GAP;
        }
        return Optional.empty();
    }

    boolean scroll(final TabArea area, final double mouseX, final double mouseY, final double scrollY) {
        if (!area.contains(mouseX, mouseY)) {
            return false;
        }
        scrollOffset -= (int) Math.signum(scrollY) * SCROLL_STEP;
        return true;
    }

    private static boolean isOver(final Card card, final int x, final int y, final int width,
                                  final double mouseX, final double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + card.height();
    }

    /** Narrower by the scrollbar's room only when there is something to scroll. */
    private static int cardWidth(final TabArea area, final List<? extends Card> cards) {
        return overflows(area, cards) ? area.width() - SCROLLBAR_WIDTH - SCROLLBAR_GAP : area.width();
    }

    private static int contentHeight(final List<? extends Card> cards) {
        return cards.stream().mapToInt(card -> card.height() + GAP).sum() - GAP;
    }

    private static boolean overflows(final TabArea area, final List<? extends Card> cards) {
        return contentHeight(cards) > area.height();
    }

    private static int clampScroll(final int offset, final TabArea area, final List<? extends Card> cards) {
        return Math.max(0, Math.min(Math.max(0, contentHeight(cards) - area.height()), offset));
    }

    private void drawScrollbar(final GuiGraphics graphics, final TabArea area, final List<? extends Card> cards) {
        final int trackLeft = area.right() - SCROLLBAR_WIDTH;
        graphics.fill(trackLeft, area.top(), area.right(), area.bottom(), TRACK_COLOR);
        final int content = contentHeight(cards);
        final int thumbHeight = Math.max(MIN_THUMB_HEIGHT, area.height() * area.height() / content);
        final int thumbTop = area.top() + (area.height() - thumbHeight) * scrollOffset / (content - area.height());
        graphics.fill(trackLeft, thumbTop, area.right(), thumbTop + thumbHeight, THUMB_COLOR);
        graphics.renderOutline(trackLeft, area.top(), SCROLLBAR_WIDTH, area.height(), SoulLandStyle.OUTLINE_COLOR);
    }
}
