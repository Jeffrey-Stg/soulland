package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import java.util.Comparator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

public final class MartialSoulAbility {
    private static final int TICKS_PER_SECOND = 20;
    private static final int TICKS_PER_MINUTE = 1200;
    private static final double ENERGY_COST_PER_SECOND = 0.5;
    private static final String TEMPORARY_BONUS_ID = "soulland_martial_soul_buff";
    private static final double NON_TOOL_ACTIVATION_BONUS_PERCENT = 0.10;
    private static final double NINE_HEART_BEGONIA_HEAL_PERCENT_PER_TEN_LEVELS = 0.10;
    private static final double PAGODA_BUFF_PERCENT_PER_TEN_LEVELS = 0.02;
    private static final int PAGODA_BUFF_DURATION_TICKS = 5 * TICKS_PER_MINUTE;
    private static final double CAST_TARGET_SEARCH_RADIUS = 15.0;

    private MartialSoulAbility() {
    }

    public static void toggle(final Player player, final CultivationData data) {
        if (data.getMartialSoul() == null) return;
        if (data.isMartialSoulActive()) {
            deactivate(player, data);
            return;
        }
        activate(player, data);
    }

    /** Turns the ability off from outside a toggle press — a soul tool can't be dropped or lost to death, only deactivated. */
    public static void forceDeactivate(final Player player, final CultivationData data) {
        if (data.isMartialSoulActive()) {
            deactivate(player, data);
        }
    }

    private static void deactivate(final Player player, final CultivationData data) {
        if (hasNonToolSoul(data)) {
            Stats.removeTemporaryBonus(player, TEMPORARY_BONUS_ID);
        }
        removeToolItem(player, data.getMartialSoul());
        removeToolItem(player, data.getSecondaryMartialSoul());
        data.setMartialSoulActive(false);
    }

    private static void activate(final Player player, final CultivationData data) {
        if (data.getSpiritEnergy() < ENERGY_COST_PER_SECOND) return;

        data.setMartialSoulActive(true);
        grantToolItem(player, data.getMartialSoul());
        grantToolItem(player, data.getSecondaryMartialSoul());
        if (hasNonToolSoul(data)) {
            Stats.applyTemporaryStatPercentBonus(player, TEMPORARY_BONUS_ID, NON_TOOL_ACTIVATION_BONUS_PERCENT);
        }
    }

    private static boolean hasNonToolSoul(final CultivationData data) {
        return isNonToolCategory(data.getMartialSoul()) || isNonToolCategory(data.getSecondaryMartialSoul());
    }

    private static boolean isNonToolCategory(final MartialSoul soul) {
        return soul != null && soul.category() != MartialSoul.Category.TOOL;
    }

    private static void grantToolItem(final Player player, final MartialSoul soul) {
        final DeferredItem<Item> item = toolItemFor(soul);
        if (item != null) {
            player.getInventory().add(item.get().getDefaultInstance());
        }
    }

    /** Takes back the tool granted on activation, wherever it landed in the player's inventory. */
    private static void removeToolItem(final Player player, final MartialSoul soul) {
        final DeferredItem<Item> item = toolItemFor(soul);
        if (item == null) return;

        final Item target = item.get();
        final var inventoryItems = player.getInventory().items;
        for (int slot = 0; slot < inventoryItems.size(); slot++) {
            if (inventoryItems.get(slot).is(target)) {
                inventoryItems.set(slot, ItemStack.EMPTY);
            }
        }
    }

    private static DeferredItem<Item> toolItemFor(final MartialSoul soul) {
        if (soul == null) return null;
        return switch (soul) {
            case CLEAR_SKY_HAMMER -> SoulLand.CLEAR_SKY_HAMMER;
            case SEVEN_KILL_SWORD -> SoulLand.SEVEN_KILL_SWORD;
            case NINE_HEART_BEGONIA -> SoulLand.NINE_HEART_BEGONIA;
            case SEVEN_TREASURE_GLAZED_TILE_PAGODA -> SoulLand.SEVEN_TREASURE_GLAZED_TILE_PAGODA;
            default -> null;
        };
    }

    public static void tick(final Player player, final CultivationData data, final long gameTick) {
        if (data.isMartialSoulActive() && gameTick % TICKS_PER_SECOND == 0) {
            data.setSpiritEnergy(data.getSpiritEnergy() - ENERGY_COST_PER_SECOND);
            if (data.getSpiritEnergy() <= 0.0) deactivate(player, data);
        }
        if (data.getMartialSoulBuffUntil() > 0 && gameTick >= data.getMartialSoulBuffUntil()) {
            Stats.removeTemporaryBonus(player, TEMPORARY_BONUS_ID);
            data.setMartialSoulBuffUntil(0L);
        }
    }

    public static void cast(final Player player, final CultivationData data) {
        if (!data.isMartialSoulActive()) return;
        castSoul(player, data, data.getMartialSoul());
        castSoul(player, data, data.getSecondaryMartialSoul());
    }

    private static void castSoul(final Player player, final CultivationData data, final MartialSoul soul) {
        if (soul == null) return;
        final int levelTens = data.getLevel() / 10;
        if (soul == MartialSoul.NINE_HEART_BEGONIA) {
            final Player target = nearestOtherPlayer(player);
            final float healAmount = (float) (target.getMaxHealth() * NINE_HEART_BEGONIA_HEAL_PERCENT_PER_TEN_LEVELS * levelTens);
            target.heal(healAmount);
            player.heal(healAmount);
        } else if (soul == MartialSoul.SEVEN_TREASURE_GLAZED_TILE_PAGODA) {
            final Player target = nearestOtherPlayer(player);
            final CultivationData targetData = target.getData(CultivationAttachment.CULTIVATION_DATA.get());
            Stats.applyTemporaryPercentBonus(target, TEMPORARY_BONUS_ID, PAGODA_BUFF_PERCENT_PER_TEN_LEVELS * levelTens);
            targetData.setMartialSoulBuffUntil(player.level().getGameTime() + PAGODA_BUFF_DURATION_TICKS);
        }
    }

    private static Player nearestOtherPlayer(final Player player) {
        return player.level().getEntitiesOfClass(Player.class,
                player.getBoundingBox().inflate(CAST_TARGET_SEARCH_RADIUS), candidate -> candidate != player
                    && player.hasLineOfSight(candidate)).stream()
            .min(Comparator.comparingDouble(player::distanceToSqr)).orElse(player);
    }
}
