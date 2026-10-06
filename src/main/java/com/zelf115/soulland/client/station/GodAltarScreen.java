package com.zelf115.soulland.client.station;

import com.zelf115.soulland.client.SoulLandStyle;
import com.zelf115.soulland.client.screen.ClientCultivation;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.network.AltarActionPayload;
import com.zelf115.soulland.network.OpenAltarScreenPayload;
import com.zelf115.soulland.trial.GodTrial;
import com.zelf115.soulland.trial.GodTrialManager;
import com.zelf115.soulland.trial.TrialTask;
import com.zelf115.soulland.trial.TrialTasks;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * A god's altar: the trial's five tasks as cards, finished ones with a Claim button while their
 * reward waits, the current one with its progress, and the ones still ahead locked.
 */
public final class GodAltarScreen extends Screen {

    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 248;
    private static final int PADDING = 10;
    private static final int TITLE_GAP = 16;
    private static final int CARD_HEIGHT = 30;
    private static final int CARD_GAP = 4;
    private static final int CARD_TEXT_INSET = 6;
    private static final int CLAIM_WIDTH = 60;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BEGIN_WIDTH = 140;
    private static final int CARD_BACKGROUND_COLOR = 0xFF1E1830;
    private static final int DONE_COLOR = 0xFF6FD08C;

    private enum CardState { DONE, CURRENT, LOCKED }

    /** What the screen's buttons were laid out for, so they are laid out again when it changes. */
    private record LayoutKey(GodTrial trial, int taskIndex, int pendingRewards) {
    }

    private final OpenAltarScreenPayload view;
    private final GodTrial altarTrial;
    private LayoutKey laidOutFor;

    private GodAltarScreen(final OpenAltarScreenPayload view) {
        super(Component.translatable("soulland.screen.altar.title"));
        this.view = view;
        this.altarTrial = GodTrial.values()[view.trialOrdinal()];
    }

    public static void open(final OpenAltarScreenPayload view) {
        if (view.trialOrdinal() < 0 || view.trialOrdinal() >= GodTrial.values().length) return;
        Minecraft.getInstance().setScreen(new GodAltarScreen(view));
    }

    @Override
    protected void init() {
        final CultivationData data = ClientCultivation.data();
        laidOutFor = layoutKey(data);
        if (!data.hasStartedGodTrial()) {
            addBeginButton(data);
            return;
        }
        if (isThisAltarsTrial(data)) {
            addClaimButtons(data);
        }
    }

    @Override
    public void tick() {
        if (!layoutKey(ClientCultivation.data()).equals(laidOutFor)) {
            rebuildWidgets();
        }
    }

    private static LayoutKey layoutKey(final CultivationData data) {
        return new LayoutKey(data.getGodTrial(), data.getGodTrialTaskIndex(), data.getGodTrialPendingRewards());
    }

    private boolean isThisAltarsTrial(final CultivationData data) {
        return data.getGodTrial() == altarTrial;
    }

    private void addBeginButton(final CultivationData data) {
        final Button begin = Button.builder(Component.translatable("soulland.screen.altar.begin"),
                        pressed -> send(AltarActionPayload.BEGIN))
                .bounds(width / 2 - BEGIN_WIDTH / 2, panelTop() + PANEL_HEIGHT - PADDING - BUTTON_HEIGHT,
                        BEGIN_WIDTH, BUTTON_HEIGHT)
                .build();
        begin.active = altarTrial.acceptsMartialSoulOf(data);
        addRenderableWidget(begin);
    }

    /** One button per finished task whose reward still waits; any of them claims the next reward due. */
    private void addClaimButtons(final CultivationData data) {
        for (int index = 0; index < TrialTasks.TASK_COUNT; index++) {
            if (isUnclaimed(data, index)) {
                addRenderableWidget(Button.builder(Component.translatable("soulland.screen.altar.claim"),
                                pressed -> send(AltarActionPayload.CLAIM))
                        .bounds(panelLeft() + PANEL_WIDTH - PADDING - CLAIM_WIDTH - CARD_TEXT_INSET,
                                cardTop(index) + (CARD_HEIGHT - BUTTON_HEIGHT) / 2, CLAIM_WIDTH, BUTTON_HEIGHT)
                        .build());
            }
        }
    }

