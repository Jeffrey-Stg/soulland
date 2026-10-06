package com.zelf115.soulland.client.screen;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

/** One page of the cultivation screen: the widgets it adds, what it draws, and the clicks it answers. */
public interface CultivationTab {

    Component title();

    /** Adds the tab's widgets; called each time the screen lays itself out again. */
    default void init(final TabArea area, final Consumer<AbstractWidget> addWidget) {
    }

    /** Draws the tab beneath its widgets. */
    void render(GuiGraphics graphics, TabArea area, int mouseX, int mouseY);

    /** Draws any tooltip above everything else. */
    default void renderTooltip(final GuiGraphics graphics, final TabArea area, final int mouseX, final int mouseY) {
    }

    default boolean mouseClicked(final TabArea area, final double mouseX, final double mouseY) {
        return false;
    }

    default boolean mouseScrolled(final TabArea area, final double mouseX, final double mouseY, final double scrollY) {
        return false;
    }
}
