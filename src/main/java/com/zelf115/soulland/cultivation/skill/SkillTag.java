package com.zelf115.soulland.cultivation.skill;

import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Reads and writes the skill a soul ring or spirit bone carries, on the item and once absorbed. */
public final class SkillTag {

    private static final String SKILL_KEY = "Skill";

    private SkillTag() {
    }

    public static void attach(final ItemStack stack, final Skill skill) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> write(tag, Optional.of(skill)));
    }

    public static void write(final CompoundTag tag, final Optional<Skill> skill) {
        skill.ifPresent(carried -> tag.putString(SKILL_KEY, carried.name()));
    }

    public static Optional<Skill> read(final CompoundTag tag) {
        return Skill.byName(tag.getString(SKILL_KEY));
    }

    public static void appendTooltip(final List<Component> tooltip, final CompoundTag tag) {
        read(tag).ifPresent(skill -> tooltip.add(
                Component.translatable("soulland.tooltip.skill", skill.displayName()).withStyle(ChatFormatting.AQUA)));
    }
}
