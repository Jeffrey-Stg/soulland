package com.zelf115.soulland.item;

import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.food.FoodProperties;

public final class AlchemyItem extends Item {
    private static final int LEVELS_PER_QI_GATHERING_TIER = 20;
    private static final double QI_GATHERING_SPIRIT_PER_TIER = 25.0;

    public enum Effect {
        MYSTERIOUS_WATER,
        SPIRIT_ASCENSION,
        QI_GATHERING
    }

    private final Effect effect;
    private final int qiLevel;

    public AlchemyItem(final Effect effect, final int qiLevel, final Properties properties) {
        super(properties);
        this.effect = effect;
        this.qiLevel = qiLevel;
    }

    public static Properties pillProperties() {
        return new Properties().stacksTo(16).food(new FoodProperties.Builder()
                .nutrition(0).saturationModifier(0.0F).alwaysEdible().fast().build());
    }

    /**
     * A banded pill outside its band is refused before it is swallowed, rather than eaten for
     * nothing. Only the server may judge that: the cultivation attachment is not synced, so the
     * client's copy always reads level one and would refuse every pill above tier one.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && effect == Effect.QI_GATHERING
                && !isQiGatheringTierUsableAt(cultivationLevelOf(player))) {
            player.sendSystemMessage(Component.translatable("soulland.alchemy.wrong_level_band",
                    tierMinLevel(), tierMaxLevel()));
            return InteractionResultHolder.fail(stack);
        }
        return super.use(level, player, hand);
    }

    private static int cultivationLevelOf(final Player player) {
        return player.getData(CultivationAttachment.CULTIVATION_DATA.get()).getLevel();
    }

    @Override
    public ItemStack finishUsingItem(final ItemStack stack, final Level level, final LivingEntity entity) {
        final ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player && !level.isClientSide()) {
            applyEffect(player);
        }
        return result;
    }

    private void applyEffect(final Player player) {
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        switch (effect) {
            case MYSTERIOUS_WATER -> data.setInnateStat(data.getInnateStat() + 1);
            case SPIRIT_ASCENSION -> {
                data.setInnateStat(data.getInnateStat() + 1);
                Stats.addSpirit(player, 110.0);
            }
            case QI_GATHERING -> Stats.addSpirit(player, qiLevel * QI_GATHERING_SPIRIT_PER_TIER);
        }
        Stats.syncDerivedPlayerStats(player, data);
    }

    // Tier N is meant for the 20-level band it was brewed for: tier 1 for levels 1-20,
    // tier 2 for 21-40, and so on - a tier 5 pill isn't a shortcut for a level 1 player.
    private boolean isQiGatheringTierUsableAt(final int playerLevel) {
        return playerLevel >= tierMinLevel() && playerLevel <= tierMaxLevel();
    }

    private int tierMinLevel() {
        return (qiLevel - 1) * LEVELS_PER_QI_GATHERING_TIER + 1;
    }

    private int tierMaxLevel() {
        return qiLevel * LEVELS_PER_QI_GATHERING_TIER;
    }

    /** Only the banded pills carry a level line; the rest work for any cultivator. */
    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltip,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (effect != Effect.QI_GATHERING) {
            return;
        }

        tooltip.add(Component.translatable("soulland.alchemy.level_range", tierMinLevel(), tierMaxLevel())
                .withStyle(ChatFormatting.AQUA));
    }
}
