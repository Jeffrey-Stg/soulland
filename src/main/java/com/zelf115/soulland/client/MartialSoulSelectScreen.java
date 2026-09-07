package com.zelf115.soulland.client;

import com.zelf115.soulland.cultivation.MartialSoul;
import com.zelf115.soulland.network.ChooseMartialSoulPayload;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Lets a player without a martial soul pick one: a category first (Tool/Beast/Body), then a
 * specific, non-evolution soul from that category.
 */
public final class MartialSoulSelectScreen extends Screen {
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int CATEGORY_TOP_OFFSET = 30;

    private MartialSoul.Category category;

    private MartialSoulSelectScreen() {
        super(Component.translatable("soulland.screen.martial_soul.title"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new MartialSoulSelectScreen());
    }

    @Override
    protected void init() {
        clearWidgets();
        if (category == null) {
            initCategoryStep();
        } else {
            initSoulStep();
        }
    }

    private void initCategoryStep() {
        int y = height / 2 - CATEGORY_TOP_OFFSET;
        for (final MartialSoul.Category candidate : MartialSoul.Category.values()) {
            addRenderableWidget(Button.builder(categoryLabel(candidate), button -> selectCategory(candidate))
                    .bounds(width / 2 - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
            y += BUTTON_HEIGHT + BUTTON_GAP;
        }
    }

    private static Component categoryLabel(final MartialSoul.Category category) {
        return Component.translatable("soulland.screen.martial_soul.category." + category.name().toLowerCase(Locale.ROOT));
    }

    private void selectCategory(final MartialSoul.Category selected) {
        this.category = selected;
        init();
    }

    private void initSoulStep() {
        final List<MartialSoul> souls = soulsIn(category);
        int y = height / 2 - (souls.size() * (BUTTON_HEIGHT + BUTTON_GAP)) / 2;
        for (final MartialSoul soul : souls) {
            addRenderableWidget(Button.builder(Component.literal(soul.displayName()), button -> choose(soul))
                    .bounds(width / 2 - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
            y += BUTTON_HEIGHT + BUTTON_GAP;
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> back())
                .bounds(width / 2 - BUTTON_WIDTH / 2, y + BUTTON_GAP, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    private void back() {
        this.category = null;
        init();
    }

    private static List<MartialSoul> soulsIn(final MartialSoul.Category category) {
        return Arrays.stream(MartialSoul.values())
                .filter(soul -> soul.category() == category && !soul.isEvolution())
                .toList();
    }

    private void choose(final MartialSoul soul) {
        PacketDistributor.sendToServer(new ChooseMartialSoulPayload(soul.ordinal()));
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
