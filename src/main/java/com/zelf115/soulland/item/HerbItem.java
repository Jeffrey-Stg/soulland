package com.zelf115.soulland.item;

import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.cultivation.MartialSoulEvolution;
import com.zelf115.soulland.spirit.Affinity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
        CULTIVATION_LEVEL_TWO,
        CULTIVATION_LEVEL_ONE,
        CULTIVATION_LEVEL_HALF,
        SPIRIT_FIFTY,
        SPIRIT_HUNDRED,
        ALL_BUT_SPIRIT_AND_INNATE_ONE,
        ICE_SPIRIT_TEN,
        FIRE_SPIRIT_TEN,
        ICE_CULTIVATION_SPEED_TWO,
        FIRE_CULTIVATION_SPEED_TWO,
        EVOLVE_PAGODA,
        ALL_BUT_SPIRIT_ONE
    }

    /** The multiplier a soul with no leaning toward an element carries for it. */
    private static final double NO_AFFINITY = 1.0;
    private static final String WASTED_HERB_MESSAGE = "soulland.herb.no_evolution";

    private final Effect effect;

    public HerbItem(final Effect effect, final Properties properties) {
        super(properties);
        this.effect = effect;
    }

    public static FoodProperties foodProperties() {
        return new FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).alwaysEdible().fast().build();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!canEat(player)) {
            return InteractionResultHolder.fail(stack);
        }
        return super.use(level, player, hand);
    }

    /**
     * The Silk Tulip does nothing but evolve the pagoda, so it is refused rather than eaten for nothing.
     * Only the server may judge that: the client's copy of the cultivation attachment is never synced,
     * so the refusal comes once eating finishes, after the client has already eaten its own copy.
     */
    private boolean isWastedOn(final Player player) {
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        return effect == Effect.EVOLVE_PAGODA && !MartialSoulEvolution.canEvolveFromSilkTulip(data);
    }

    @Override
    public ItemStack finishUsingItem(final ItemStack stack, final Level level, final LivingEntity entity) {
        if (entity instanceof ServerPlayer player && isWastedOn(player)) {
            refuse(player);
            return stack;
        }
        final ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player && !level.isClientSide()) {
            applyEffect(player);
        }
        return result;
    }

    /** Resending the inventory gives the client back the herb it ate on its side. */
    private static void refuse(final ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(WASTED_HERB_MESSAGE));
        player.containerMenu.sendAllDataToRemote();
    }

    private boolean canEat(final Player player) {
        if (effect == Effect.ICE_SPIRIT_TEN || effect == Effect.ICE_CULTIVATION_SPEED_TWO) {
            return matchesElement(player, Affinity.ICE);
        }
        if (effect == Effect.FIRE_SPIRIT_TEN || effect == Effect.FIRE_CULTIVATION_SPEED_TWO) {
            return matchesElement(player, Affinity.FIRE);
        }
        return true;
    }

    /**
     * An elemental herb only feeds a soul that carries that element. A cultivator who has not
     * awakened a soul yet is not held back, since there is no element to match against.
     */
    private static boolean matchesElement(final Player player, final Affinity required) {
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        if (data.getMartialSoul() == null) {
            return true;
        }
        return data.getAffinityMultiplier(required) > NO_AFFINITY;
    }

    private void applyEffect(final Player player) {
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        switch (effect) {
            case INNATE_TWO -> data.setInnateStat(data.getInnateStat() + 2);
            case INNATE_ONE -> data.setInnateStat(data.getInnateStat() + 1);
            case CULTIVATION_LEVEL_TWO -> grantLevelFraction(player, data, 2.0);
            case CULTIVATION_LEVEL_ONE -> grantLevelFraction(player, data, 1.0);
            case CULTIVATION_LEVEL_HALF -> grantLevelFraction(player, data, 0.5);
            case SPIRIT_FIFTY -> {
                Stats.addSpirit(player, 50.0);
                MartialSoulEvolution.evolveFromFullMoonDew(player, data);
            }
            case SPIRIT_HUNDRED -> Stats.addSpirit(player, 100.0);
            case ICE_CULTIVATION_SPEED_TWO, FIRE_CULTIVATION_SPEED_TWO ->
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
