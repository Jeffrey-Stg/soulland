package com.zelf115.soulland.client.screen;

import com.zelf115.soulland.client.SoulLandStyle;
import com.zelf115.soulland.cultivation.AbsorbedBone;
import com.zelf115.soulland.item.SpiritBoneItem;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

/**
 * The player's model with a read-only slot over each limb for the bone absorbed there, and the same
 * slots listed by name beside it, external bones included. Hovering either shows the bone's own tooltip.
 */
final class BonesTab implements CultivationTab {

    private static final int MODEL_BOX_WIDTH = 120;
    private static final int MODEL_SCALE = 70;
    private static final float PLAYER_HEIGHT_BLOCKS = 1.8F;
    private static final int SLOT_SIZE = 18;
    private static final int LIST_ROW_HEIGHT = 20;
    private static final int LIST_TEXT_OFFSET = 5;
    /** Above the player model, which is drawn pushed toward the viewer. */
    private static final float SLOT_LAYER_Z = 200.0F;
    private static final int HOVER_HIGHLIGHT_COLOR = 0x80FFFFFF;

    /**
     * Where each internal bone sits on the model, as fractions of the model's height from its
     * centre. The player faces the viewer, so their right arm and leg show on the viewer's left.
     */
    private record LimbPosition(String slot, float xOffsetHeights, float yFromTopHeights) {
    }

    private static final List<LimbPosition> LIMBS = List.of(
            new LimbPosition("Skull Bone", 0.0F, 0.12F),
            new LimbPosition("Torso Bone", 0.0F, 0.42F),
            new LimbPosition("Right Arm Bone", -0.21F, 0.42F),
            new LimbPosition("Left Arm Bone", 0.21F, 0.42F),
            new LimbPosition("Right Leg Bone", -0.09F, 0.80F),
            new LimbPosition("Left Leg Bone", 0.09F, 0.80F));

    private record Slot(String name, int x, int y) {
        boolean contains(final double mouseX, final double mouseY) {
            return mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE;
        }
    }

    @Override
    public Component title() {
        return Component.translatable("soulland.screen.cultivation.tab.bones");
    }

    @Override
    public void render(final GuiGraphics graphics, final TabArea area, final int mouseX, final int mouseY) {
        InventoryScreen.renderEntityInInventoryFollowsAngle(graphics, area.left(), area.top(),
                area.left() + MODEL_BOX_WIDTH, area.bottom(), MODEL_SCALE, 0.0F, 0.0F, 0.0F, ClientCultivation.player());

        final Map<String, AbsorbedBone> bones = ClientCultivation.data().getSpiritBones();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, SLOT_LAYER_Z);
        for (final Slot slot : allSlots(area)) {
            drawSlot(graphics, slot, boneIn(bones, slot.name()), slot.contains(mouseX, mouseY));
        }
        graphics.pose().popPose();
        drawSlotNames(graphics, area, bones);
    }

    @Override
    public void renderTooltip(final GuiGraphics graphics, final TabArea area, final int mouseX, final int mouseY) {
        final Map<String, AbsorbedBone> bones = ClientCultivation.data().getSpiritBones();
        allSlots(area).stream()
                .filter(slot -> slot.contains(mouseX, mouseY))
                .findFirst()
                .flatMap(slot -> boneIn(bones, slot.name()))
                .ifPresent(bone -> graphics.renderTooltip(Minecraft.getInstance().font,
                        SpiritBoneItem.createFrom(bone), mouseX, mouseY));
    }

    /** The limb slots over the model, then one slot per list row beside it. */
    private static List<Slot> allSlots(final TabArea area) {
        final List<Slot> slots = new ArrayList<>(limbSlots(area));
        final List<String> listed = listedSlotNames();
        for (int index = 0; index < listed.size(); index++) {
            slots.add(new Slot(listed.get(index), listLeft(area), area.top() + index * LIST_ROW_HEIGHT));
        }
        return slots;
    }

    private static List<Slot> limbSlots(final TabArea area) {
        final float modelHeight = PLAYER_HEIGHT_BLOCKS * MODEL_SCALE;
        final float centerX = area.left() + MODEL_BOX_WIDTH / 2.0F;
        final float modelTop = (area.top() + area.bottom()) / 2.0F - modelHeight / 2.0F;
        return LIMBS.stream()
                .map(limb -> new Slot(limb.slot(),
                        Math.round(centerX + limb.xOffsetHeights() * modelHeight - SLOT_SIZE / 2.0F),
                        Math.round(modelTop + limb.yFromTopHeights() * modelHeight - SLOT_SIZE / 2.0F)))
                .toList();
    }

    /** Every internal slot, then the external bones the player actually carries. */
    private static List<String> listedSlotNames() {
        final List<String> names = new ArrayList<>(SpiritBeastManager.INTERNAL_BONE_SLOTS);
        ClientCultivation.data().getSpiritBones().values().stream()
                .map(AbsorbedBone::slot)
                .filter(SpiritBeastManager::isExternalBoneSlot)
                .sorted()
                .forEach(names::add);
        return names;
    }

    private static int listLeft(final TabArea area) {
        return area.left() + MODEL_BOX_WIDTH + SLOT_SIZE;
    }

    private static void drawSlotNames(final GuiGraphics graphics, final TabArea area, final Map<String, AbsorbedBone> bones) {
        final Font font = Minecraft.getInstance().font;
        final int textLeft = listLeft(area) + SLOT_SIZE + LIST_TEXT_OFFSET;
        final List<String> listed = listedSlotNames();
        for (int index = 0; index < listed.size(); index++) {
            final String slot = listed.get(index);
            final int y = area.top() + index * LIST_ROW_HEIGHT + LIST_TEXT_OFFSET;
            final Component occupant = boneIn(bones, slot)
                    .map(AbsorbedBone::coloredSourceName)
                    .orElse(Component.translatable("soulland.screen.bones.empty"));
            graphics.drawString(font, Component.literal(slot + ": ").append(occupant), textLeft, y,
                    SoulLandStyle.TEXT_COLOR, true);
        }
    }

    private static void drawSlot(final GuiGraphics graphics, final Slot slot, final Optional<AbsorbedBone> bone,
                                 final boolean hovered) {
        graphics.fill(slot.x(), slot.y(), slot.x() + SLOT_SIZE, slot.y() + SLOT_SIZE, SoulLandStyle.OUTLINE_COLOR);
        graphics.fill(slot.x() + 1, slot.y() + 1, slot.x() + SLOT_SIZE - 1, slot.y() + SLOT_SIZE - 1,
                SoulLandStyle.SLOT_BACKGROUND_COLOR);
        bone.ifPresent(present -> graphics.renderItem(SpiritBoneItem.createFrom(present), slot.x() + 1, slot.y() + 1));
        if (hovered) {
            graphics.fill(slot.x() + 1, slot.y() + 1, slot.x() + SLOT_SIZE - 1, slot.y() + SLOT_SIZE - 1,
                    HOVER_HIGHLIGHT_COLOR);
        }
    }

    private static Optional<AbsorbedBone> boneIn(final Map<String, AbsorbedBone> bones, final String slot) {
        return Optional.ofNullable(bones.get(AbsorbedBone.slotKey(slot)));
    }
}
