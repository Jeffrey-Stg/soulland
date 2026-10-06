package com.zelf115.soulland.item;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.StatBonus;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.AbsorbedBone;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.cultivation.skill.SkillTag;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public final class SpiritBoneItem extends Item {
    public SpiritBoneItem(final Properties properties) {
        super(properties);
    }

    public static ItemStack create(final String sourceName, final String slot, final int tier, final int years,
                                   final StatBonus bonus) {
        final ItemStack stack = new ItemStack(SoulLand.SPIRIT_BONE_ITEM.get());
        final CompoundTag tag = new CompoundTag();
        tag.putString(AbsorbedBone.SOURCE_NAME_KEY, sourceName);
        tag.putString(AbsorbedBone.SLOT_KEY, slot);
        tag.putInt(AbsorbedBone.TIER_KEY, tier);
        tag.putInt(AbsorbedBone.YEARS_KEY, years);
        bonus.writeTo(tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(SpiritBeastManager.describeTier(tier) + " " + slot)
                .withStyle(style -> style.withColor(SpiritBeastManager.tierTextColor(tier))));
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        final CompoundTag tag = boneData(stack);
        if (tag == null) {
            return InteractionResultHolder.fail(stack);
        }

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        final int boneTier = tag.getInt(AbsorbedBone.TIER_KEY);
        final int allowedTier = CultivationManager.maxAbsorbableTier(Stats.getSpirit(player));
        if (boneTier > allowedTier) {
            player.displayClientMessage(Component.translatable("soulland.spirit_bone.tier_locked", allowedTier), true);
            return InteractionResultHolder.fail(stack);
        }

        final AbsorbedBone bone = new AbsorbedBone(tag.getString(AbsorbedBone.SLOT_KEY), tag.getString(AbsorbedBone.SOURCE_NAME_KEY),
                boneTier, tag.getInt(AbsorbedBone.YEARS_KEY), StatBonus.readFrom(tag), SkillTag.read(tag));
        final AbsorbedBone previous = data.putBone(bone);
        applyBone(player, data, bone);
        if (previous != null) {
            player.getInventory().placeItemBackInInventory(createFrom(previous));
        }
        player.displayClientMessage(absorbedMessage(bone, previous), true);

        stack.shrink(1);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    /**
     * Grants the bone now in its body slot. Each limb holds one bone at a time, and the bone it
     * replaced shares the slot's modifier id, so this takes back exactly what the old bone gave no
     * matter how many breakthroughs happened while it was worn.
     */
    private static void applyBone(final Player player, final CultivationData data, final AbsorbedBone bone) {
        Stats.applyBonus(player, AbsorbedBone.modifierId(bone.slot()), bone.bonus());
        Stats.syncDerivedPlayerStats(player, data);
    }

    /** One line for the action bar, naming the bone handed back when the slot was already taken. */
    private static Component absorbedMessage(final AbsorbedBone bone, final AbsorbedBone previous) {
        if (previous == null) {
            return Component.translatable("soulland.spirit_bone.absorbed", bone.slot(), bone.coloredSourceName());
        }
        return Component.translatable("soulland.spirit_bone.swapped", bone.slot(), bone.coloredSourceName(),
                previous.coloredSourceName());
    }

    /** The item an absorbed bone came from, skill included. */
    public static ItemStack createFrom(final AbsorbedBone bone) {
        final ItemStack stack = create(bone.sourceName(), bone.slot(), bone.tier(), bone.years(), bone.bonus());
        bone.skill().ifPresent(skill -> SkillTag.attach(stack, skill));
        return stack;
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltipComponents, final TooltipFlag tooltipFlag) {
        final CompoundTag tag = boneData(stack);
        if (tag == null) {
            return;
        }

        StatBonusTooltip.appendOrigin(tooltipComponents, tag.getString(AbsorbedBone.SOURCE_NAME_KEY), tag.getInt(AbsorbedBone.YEARS_KEY));
        tooltipComponents.add(Component.translatable("soulland.tooltip.slot", tag.getString(AbsorbedBone.SLOT_KEY))
                .withStyle(ChatFormatting.DARK_GRAY));
        SkillTag.appendTooltip(tooltipComponents, tag);
        StatBonusTooltip.appendStats(tooltipComponents, StatBonus.readFrom(tag));
    }

    private static CompoundTag boneData(final ItemStack stack) {
        final CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData == null ? null : customData.copyTag();
    }
}
