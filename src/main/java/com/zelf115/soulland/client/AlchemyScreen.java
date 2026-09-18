package com.zelf115.soulland.client;

import com.zelf115.soulland.menu.AlchemyMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Lists the pills the alchemy furnace can brew, one clickable row each.
 * There's no grid to place items in - brewing is a straight "spend ingredients, get pill" swap.
 */
public final class AlchemyScreen extends AbstractContainerScreen<AlchemyMenu> {
    private static final int PANEL_WIDTH = 256;
    private static final int ROW_HEIGHT = 38;
    private static final int ROW_GAP = 3;
    private static final int LIST_TOP = 24;
    private static final int SIDE_MARGIN = 7;
    private static final int FOOTER_HEIGHT = 18;
    private static final int HINT_BOTTOM_INSET = 13;

    private static final int PANEL_COLOR = 0xE00F0F14;
    private static final int PANEL_BORDER_COLOR = 0xFF3A3A48;
    private static final int TITLE_COLOR = 0xFFFFD98A;
    private static final int HINT_COLOR = 0xFF8A8A96;

    public AlchemyScreen(final AlchemyMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = LIST_TOP + rowCount() * (ROW_HEIGHT + ROW_GAP) + FOOTER_HEIGHT;
    }

    private int rowCount() {
        return Math.max(1, menu.recipes().size());
    }

    @Override
    protected void init() {
        super.init();
        final var recipes = menu.recipes();
        final int rowWidth = imageWidth - SIDE_MARGIN * 2;
        for (int row = 0; row < recipes.size(); row++) {
            final int recipeId = row;
            addRenderableWidget(new AlchemyRecipeRow(
                    leftPos + SIDE_MARGIN, topPos + LIST_TOP + row * (ROW_HEIGHT + ROW_GAP), rowWidth, ROW_HEIGHT,
                    recipes.get(row).value(), menu.tier().costMultiplier(),
                    () -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, recipeId)));
        }
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTick, final int mouseX, final int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL_COLOR);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, PANEL_BORDER_COLOR);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        // This menu has no player-inventory slots to label, unlike the vanilla screens
        // this class descends from.
        graphics.drawString(font, title, SIDE_MARGIN, SIDE_MARGIN, TITLE_COLOR, false);
        graphics.drawString(font, Component.translatable("soulland.alchemy.hint"),
                SIDE_MARGIN, imageHeight - HINT_BOTTOM_INSET, HINT_COLOR, false);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderHoveredRequirements(graphics, mouseX, mouseY);
    }

    /** The hovered recipe spells its whole cost out, which the one-line row cannot always fit. */
    private void renderHoveredRequirements(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        for (final var child : children()) {
            if (child instanceof AlchemyRecipeRow row && row.isHovered()) {
                graphics.renderComponentTooltip(font, row.tooltipLines(), mouseX, mouseY);
                return;
            }
        }
    }
}
