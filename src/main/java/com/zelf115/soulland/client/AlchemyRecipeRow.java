package com.zelf115.soulland.client;

import com.zelf115.soulland.recipe.AlchemyPillRecipe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

/**
 * One brewable pill, drawn as a whole clickable row: its name and what it costs. Clicking
 * anywhere on the row brews it, so there is no separate craft button and nothing to select first.
 */
public final class AlchemyRecipeRow extends AbstractButton {

    private static final int TEXT_INSET = 7;
    private static final int NAME_TOP = 5;
    private static final int COST_TOP = 16;
    private static final int COST_LINE_HEIGHT = 10;
    /** Two lines of cost is all a row has space for; beyond that the hover tooltip carries the rest. */
    private static final int MAX_COST_LINES = 2;
    private static final int ELLIPSIS_WIDTH = 6;

    private static final int BACKGROUND = 0x30FFFFFF;
    private static final int BACKGROUND_HOVERED = 0x55FFFFFF;
    /** Colours drawn straight to the screen carry their alpha; text styles carry plain rgb. */
    private static final int NAME_ARGB = 0xFFFFFFFF;
    private static final int COST_FALLBACK_ARGB = 0xFFB4B4B4;
    private static final int NAME_RGB = 0xFFFFFF;
    private static final int STOCKED_RGB = 0xB4B4B4;
    private static final int MISSING_RGB = 0xE86A6A;
    private static final int LEVEL_RGB = 0x9BD1FF;

    private static final String ELLIPSIS = "…";
    private static final String COST_SEPARATOR = ", ";

    private final AlchemyPillRecipe recipe;
    private final double costMultiplier;
    private final Runnable brew;

    public AlchemyRecipeRow(final int x, final int y, final int width, final int height,
                            final AlchemyPillRecipe recipe, final double costMultiplier, final Runnable brew) {
        super(x, y, width, height, Component.translatable(recipe.result().item().getDescriptionId()));
        this.recipe = recipe;
        this.costMultiplier = costMultiplier;
        this.brew = brew;
    }

    @Override
    public void onPress() {
        brew.run();
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        final var font = Minecraft.getInstance().font;

        graphics.fill(getX(), getY(), getX() + width, getY() + height, backgroundColor());

        final int textWidth = width - TEXT_INSET * 2;
        graphics.drawString(font, clip(getMessage().getString(), textWidth), getX() + TEXT_INSET, getY() + NAME_TOP,
                NAME_ARGB, false);

        final List<Component> costLines = costLines(textWidth);
        for (int index = 0; index < costLines.size(); index++) {
            graphics.drawString(font, costLines.get(index), getX() + TEXT_INSET,
                    getY() + COST_TOP + index * COST_LINE_HEIGHT, COST_FALLBACK_ARGB, false);
        }
    }

    private int backgroundColor() {
        return isHovered() ? BACKGROUND_HOVERED : BACKGROUND;
    }

    /**
     * The cost, each ingredient coloured on its own so only the ones the player is short of read
     * as missing. Ingredients that will not fit beside each other wrap onto the row's second line
     * rather than being cut off, since a recipe's later ingredients matter as much as its first.
     */
    private List<Component> costLines(final int maxWidth) {
        final var font = Minecraft.getInstance().font;
        final List<Component> lines = new ArrayList<>();
        MutableComponent line = Component.empty();
        int usedWidth = 0;
        for (final AlchemyPillRecipe.CostEntry entry : recipe.ingredients()) {
            final Component part = costPart(entry);
            final int partWidth = font.width(part) + (usedWidth == 0 ? 0 : font.width(COST_SEPARATOR));
            if (usedWidth > 0 && usedWidth + partWidth > maxWidth) {
                if (lines.size() + 1 >= MAX_COST_LINES) {
                    line.append(separator()).append(ellipsis());
                    break;
                }
                lines.add(line);
                line = Component.empty();
                usedWidth = 0;
            }
            if (usedWidth > 0) {
                line.append(separator());
            }
            line.append(part);
            usedWidth += partWidth;
        }
        lines.add(line);
        return lines;
    }

    private Component costPart(final AlchemyPillRecipe.CostEntry entry) {
        final String text = AlchemyPillRecipe.costFor(entry, costMultiplier) + "x " + describeIngredient(entry);
        return Component.literal(text).withStyle(style -> style.withColor(ingredientColor(entry)));
    }

    private static Component separator() {
        return Component.literal(COST_SEPARATOR).withStyle(style -> style.withColor(STOCKED_RGB));
    }

    private static Component ellipsis() {
        return Component.literal(ELLIPSIS).withStyle(style -> style.withColor(STOCKED_RGB));
    }

    private int ingredientColor(final AlchemyPillRecipe.CostEntry entry) {
        return isStocked(entry) ? STOCKED_RGB : MISSING_RGB;
    }

    private boolean isStocked(final AlchemyPillRecipe.CostEntry entry) {
        return carried(entry) >= AlchemyPillRecipe.costFor(entry, costMultiplier);
    }

    private int carried(final AlchemyPillRecipe.CostEntry entry) {
        final Player player = Minecraft.getInstance().player;
        return player == null ? 0 : AlchemyPillRecipe.carriedCount(player, entry);
    }

    /** The full requirement list, shown on hover where there is room for every ingredient. */
    public List<Component> tooltipLines() {
        final List<Component> lines = new ArrayList<>();
        lines.add(getMessage().copy().withStyle(style -> style.withColor(NAME_RGB)));
        lines.add(describeUsableLevels().copy().withStyle(style -> style.withColor(LEVEL_RGB)));
        lines.add(Component.translatable("soulland.alchemy.tooltip.requires")
                .withStyle(style -> style.withColor(STOCKED_RGB)));
        for (final AlchemyPillRecipe.CostEntry entry : recipe.ingredients()) {
            lines.add(Component.translatable("soulland.alchemy.tooltip.entry",
                            carried(entry), AlchemyPillRecipe.costFor(entry, costMultiplier), describeIngredient(entry))
                    .withStyle(style -> style.withColor(ingredientColor(entry))));
        }
        return lines;
    }

    /** The levels the pill does anything for once eaten; brewing it is open to everyone. */
    private Component describeUsableLevels() {
        if (recipe.maxLevel() >= Integer.MAX_VALUE) {
            return Component.translatable("soulland.alchemy.level_range_open", recipe.minLevel());
        }
        return Component.translatable("soulland.alchemy.level_range", recipe.minLevel(), recipe.maxLevel());
    }

    private static String describeIngredient(final AlchemyPillRecipe.CostEntry entry) {
        final var items = entry.ingredient().getItems();
        return items.length > 0 ? items[0].getHoverName().getString() : "?";
    }

    /** Trims text to the space the row can spare, marking anything cut off. */
    private static String clip(final String text, final int maxWidth) {
        final var font = Minecraft.getInstance().font;
        if (font.width(text) <= maxWidth) {
            return text;
        }
        return font.plainSubstrByWidth(text, Math.max(0, maxWidth - ELLIPSIS_WIDTH)) + ELLIPSIS;
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