    /** The rewards still owed belong to the most recently finished tasks. */
    private static boolean isUnclaimed(final CultivationData data, final int index) {
        final int finished = data.getGodTrialTaskIndex();
        return index < finished && index >= finished - data.getGodTrialPendingRewards();
    }

    private void send(final int action) {
        PacketDistributor.sendToServer(new AltarActionPayload(view.pos(), action));
    }

    @Override
    public void renderBackground(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        final CultivationData data = ClientCultivation.data();
        SoulLandStyle.drawPanel(graphics, panelLeft(), panelTop(), PANEL_WIDTH, PANEL_HEIGHT);
        graphics.drawCenteredString(font, altarTrial.displayName(), width / 2, panelTop() + PADDING,
                SoulLandStyle.HEADING_COLOR);
        drawStatus(graphics, data);
        for (int index = 0; index < TrialTasks.TASK_COUNT; index++) {
            drawCard(graphics, data, index);
        }
    }

    private void drawStatus(final GuiGraphics graphics, final CultivationData data) {
        graphics.drawCenteredString(font, statusLine(data), width / 2, panelTop() + PADDING + TITLE_GAP,
                SoulLandStyle.TEXT_COLOR);
    }

    private Component statusLine(final CultivationData data) {
        if (!data.hasStartedGodTrial()) {
            return altarTrial.acceptsMartialSoulOf(data)
                    ? Component.translatable("soulland.screen.altar.not_started")
                    : Component.translatable("soulland.trial.requires_martial_soul",
                            altarTrial.getRequiredMartialSoul().displayName());
        }
        if (!isThisAltarsTrial(data)) {
            return Component.translatable("soulland.trial.wrong_altar", data.getGodTrial().displayName());
        }
        if (data.isGodTrialFinished()) {
            return Component.translatable("soulland.trial.finished", altarTrial.displayName());
        }
        return Component.translatable("soulland.screen.altar.task_of", data.getGodTrialTaskIndex() + 1,
                TrialTasks.TASK_COUNT);
    }

    private void drawCard(final GuiGraphics graphics, final CultivationData data, final int index) {
        final int left = panelLeft() + PADDING;
        final int top = cardTop(index);
        graphics.fill(left, top, left + PANEL_WIDTH - PADDING * 2, top + CARD_HEIGHT, CARD_BACKGROUND_COLOR);

        final CardState state = cardState(data, index);
        final int textWidth = PANEL_WIDTH - PADDING * 2 - CARD_TEXT_INSET * 2 - CLAIM_WIDTH;
        final List<FormattedCharSequence> lines = font.split(cardText(data, index, state), textWidth);
        int y = top + (CARD_HEIGHT - lines.size() * font.lineHeight) / 2;
        for (final FormattedCharSequence line : lines) {
            graphics.drawString(font, line, left + CARD_TEXT_INSET, y, cardColor(state), true);
            y += font.lineHeight;
        }
    }

    /** Before this god's trial is under way, every card stays locked: the tasks are only revealed one at a time. */
    private CardState cardState(final CultivationData data, final int index) {
        if (!isThisAltarsTrial(data) || index > data.getGodTrialTaskIndex()) return CardState.LOCKED;
        if (index < data.getGodTrialTaskIndex()) return CardState.DONE;
        return CardState.CURRENT;
    }

    private static Component cardText(final CultivationData data, final int index, final CardState state) {
        final Component number = Component.translatable("soulland.screen.altar.task_number", index + 1);
        if (state == CardState.LOCKED || index >= data.getGodTrialTasks().size()) {
            return number.copy().append(Component.translatable("soulland.screen.altar.locked"));
        }
        final TrialTask task = data.getGodTrialTasks().get(index);
        final int progress = state == CardState.DONE ? task.target() : data.getGodTrialProgress();
        return number.copy().append(GodTrialManager.describeTask(task, progress));
    }

    private static int cardColor(final CardState state) {
        return switch (state) {
            case DONE -> DONE_COLOR;
            case CURRENT -> SoulLandStyle.HEADING_COLOR;
            case LOCKED -> SoulLandStyle.MUTED_TEXT_COLOR;
        };
    }

    private int cardTop(final int index) {
        return panelTop() + PADDING + TITLE_GAP * 2 + index * (CARD_HEIGHT + CARD_GAP);
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
