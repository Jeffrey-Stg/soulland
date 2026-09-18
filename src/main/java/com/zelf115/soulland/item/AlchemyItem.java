package com.zelf115.soulland.item;

import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.food.FoodProperties;

public final class AlchemyItem extends Item {
    private static final int LEVELS_PER_QI_GATHERING_TIER = 20;

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
                .nutrition(0).saturationModifier(0.0F).alwaysEdible().build());
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
            case QI_GATHERING -> {
                if (!isQiGatheringTierUsableAt(data.getLevel())) {
                    return;
                }
                Stats.addSpirit(player, qiLevel * 10.0);
            }
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
