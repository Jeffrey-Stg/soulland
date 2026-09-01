package com.zelf115.soulland.item;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class SoulRingItem extends Item {
    public SoulRingItem(final Properties properties) {
        super(properties);
    }

    public static ItemStack create(final String sourceName, final int tier, final int years, final double damage, final double health, final double defense, final double speed, final double spirit) {
        final ItemStack stack = new ItemStack(SoulLand.SOUL_RING_ITEM.get());
        final CompoundTag tag = new CompoundTag();
        tag.putString("SourceName", sourceName);
        tag.putInt("Tier", tier);
        tag.putInt("Years", years);
        tag.putDouble("DamageBonus", damage);
        tag.putDouble("HealthBonus", health);
        tag.putDouble("DefenseBonus", defense);
        tag.putDouble("SpeedBonus", speed);
        tag.putDouble("SpiritBonus", spirit);
        tag.putDouble("CultivationSpeedBonus", tier * 5.0);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(SpiritBeastManager.describeTier(tier) + " Soul Ring").withStyle(SpiritBeastManager.tierColor(tier)));
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        final CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return InteractionResultHolder.fail(stack);
        }

        final CompoundTag tag = customData.getUnsafe();
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        final int currentLevel = data.getLevel();
        final int maximumRings = CultivationManager.maxSoulRingCountForLevel(currentLevel);
        if (data.getSoulRingCount() >= maximumRings) {
            player.sendSystemMessage(Component.translatable("soulland.soul_ring.limit", maximumRings));
            return InteractionResultHolder.fail(stack);
        }

        final int ringTier = tag.getInt("Tier");
        final int allowedTier = CultivationManager.maxAbsorbableTier(Stats.getSpirit(player));
        if (ringTier > allowedTier) {
            final double successChance = CultivationManager.overreachSuccessChance(allowedTier, ringTier, data.getRebirthCount());
            if (!player.isCrouching()) {
                player.sendSystemMessage(Component.translatable("soulland.soul_ring.overreach_warning", Math.round(successChance * 100.0D)));
                return InteractionResultHolder.fail(stack);
            }
            if (player.getRandom().nextDouble() >= successChance) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                player.kill();
                return InteractionResultHolder.fail(stack);
            }
        }

        Stats.addDamage(player, tag.getDouble("DamageBonus"));
        Stats.addHealth(player, tag.getDouble("HealthBonus"));
        Stats.addDefense(player, tag.getDouble("DefenseBonus"));
        Stats.addSpeed(player, tag.getDouble("SpeedBonus"));
        Stats.addSpirit(player, tag.getDouble("SpiritBonus"));
        Stats.addCultivationSpeed(player, tag.getDouble("CultivationSpeedBonus"));
        data.setSoulRingCount(data.getSoulRingCount() + 1);
        Stats.syncDerivedPlayerStats(player, data);
        player.sendSystemMessage(Component.translatable("soulland.soul_ring.absorbed", tag.getString("SourceName"), data.getSoulRingCount()));

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltipComponents, final TooltipFlag tooltipFlag) {
        final CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }

        final CompoundTag tag = customData.getUnsafe();
        tooltipComponents.add(Component.literal(tag.getString("SourceName")).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("soulland.tooltip.tier", SpiritBeastManager.describeTier(tag.getInt("Tier"))));
        tooltipComponents.add(Component.translatable("soulland.tooltip.years", tag.getInt("Years")));
        tooltipComponents.add(Component.translatable("soulland.tooltip.damage_bonus", formatBonus(tag.getDouble("DamageBonus"))));
        tooltipComponents.add(Component.translatable("soulland.tooltip.health_bonus", formatBonus(tag.getDouble("HealthBonus"))));
        tooltipComponents.add(Component.translatable("soulland.tooltip.defense_bonus", formatBonus(tag.getDouble("DefenseBonus"))));
        tooltipComponents.add(Component.translatable("soulland.tooltip.speed_bonus", formatBonus(tag.getDouble("SpeedBonus"))));
        tooltipComponents.add(Component.translatable("soulland.tooltip.spirit_bonus", formatBonus(tag.getDouble("SpiritBonus"))));
    }

    private static String formatBonus(final double value) {
        return String.format("%.1f", value);
    }
}
