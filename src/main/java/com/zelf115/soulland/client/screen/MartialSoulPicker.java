package com.zelf115.soulland.client.screen;

import com.zelf115.soulland.client.SoulLandStyle;
import com.zelf115.soulland.cultivation.MartialSoul;
import com.zelf115.soulland.network.ChooseMartialSoulPayload;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Lets a player pick a martial soul: a category first (Tool/Beast/Body), then a specific,
 * non-evolution soul from that category. The same steps serve the twin-soul bonus pick.
 */
final class MartialSoulPicker implements CultivationTab {

    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int TITLE_HEIGHT = 16;

    private final Runnable relayout;
    private MartialSoul.Category category;

    MartialSoulPicker(final Runnable relayout) {
        this.relayout = relayout;
    }

    @Override
    public Component title() {
        return Component.translatable("soulland.screen.martial_soul.title");
    }

    @Override
    public void init(final TabArea area, final Consumer<AbstractWidget> addWidget) {
        if (category == null) {
            addCategoryButtons(area, addWidget);
        } else {
            addSoulButtons(area, addWidget);
        }
    }

    @Override
    public void render(final GuiGraphics graphics, final TabArea area, final int mouseX, final int mouseY) {
        graphics.drawCenteredString(Minecraft.getInstance().font, title(), area.centerX(), area.top(),
                SoulLandStyle.HEADING_COLOR);
    }

    private record Choice(Component label, Runnable action) {
    }

    private void addCategoryButtons(final TabArea area, final Consumer<AbstractWidget> addWidget) {
        final List<Choice> choices = Arrays.stream(MartialSoul.Category.values())
                .map(candidate -> new Choice(categoryLabel(candidate), () -> selectCategory(candidate)))
                .toList();
        addButtonGrid(area, choices, addWidget);
    }

    private void addSoulButtons(final TabArea area, final Consumer<AbstractWidget> addWidget) {
        final List<Choice> choices = new ArrayList<>();
        for (final MartialSoul soul : soulsIn(category)) {
            choices.add(new Choice(Component.literal(soul.displayName()), () -> choose(soul)));
        }
        choices.add(new Choice(Component.translatable("gui.back"), () -> selectCategory(null)));
        addButtonGrid(area, choices, addWidget);
    }

    /**
     * Lays the buttons out in as few columns as fit under the title, column by column, and centres
     * the block; the Beast category alone holds more souls than one column has room for.
     */
    private static void addButtonGrid(final TabArea area, final List<Choice> choices,
                                      final Consumer<AbstractWidget> addWidget) {
        final int rowStep = BUTTON_HEIGHT + BUTTON_GAP;
        final int rowsThatFit = Math.max(1, (area.height() - TITLE_HEIGHT + BUTTON_GAP) / rowStep);
        final int columns = Math.ceilDiv(choices.size(), rowsThatFit);
        final int rows = Math.ceilDiv(choices.size(), columns);
        final int buttonWidth = Math.min(BUTTON_WIDTH, (area.width() - BUTTON_GAP * (columns - 1)) / columns);
        final int gridWidth = columns * buttonWidth + (columns - 1) * BUTTON_GAP;
        final int gridLeft = area.centerX() - gridWidth / 2;
        final int gridTop = area.top() + TITLE_HEIGHT + Math.max(0, (area.height() - TITLE_HEIGHT - rows * rowStep) / 2);
        for (int index = 0; index < choices.size(); index++) {
            final Choice choice = choices.get(index);
            final int x = gridLeft + (index / rows) * (buttonWidth + BUTTON_GAP);
            final int y = gridTop + (index % rows) * rowStep;
            addWidget.accept(Button.builder(choice.label(), pressed -> choice.action().run())
                    .bounds(x, y, buttonWidth, BUTTON_HEIGHT).build());
        }
    }

    private static Component categoryLabel(final MartialSoul.Category category) {
        return Component.translatable("soulland.screen.martial_soul.category." + category.name().toLowerCase(Locale.ROOT));
    }

    private void selectCategory(final MartialSoul.Category selected) {
        category = selected;
        relayout.run();
    }

    private static List<MartialSoul> soulsIn(final MartialSoul.Category category) {
        return Arrays.stream(MartialSoul.values())
                .filter(soul -> soul.category() == category && !soul.isEvolution())
                .toList();
    }

    /** The screen swaps to the tabs on its own once the server's record shows the choice. */
    private void choose(final MartialSoul soul) {
        PacketDistributor.sendToServer(new ChooseMartialSoulPayload(soul.ordinal()));
        category = null;
    }
}
