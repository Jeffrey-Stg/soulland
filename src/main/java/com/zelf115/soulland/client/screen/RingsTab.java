package com.zelf115.soulland.client.screen;

import com.zelf115.soulland.client.SoulLandStyle;
import com.zelf115.soulland.cultivation.AbsorbedRing;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.MartialSoul;
import com.zelf115.soulland.cultivation.SoulRingSkills;
import com.zelf115.soulland.cultivation.SoulSlot;
import com.zelf115.soulland.item.SoulRingItem;
import com.zelf115.soulland.item.StatBonusTooltip;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Every absorbed soul ring as a card under the martial soul it belongs to, in a list that scrolls.
 * A soul's heading folds its rings away; hovering a ring shows the tooltip the ring item itself would.
 */
final class RingsTab implements CultivationTab {

    private static final int HEADING_HEIGHT = 12;
    private static final int RING_CARD_HEIGHT = 28;
    private static final int ICON_INSET = 5;
    private static final int TEXT_LEFT = 26;
    private static final int NAME_TOP = 5;
    private static final int DETAIL_TOP = 16;

    private final Set<SoulSlot> collapsed = EnumSet.noneOf(SoulSlot.class);
    private final CardList list = new CardList();

    @Override
    public Component title() {
        return Component.translatable("soulland.screen.cultivation.tab.rings");
    }

    @Override
    public void render(final GuiGraphics graphics, final TabArea area, final int mouseX, final int mouseY) {
        list.render(graphics, area, cards(), mouseX, mouseY);
    }

    @Override
    public void renderTooltip(final GuiGraphics graphics, final TabArea area, final int mouseX, final int mouseY) {
        list.cardAt(area, cards(), mouseX, mouseY).ifPresent(card -> card.drawTooltip(graphics, mouseX, mouseY));
    }

    @Override
    public boolean mouseClicked(final TabArea area, final double mouseX, final double mouseY) {
        return list.cardAt(area, cards(), mouseX, mouseY).map(CardList.Card::click).orElse(false);
    }

    @Override
    public boolean mouseScrolled(final TabArea area, final double mouseX, final double mouseY, final double scrollY) {
        return list.scroll(area, mouseX, mouseY, scrollY);
    }

    private List<CardList.Card> cards() {
        final CultivationData data = ClientCultivation.data();
        final List<CardList.Card> cards = new ArrayList<>();
        for (final SoulSlot slot : SoulSlot.values()) {
            final MartialSoul soul = data.getMartialSoul(slot);
            if (soul == null) continue;
            final List<AbsorbedRing> rings = data.getRings(slot);
            cards.add(new Heading(slot, soul, rings.size()));
            if (!collapsed.contains(slot)) {
                addRingCards(cards, soul, rings);
            }
        }
        return cards;
    }

    private static void addRingCards(final List<CardList.Card> cards, final MartialSoul soul, final List<AbsorbedRing> rings) {
        if (rings.isEmpty()) {
            cards.add(new EmptyNote());
            return;
        }
        for (int index = 0; index < rings.size(); index++) {
            cards.add(new RingCard(index, rings.get(index), soul));
        }
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    /** A martial soul's name and ring count; clicking it folds or unfolds the soul's rings. */
    private final class Heading implements CardList.Card {
        private final SoulSlot slot;
        private final MartialSoul soul;
        private final int ringCount;

        Heading(final SoulSlot slot, final MartialSoul soul, final int ringCount) {
            this.slot = slot;
            this.soul = soul;
            this.ringCount = ringCount;
        }

        @Override
        public int height() {
            return HEADING_HEIGHT;
        }

        @Override
        public void draw(final GuiGraphics graphics, final int x, final int y, final int width, final boolean hovered) {
            final String marker = collapsed.contains(slot) ? "▶ " : "▼ ";
            final Component text = Component.literal(marker + soul.displayName())
                    .append(Component.translatable("soulland.screen.rings.count", ringCount));
            graphics.drawString(font(), text, x, y + 2, hovered ? SoulLandStyle.TEXT_COLOR : SoulLandStyle.HEADING_COLOR, true);
        }

        @Override
        public boolean click() {
            if (!collapsed.remove(slot)) {
                collapsed.add(slot);
            }
            return true;
        }
    }

    /** A ring's icon, its number and beast, then its age and skill. */
    private record RingCard(int index, AbsorbedRing ring, MartialSoul soul) implements CardList.Card {

        @Override
        public int height() {
            return RING_CARD_HEIGHT;
        }

        @Override
        public void draw(final GuiGraphics graphics, final int x, final int y, final int width, final boolean hovered) {
            graphics.fill(x, y, x + width, y + RING_CARD_HEIGHT,
                    hovered ? CardList.CARD_BACKGROUND_HOVERED : CardList.CARD_BACKGROUND);
            graphics.renderItem(stack(), x + ICON_INSET, y + (RING_CARD_HEIGHT - 16) / 2);

            final Component name = Component.literal((index + 1) + ". ")
                    .append(SpiritBeastManager.nameInTierColor(ring.sourceName(), ring.tier()));
            graphics.drawString(font(), name, x + TEXT_LEFT, y + NAME_TOP, SoulLandStyle.TEXT_COLOR, false);

            final Component details = Component.translatable("soulland.screen.rings.details",
                    Component.translatable("soulland.screen.rings.age", StatBonusTooltip.formatYears(ring.years())),
                    skillName().copy().withStyle(style -> style.withColor(SoulLandStyle.MARTIAL_SOUL_COLOR)));
            graphics.drawString(font(), details, x + TEXT_LEFT, y + DETAIL_TOP, SoulLandStyle.MUTED_TEXT_COLOR, false);
        }

        @Override
        public void drawTooltip(final GuiGraphics graphics, final int mouseX, final int mouseY) {
            graphics.renderTooltip(font(), stack(), mouseX, mouseY);
        }

        private ItemStack stack() {
            return SoulRingItem.createFrom(ring);
        }

        private Component skillName() {
            final String abilityKey = SoulRingSkills.abilityKey(soul, index, ring);
            return abilityKey.isEmpty()
                    ? Component.translatable("soulland.screen.rings.no_skill")
                    : Component.translatable(abilityKey);
        }
    }

    /** Stands in for the rings of a soul that has none yet. */
    private record EmptyNote() implements CardList.Card {
        @Override
        public int height() {
            return HEADING_HEIGHT;
        }

        @Override
        public void draw(final GuiGraphics graphics, final int x, final int y, final int width, final boolean hovered) {
            graphics.drawString(font(), Component.translatable("soulland.screen.rings.none"), x + TEXT_LEFT, y + 2,
                    SoulLandStyle.MUTED_TEXT_COLOR, true);
        }
    }
}
