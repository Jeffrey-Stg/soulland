package com.zelf115.soulland.item;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.StatBonus;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.AbsorbedRing;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.cultivation.SoulRingAbsorption;
import com.zelf115.soulland.network.OverreachPromptPayload;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import com.zelf115.soulland.spirit.Affinity;
import com.zelf115.soulland.spirit.AffinitySystem;
import java.util.Set;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SoulRingItem extends Item {
    private static final double PERCENT = 100.0;

    public SoulRingItem(final Properties properties) {
        super(properties);
    }

    public static ItemStack create(final String sourceName, final int tier, final int years, final StatBonus bonus) {
        return create(sourceName, tier, years, bonus, Set.of());
    }

    public static ItemStack create(final String sourceName, final int tier, final int years, final StatBonus bonus,
                                   final Set<Affinity> affinities) {
        final ItemStack stack = new ItemStack(SoulLand.SOUL_RING_ITEM.get());
        final CompoundTag tag = new CompoundTag();
        tag.putString("SourceName", sourceName);
        tag.putInt("Tier", tier);
        tag.putInt("Years", years);
        AffinitySystem.writeRingAffinities(tag, affinities);
        bonus.writeTo(tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(SpiritBeastManager.describeTier(tier) + " Soul Ring")
                .withStyle(style -> style.withColor(SpiritBeastManager.tierTextColor(tier))));
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        final SoulRingAbsorption.Result result = SoulRingAbsorption.absorb(player, stack);
        if (result == SoulRingAbsorption.Result.OVERREACH_REQUIRED) {
            promptOverreach(player, stack, hand);
            return InteractionResultHolder.fail(stack);
        }

        reportAbsorption(player, result);
        return result == SoulRingAbsorption.Result.ABSORBED
                ? InteractionResultHolder.sidedSuccess(stack, false)
                : InteractionResultHolder.fail(stack);
    }

    /** Tells the player how the absorption went. */
    public static void reportAbsorption(final Player player, final SoulRingAbsorption.Result result) {
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        switch (result) {
            case ABSORBED -> player.sendSystemMessage(Component.translatable("soulland.soul_ring.absorbed",
                    data.getAbsorbedRings().get(data.getSoulRingCount() - 1).sourceName(), data.getSoulRingCount()));
            case LEVEL_LIMIT -> player.sendSystemMessage(Component.translatable("soulland.soul_ring.limit",
                    CultivationManager.maxSoulRingCountForLevel(data.getLevel())));
            case SPIRIT_CAPACITY -> player.sendSystemMessage(Component.translatable("soulland.soul_ring.no_capacity"));
            case OVERREACH_FAILED -> player.sendSystemMessage(Component.translatable("soulland.soul_ring.overreach_failed"));
            case NOT_A_RING, OVERREACH_REQUIRED -> {
                // One is not a soul ring at all, the other has the confirmation screen to answer first.
            }
        }
    }

    private static void promptOverreach(final Player player, final ItemStack stack, final InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        final CompoundTag tag = SoulRingAbsorption.ringData(stack);
        if (tag == null) {
            return;
        }

        final int ringTier = SoulRingAbsorption.tierOf(tag);
        final int allowedTier = CultivationManager.maxAbsorbableTier(Stats.getSpirit(player));
        final int chancePercent = (int) Math.round(SoulRingAbsorption.overreachChanceFor(player, ringTier) * PERCENT);
        PacketDistributor.sendToPlayer(serverPlayer,
                new OverreachPromptPayload(hand.ordinal(), ringTier, allowedTier, chancePercent));
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltipComponents, final TooltipFlag tooltipFlag) {
        final CompoundTag tag = SoulRingAbsorption.ringData(stack);
        if (tag == null) {
            return;
        }

        StatBonusTooltip.appendOrigin(tooltipComponents, tag.getString(AbsorbedRing.SOURCE_NAME_KEY), tag.getInt(AbsorbedRing.YEARS_KEY));
        StatBonusTooltip.appendStats(tooltipComponents, StatBonus.readFrom(tag));
    }
}
