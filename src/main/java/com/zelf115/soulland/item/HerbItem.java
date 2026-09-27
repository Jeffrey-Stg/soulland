package com.zelf115.soulland.item;

import com.zelf115.soulland.Stats;
import com.zelf115.soulland.biome.SoulLandBiomes;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.cultivation.MartialSoulEvolution;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;

public final class HerbItem extends Item {
    public enum Effect {
        INNATE_TWO,
        INNATE_ONE,
        COMMON_SPIRIT,
        CULTIVATION_LEVEL_ONE,
        CULTIVATION_LEVEL_HALF,
        SPIRIT_FIFTY,
        SPIRIT_HUNDRED,
        CULTIVATION_SPEED_TWO,
        ALL_BUT_SPIRIT_AND_INNATE_ONE,
        ALL_BUT_SPIRIT_AND_INNATE_ONE_SOUL_END,
        ICE_SPIRIT_TEN,
        FIRE_SPIRIT_TEN,
        ICE_CULTIVATION_SPEED_TWO,
        FIRE_CULTIVATION_SPEED_TWO,
        EVOLVE_PAGODA,
        ALL_BUT_SPIRIT_ONE
    }

    private static final String MARTIAL_SOUL_ELEMENT_KEY = "soulland_martial_soul_element";

    private final Effect effect;

    public HerbItem(final Effect effect, final Properties properties) {
        super(properties);
        this.effect = effect;
    }

    public static FoodProperties foodProperties(final Effect effect) {
        if (effect == Effect.COMMON_SPIRIT) {
            return new FoodProperties.Builder().nutrition(2).saturationModifier(0.5F).alwaysEdible().build();
        }
        return new FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).alwaysEdible().build();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!canEat(player, level)) {
            return InteractionResultHolder.fail(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(final ItemStack stack, final Level level, final LivingEntity entity) {
        final ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player && !level.isClientSide()) {
            applyEffect(player);
        }
        return result;
    }

    private boolean canEat(final Player player, final Level level) {
        if (effect == Effect.ICE_SPIRIT_TEN || effect == Effect.ICE_CULTIVATION_SPEED_TWO) {
            return matchesElement(player, "ice", level);
        }
        if (effect == Effect.FIRE_SPIRIT_TEN || effect == Effect.FIRE_CULTIVATION_SPEED_TWO) {
            return matchesElement(player, "fire", level);
        }
        if (effect == Effect.ALL_BUT_SPIRIT_AND_INNATE_ONE_SOUL_END) {
            return level.getBiome(player.blockPosition()).is(SoulLandBiomes.SOUL_END);
        }
        return true;
    }

    private static boolean matchesElement(final Player player, final String required, final Level level) {
        final String element = player.getPersistentData().getString(MARTIAL_SOUL_ELEMENT_KEY);
        return element.isEmpty() || required.equalsIgnoreCase(element);
    }

    private void applyEffect(final Player player) {
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        switch (effect) {
            case INNATE_TWO -> data.setInnateStat(data.getInnateStat() + 2);
            case INNATE_ONE -> data.setInnateStat(data.getInnateStat() + 1);
            case CULTIVATION_LEVEL_ONE -> grantLevelFraction(player, data, 1.0);
            case CULTIVATION_LEVEL_HALF -> grantLevelFraction(player, data, 0.5);
            case SPIRIT_FIFTY -> {
                Stats.addSpirit(player, 50.0);
                MartialSoulEvolution.evolveFromFullMoonDew(player, data);
            }
            case SPIRIT_HUNDRED -> Stats.addSpirit(player, 100.0);
            case COMMON_SPIRIT, CULTIVATION_SPEED_TWO, ICE_CULTIVATION_SPEED_TWO, FIRE_CULTIVATION_SPEED_TWO ->
                    Stats.addCultivationSpeed(player, 2.0);
            case ALL_BUT_SPIRIT_ONE -> {
                Stats.addDamage(player, 1.0);
                Stats.addHealth(player, 1.0);
                Stats.addDefense(player, 1.0);
                Stats.addSpeed(player, 1.0);
            }
            case ALL_BUT_SPIRIT_AND_INNATE_ONE -> {
                Stats.addDamage(player, 1.0);
                Stats.addHealth(player, 1.0);
                Stats.addDefense(player, 1.0);
                Stats.addSpeed(player, 1.0);
                data.setInnateStat(data.getInnateStat() + 1);
            }
            case ALL_BUT_SPIRIT_AND_INNATE_ONE_SOUL_END -> {
                Stats.addDamage(player, 1.0);
                Stats.addHealth(player, 1.0);
                Stats.addDefense(player, 1.0);
                Stats.addSpeed(player, 1.0);
                data.setInnateStat(data.getInnateStat() + 1);
            }
            case ICE_SPIRIT_TEN, FIRE_SPIRIT_TEN -> Stats.addSpirit(player, 10.0);
            case EVOLVE_PAGODA -> MartialSoulEvolution.evolveFromSilkTulip(player, data);
        }
        Stats.syncDerivedPlayerStats(player, data);
    }

    private static void grantLevelFraction(final Player player, final CultivationData data, final double fraction) {
        if (data.getLevel() < CultivationManager.MAX_LEVEL) {
            CultivationManager.grantXp(player, data,
                    CultivationManager.xpRequiredForLevel(data.getLevel()) * fraction);
        }
    }
}
