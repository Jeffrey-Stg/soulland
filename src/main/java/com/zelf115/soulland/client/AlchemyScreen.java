package com.zelf115.soulland.client;

import com.zelf115.soulland.menu.AlchemyMenu;
import com.zelf115.soulland.recipe.AlchemyPillRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Lists the pills the alchemy furnace can make, with their cost and a craft button per row.
 * There's no grid to place items in - crafting is a straight "spend ingredients, get pill" swap.
 */
public final class AlchemyScreen extends AbstractContainerScreen<AlchemyMenu> {
    private static final int PANEL_MARGIN = 8;
    private static final int ROW_HEIGHT = 22;
    private static final int ROW_TOP = 24;
    private static final int NAME_X = 8;
    private static final int BUTTON_WIDTH = 60;
    private static final int BUTTON_HEIGHT = 16;
    private static final int BUTTON_RIGHT_MARGIN = 8;
    private static final int PANEL_COLOR = 0xC0101010;
    private static final int TEXT_COLOR = 0xFFFFFF;

    public AlchemyScreen(final AlchemyMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 220;
        this.imageHeight = ROW_TOP + Math.max(1, menu.recipes().size()) * ROW_HEIGHT + PANEL_MARGIN;
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        // This menu has no player-inventory slots to label, unlike the vanilla screens
        // this class descends from.
        graphics.drawString(font, title, titleLabelX, titleLabelY, TEXT_COLOR, false);
    }

    @Override
    protected void init() {
        super.init();
        final int buttonX = leftPos + imageWidth - BUTTON_WIDTH - BUTTON_RIGHT_MARGIN;
        final var recipes = menu.recipes();
        for (int row = 0; row < recipes.size(); row++) {
            final int recipeId = row;
            final int buttonY = topPos + ROW_TOP + row * ROW_HEIGHT;
            addRenderableWidget(Button.builder(Component.translatable("soulland.alchemy.craft"),
                            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, recipeId))
                    .bounds(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        }
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTick, final int mouseX, final int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL_COLOR);

        final var recipes = menu.recipes();
        for (int row = 0; row < recipes.size(); row++) {
            final int textY = topPos + ROW_TOP + row * ROW_HEIGHT + BUTTON_HEIGHT / 2 - font.lineHeight / 2;
            graphics.drawString(font, describeRecipe(recipes.get(row).value()), leftPos + NAME_X, textY, TEXT_COLOR, false);
        }
    }

    private Component describeRecipe(final AlchemyPillRecipe recipe) {
        final Component name = Component.translatable(recipe.result().item().getDescriptionId());
        final Component cost = describeCost(recipe);
        return Component.empty().append(name).append(" - ").append(cost.copy().withStyle(ChatFormatting.GRAY));
    }

    private Component describeCost(final AlchemyPillRecipe recipe) {
        final MutableComponentJoiner joiner = new MutableComponentJoiner();
        for (final AlchemyPillRecipe.CostEntry entry : recipe.ingredients()) {
            joiner.add(entry.count() + "x " + describeIngredient(entry));
        }
        return Component.literal(joiner.toString());
    }

    private String describeIngredient(final AlchemyPillRecipe.CostEntry entry) {
        final var items = entry.ingredient().getItems();
        return items.length > 0 ? items[0].getHoverName().getString() : "?";
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** Joins ingredient descriptions with ", " without pulling in a full text-formatting dependency. */
    private static final class MutableComponentJoiner {
        private static final String SEPARATOR = ", ";
        private final StringBuilder builder = new StringBuilder();

        void add(final String part) {
            if (!builder.isEmpty()) {
                builder.append(SEPARATOR);
            }
            builder.append(part);
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }
}
