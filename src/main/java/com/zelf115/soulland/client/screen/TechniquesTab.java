package com.zelf115.soulland.client.screen;

import com.zelf115.soulland.client.GaugeBar;
import com.zelf115.soulland.client.SoulLandStyle;
import com.zelf115.soulland.cultivation.technique.LearnedTechniques;
import com.zelf115.soulland.cultivation.technique.Technique;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Each learned technique as a card with its level and the progress toward the next, as
 * {@code /cultivation techniques} lists them, in a list that scrolls.
 */
final class TechniquesTab implements CultivationTab {

    private static final int CARD_HEIGHT = 30;
    private static final int TEXT_INSET = 7;
    private static final int NAME_TOP = 5;
    private static final int BAR_TOP = 17;
    private static final int BAR_RESERVE = 40;
    private static final int TECHNIQUE_BAR_COLOR = 0xFF6FD08C;
    private static final double PERCENT = 100.0;

    private final CardList list = new CardList();

    @Override
    public Component title() {
        return Component.translatable("soulland.screen.cultivation.tab.techniques");
    }

    @Override
    public void render(final GuiGraphics graphics, final TabArea area, final int mouseX, final int mouseY) {
        final LearnedTechniques techniques = ClientCultivation.data().getTechniques();
        if (techniques.learned().isEmpty()) {
            graphics.drawString(font(), Component.translatable("soulland.command.techniques.none"), area.left(),
                    area.top(), SoulLandStyle.MUTED_TEXT_COLOR, true);
            return;
        }
        list.render(graphics, area, cards(techniques), mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(final TabArea area, final double mouseX, final double mouseY, final double scrollY) {
        return list.scroll(area, mouseX, mouseY, scrollY);
    }

    private static List<TechniqueCard> cards(final LearnedTechniques techniques) {
        return Arrays.stream(Technique.values())
                .filter(techniques::isLearned)
                .map(technique -> new TechniqueCard(technique, techniques))
                .toList();
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    private record TechniqueCard(Technique technique, LearnedTechniques techniques) implements CardList.Card {

        @Override
        public int height() {
            return CARD_HEIGHT;
        }

        @Override
        public void draw(final GuiGraphics graphics, final int x, final int y, final int width, final boolean hovered) {
            graphics.fill(x, y, x + width, y + CARD_HEIGHT,
                    hovered ? CardList.CARD_BACKGROUND_HOVERED : CardList.CARD_BACKGROUND);
            graphics.drawString(font(), technique.displayName(), x + TEXT_INSET, y + NAME_TOP,
                    SoulLandStyle.HEADING_COLOR, false);

            final Component level = Component.translatable("soulland.screen.techniques.level",
                    techniques.level(technique), technique.maxLevel());
            graphics.drawString(font(), level, x + width - TEXT_INSET - font().width(level), y + NAME_TOP,
                    SoulLandStyle.TEXT_COLOR, false);

            final int percent = technique.percentToNextLevel(techniques.getProgress(technique));
            new GaugeBar(0, width - TEXT_INSET * 2 - BAR_RESERVE).draw(graphics, font(), x + TEXT_INSET, y + BAR_TOP,
                    new GaugeBar.Fill(Component.empty(), percent / PERCENT, percent, TECHNIQUE_BAR_COLOR));
        }
    }
}
