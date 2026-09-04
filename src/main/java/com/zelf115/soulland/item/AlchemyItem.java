package com.zelf115.soulland.item;

import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import java.util.function.Consumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.food.FoodProperties;

public final class AlchemyItem extends Item {
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
            case QI_GATHERING -> Stats.addSpirit(player, qiLevel * 10.0);
        }
        Stats.syncDerivedPlayerStats(player, data);
    }
}
