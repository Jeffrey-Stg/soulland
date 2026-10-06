package com.zelf115.soulland.client;

import com.zelf115.soulland.menu.AlchemyMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Lists the pills the alchemy furnace can brew, one clickable row each, in the cultivation screens'
 * panel. There's no grid to place items in - brewing is a straight "spend ingredients, get pill" swap.
 * The list scrolls once it outgrows the screen.
 */
public final class AlchemyScreen extends AbstractContainerScreen<AlchemyMenu> {
    private static final int PANEL_WIDTH = 300;
    private static final int MAX_PANEL_HEIGHT = 240;
    private static final int SCREEN_MARGIN = 20;
    private static final int ROW_HEIGHT = 38;
    private static final int ROW_GAP = 3;
    private static final int PADDING = 8;
    private static final int LIST_TOP = 22;
    private static final int FOOTER_HEIGHT = 18;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLLBAR_GAP = 3;
    private static final int MIN_THUMB_HEIGHT = 12;
    private static final int SCROLL_STEP = ROW_HEIGHT / 2;
    private static final int TRACK_COLOR = 0x40000000;
    private static final int THUMB_COLOR = 0xFF8A6FC0;

    private final List<AlchemyRecipeRow> rows = new ArrayList<>();
    private int scrollOffset;

    public AlchemyScreen(final AlchemyMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = PANEL_WIDTH;
    }

    @Override
    protected void init() {
        imageHeight = Math.min(Math.min(MAX_PANEL_HEIGHT, height - SCREEN_MARGIN),
                LIST_TOP + contentHeight() + FOOTER_HEIGHT);
        super.init();
        rows.clear();
        final var recipes = menu.recipes();
        for (int row = 0; row < recipes.size(); row++) {
            final int recipeId = row;
            rows.add(addWidget(new AlchemyRecipeRow(leftPos + PADDING, 0, rowWidth(), ROW_HEIGHT,
                    recipes.get(row).value(), menu.tier().costMultiplier(),
                    () -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonIdFor(recipeId)))));
        }
        layoutRows();
    }

    /** Read on press, not when the row is built, so holding Shift at click time is what counts. */
    private static int buttonIdFor(final int recipeIndex) {
        return hasShiftDown() ? AlchemyMenu.brewStackButtonId(recipeIndex) : recipeIndex;
    }

    private int contentHeight() {
        return Math.max(0, menu.recipes().size() * (ROW_HEIGHT + ROW_GAP) - ROW_GAP);
    }

    private int listTop() {
        return topPos + LIST_TOP;
    }

    private int listHeight() {
        return imageHeight - LIST_TOP - FOOTER_HEIGHT;
    }

    private boolean overflows() {
        return contentHeight() > listHeight();
    }

    private int rowWidth() {
        final int fullWidth = imageWidth - PADDING * 2;
        return overflows() ? fullWidth - SCROLLBAR_WIDTH - SCROLLBAR_GAP : fullWidth;
    }

    /** Moves each row to the scroll position; a row wholly outside the list can neither draw nor be clicked. */
    private void layoutRows() {
        scrollOffset = Math.max(0, Math.min(Math.max(0, contentHeight() - listHeight()), scrollOffset));
        for (int index = 0; index < rows.size(); index++) {
            final AlchemyRecipeRow row = rows.get(index);
            row.setY(listTop() + index * (ROW_HEIGHT + ROW_GAP) - scrollOffset);
            row.visible = row.getY() + ROW_HEIGHT > listTop() && row.getY() < listTop() + listHeight();
        }
    }

    private boolean isOverList(final double mouseX, final double mouseY) {
        return mouseX >= leftPos && mouseX < leftPos + imageWidth
                && mouseY >= listTop() && mouseY < listTop() + listHeight();
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double scrollX, final double scrollY) {
        if (!isOverList(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        scrollOffset -= (int) Math.signum(scrollY) * SCROLL_STEP;
        layoutRows();
        return true;
    }

    /** A row half scrolled out of view only takes the clicks that land on its visible part. */
    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (!isOverList(mouseX, mouseY) && rows.stream().anyMatch(row -> row.isMouseOver(mouseX, mouseY))) {
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTick, final int mouseX, final int mouseY) {
        SoulLandStyle.drawPanel(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.enableScissor(leftPos, listTop(), leftPos + imageWidth, listTop() + listHeight());
        final boolean mouseOverList = isOverList(mouseX, mouseY);
        for (final AlchemyRecipeRow row : rows) {
            row.render(graphics, mouseOverList ? mouseX : -1, mouseOverList ? mouseY : -1, partialTick);
        }
        graphics.disableScissor();
        if (overflows()) {
            drawScrollbar(graphics);
        }
    }

    private void drawScrollbar(final GuiGraphics graphics) {
        final int trackLeft = leftPos + imageWidth - PADDING - SCROLLBAR_WIDTH;
        final int trackRight = trackLeft + SCROLLBAR_WIDTH;
        graphics.fill(trackLeft, listTop(), trackRight, listTop() + listHeight(), TRACK_COLOR);
        final int thumbHeight = Math.max(MIN_THUMB_HEIGHT, listHeight() * listHeight() / contentHeight());
        final int thumbTop = listTop() + (listHeight() - thumbHeight) * scrollOffset / (contentHeight() - listHeight());
        graphics.fill(trackLeft, thumbTop, trackRight, thumbTop + thumbHeight, THUMB_COLOR);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        // This menu has no player-inventory slots to label, unlike the vanilla screens
        // this class descends from.
        graphics.drawString(font, title, PADDING, PADDING, SoulLandStyle.HEADING_COLOR, true);
        graphics.drawString(font, Component.translatable("soulland.alchemy.hint"),
                PADDING, imageHeight - FOOTER_HEIGHT + (FOOTER_HEIGHT - font.lineHeight) / 2,
                SoulLandStyle.MUTED_TEXT_COLOR, false);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderHoveredRequirements(graphics, mouseX, mouseY);
    }

    /** The hovered recipe spells its whole cost out, which the one-line row cannot always fit. */
    private void renderHoveredRequirements(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        if (!isOverList(mouseX, mouseY)) {
            return;
        }
        rows.stream()
                .filter(row -> row.visible && row.isMouseOver(mouseX, mouseY))
                .findFirst()
                .ifPresent(row -> graphics.renderComponentTooltip(font, row.tooltipLines(), mouseX, mouseY));
    }
}
