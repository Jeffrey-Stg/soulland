package com.zelf115.soulland.client;

import com.zelf115.soulland.network.OverreachConfirmPayload;
import com.zelf115.soulland.network.OverreachPromptPayload;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Confirmation for absorbing a soul ring above the player's spirit limit: it shows the
 * success chance and spells out that failing kills them.
 */
public final class OverreachConfirmScreen extends Screen {

    private static final int BUTTON_WIDTH = 110;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 8;
    private static final int LINE_HEIGHT = 12;
    private static final int TITLE_OFFSET = 50;
    private static final int BUTTON_OFFSET = 30;

    private final OverreachPromptPayload prompt;

    private OverreachConfirmScreen(final OverreachPromptPayload prompt) {
        super(Component.translatable("soulland.screen.overreach.title"));
        this.prompt = prompt;
    }

    /** Called on the client when the server asks for a confirmation. */
    public static void open(final OverreachPromptPayload prompt) {
        Minecraft.getInstance().setScreen(new OverreachConfirmScreen(prompt));
    }

    @Override
    protected void init() {
        final int buttonY = height / 2 + BUTTON_OFFSET;
        final int leftX = width / 2 - BUTTON_WIDTH - BUTTON_GAP / 2;
        final int rightX = width / 2 + BUTTON_GAP / 2;

        addRenderableWidget(Button.builder(Component.translatable("soulland.screen.overreach.confirm"), button -> confirm())
                .bounds(leftX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(rightX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    private void confirm() {
        PacketDistributor.sendToServer(new OverreachConfirmPayload(prompt.hand()));
        onClose();
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int lineY = height / 2 - TITLE_OFFSET;
        drawCentered(graphics, title.copy().withStyle(ChatFormatting.GOLD), lineY);
        lineY += LINE_HEIGHT * 2;
        drawCentered(graphics, Component.translatable("soulland.screen.overreach.ring",
                SpiritBeastManager.describeTier(prompt.ringTier()),
                SpiritBeastManager.describeTier(prompt.allowedTier())), lineY);
        lineY += LINE_HEIGHT;
        drawCentered(graphics, Component.translatable("soulland.screen.overreach.chance", prompt.successChancePercent())
                .withStyle(ChatFormatting.AQUA), lineY);
        lineY += LINE_HEIGHT;
        drawCentered(graphics, Component.translatable("soulland.screen.overreach.warning").withStyle(ChatFormatting.RED), lineY);
    }

    private void drawCentered(final GuiGraphics graphics, final Component text, final int y) {
        graphics.drawCenteredString(font, text, width / 2, y, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
