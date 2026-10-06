package com.zelf115.soulland.client.screen;

import com.zelf115.soulland.client.SoulLandStyle;
import com.zelf115.soulland.cultivation.CultivationData;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The cultivation screen: Info, Rings, Bones, Techniques and Settings tabs. A player still owed a
 * martial soul sees only the picker until they choose one.
 */
public final class CultivationScreen extends Screen {

    private static final int PANEL_WIDTH = 330;
    private static final int PANEL_HEIGHT = 210;
    private static final int PADDING = 8;
    private static final int TAB_HEIGHT = 18;
    private static final int TAB_GAP = 2;

    private final List<CultivationTab> tabs = List.of(
            new InfoTab(), new RingsTab(), new BonesTab(), new TechniquesTab(), new SettingsTab());
    private final MartialSoulPicker picker = new MartialSoulPicker(this::rebuildWidgets);
    private CultivationTab activeTab = tabs.getFirst();
    private boolean showingPicker;

    private CultivationScreen() {
        super(Component.translatable("soulland.screen.cultivation.title"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new CultivationScreen());
    }

    @Override
    protected void init() {
        showingPicker = needsPicker();
        if (showingPicker) {
            picker.init(contentArea(), this::addRenderableWidget);
            return;
        }
        addTabButtons();
        activeTab.init(contentArea(), this::addRenderableWidget);
    }

    private static boolean needsPicker() {
        final CultivationData data = ClientCultivation.data();
        return data.getMartialSoul() == null || data.isSecondMartialSoulPending();
    }

    private void addTabButtons() {
        final int tabWidth = (PANEL_WIDTH - PADDING * 2 - TAB_GAP * (tabs.size() - 1)) / tabs.size();
        int x = panelLeft() + PADDING;
        for (final CultivationTab tab : tabs) {
            final Button button = Button.builder(tab.title(), pressed -> select(tab))
                    .bounds(x, panelTop() + PADDING, tabWidth, TAB_HEIGHT).build();
            button.active = tab != activeTab;
            addRenderableWidget(button);
            x += tabWidth + TAB_GAP;
        }
    }

    private void select(final CultivationTab tab) {
        activeTab = tab;
        rebuildWidgets();
    }

    /** Swaps between the picker and the tabs as soon as the synced record says a soul was chosen or is owed. */
    @Override
    public void tick() {
        if (needsPicker() != showingPicker) {
            rebuildWidgets();
        }
    }

    @Override
    public void renderBackground(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        SoulLandStyle.drawPanel(graphics, panelLeft(), panelTop(), PANEL_WIDTH, PANEL_HEIGHT);
        currentTab().render(graphics, contentArea(), mouseX, mouseY);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        currentTab().renderTooltip(graphics, contentArea(), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        return super.mouseClicked(mouseX, mouseY, button) || currentTab().mouseClicked(contentArea(), mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double scrollX, final double scrollY) {
        return currentTab().mouseScrolled(contentArea(), mouseX, mouseY, scrollY)
                || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private CultivationTab currentTab() {
        return showingPicker ? picker : activeTab;
    }

    /** Below the tab buttons when they show; the whole panel inside its padding for the picker. */
    private TabArea contentArea() {
        final int top = panelTop() + PADDING + (showingPicker ? 0 : TAB_HEIGHT + PADDING);
        return new TabArea(panelLeft() + PADDING, top, PANEL_WIDTH - PADDING * 2, panelTop() + PANEL_HEIGHT - PADDING - top);
    }

    private int panelLeft() {
        return (width - PANEL_WIDTH) / 2;
    }

    private int panelTop() {
        return (height - PANEL_HEIGHT) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
