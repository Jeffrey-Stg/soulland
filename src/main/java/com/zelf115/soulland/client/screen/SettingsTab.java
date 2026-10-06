package com.zelf115.soulland.client.screen;

import com.zelf115.soulland.ClientConfig;
import com.zelf115.soulland.client.HudAnchor;
import com.zelf115.soulland.network.CultivationActionPayload;
import com.zelf115.soulland.network.SetMovementUsagePayload;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The HUD's visibility and position, which this client keeps, and the movement speed and stat
 * gain, which the server keeps for the player.
 */
final class SettingsTab implements CultivationTab {

    private static final int WIDGET_WIDTH = 220;
    private static final int WIDGET_HEIGHT = 20;
    private static final int WIDGET_GAP = 6;
    private static final double PERCENT = 100.0;

    @Override
    public Component title() {
        return Component.translatable("soulland.screen.cultivation.tab.settings");
    }

    @Override
    public void init(final TabArea area, final Consumer<AbstractWidget> addWidget) {
        final int x = area.centerX() - WIDGET_WIDTH / 2;
        final int step = WIDGET_HEIGHT + WIDGET_GAP;
        int y = area.top();

        addWidget.accept(CycleButton.onOffBuilder(ClientConfig.HUD_VISIBLE.getAsBoolean())
                .create(x, y, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable("soulland.screen.settings.hud_visible"),
                        (button, visible) -> saveClientSetting(() -> ClientConfig.HUD_VISIBLE.set(visible))));
        y += step;

        addWidget.accept(CycleButton.builder((HudAnchor anchor) -> Component.translatable(anchor.translationKey()))
                .withValues(HudAnchor.values())
                .withInitialValue(ClientConfig.HUD_ANCHOR.get())
                .create(x, y, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable("soulland.screen.settings.hud_anchor"),
                        (button, anchor) -> saveClientSetting(() -> ClientConfig.HUD_ANCHOR.set(anchor))));
        y += step;

        addWidget.accept(new MovementSlider(x, y, ClientCultivation.data().getMovementUsagePercent()));
        y += step;

        addWidget.accept(CycleButton.onOffBuilder(ClientCultivation.data().areModStatsEnabled())
                .create(x, y, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable("soulland.screen.settings.mod_stats"),
                        (button, enabled) -> PacketDistributor.sendToServer(
                                new CultivationActionPayload(CultivationActionPayload.TOGGLE_MOD_STATS))));
    }

    @Override
    public void render(final GuiGraphics graphics, final TabArea area, final int mouseX, final int mouseY) {
    }

    private static void saveClientSetting(final Runnable change) {
        change.run();
        ClientConfig.SPEC.save();
    }

    /** Sends each whole-percent step to the server as the slider moves. */
    private static final class MovementSlider extends AbstractSliderButton {

        private int sentPercent;

        MovementSlider(final int x, final int y, final int percent) {
            super(x, y, WIDGET_WIDTH, WIDGET_HEIGHT, Component.empty(), percent / PERCENT);
            this.sentPercent = percent;
            updateMessage();
        }

        private int percent() {
            return (int) Math.round(value * PERCENT);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("soulland.screen.settings.movement", percent()));
        }

        @Override
        protected void applyValue() {
            if (percent() == sentPercent) return;
            sentPercent = percent();
            PacketDistributor.sendToServer(new SetMovementUsagePayload(sentPercent));
        }
    }
}
