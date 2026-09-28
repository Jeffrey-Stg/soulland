package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

/** Summons, puts away and switches the martial soul the player currently has selected. */
public final class MartialSoulAbility {
    private static final int TICKS_PER_SECOND = 20;
    private static final double ENERGY_COST_PER_SECOND = 0.5;
    private static final String ACTIVATION_BONUS_ID = "soulland_martial_soul_activation";
    private static final double NON_TOOL_ACTIVATION_BONUS_PERCENT = 0.10;

    private MartialSoulAbility() {
    }

    public static void toggle(final Player player, final CultivationData data) {
        if (data.getActiveMartialSoul() == null) return;
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

    /** Moves the keys over to the player's other martial soul, putting the current one away first. */
    public static void switchActiveSoul(final Player player, final CultivationData data) {
        if (data.getSecondaryMartialSoul() == null) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.martial_soul.no_second_soul"));
            return;
        }

        forceDeactivate(player, data);
        final SoulSlot next = data.getActiveSoulSlot() == SoulSlot.PRIMARY ? SoulSlot.SECONDARY : SoulSlot.PRIMARY;
        data.setActiveSoulSlot(next);
        data.setSelectedRingIndex(0);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.martial_soul.switched",
                data.getActiveMartialSoul().displayName()));
    }

    private static void deactivate(final Player player, final CultivationData data) {
        final MartialSoul soul = data.getActiveMartialSoul();
        if (isNonToolCategory(soul)) {
            Stats.removeTemporaryBonus(player, ACTIVATION_BONUS_ID);
        }
        removeToolItem(player, soul);
        data.setMartialSoulActive(false);
    }

    private static void activate(final Player player, final CultivationData data) {
        if (data.getSpiritEnergy() < ENERGY_COST_PER_SECOND) return;

        final MartialSoul soul = data.getActiveMartialSoul();
        if (!grantToolItem(player, soul)) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.martial_soul.no_room"));
            return;
        }
        data.setMartialSoulActive(true);
        if (isNonToolCategory(soul)) {
            Stats.applyTemporaryStatPercentBonus(player, ACTIVATION_BONUS_ID, NON_TOOL_ACTIVATION_BONUS_PERCENT);
        }
    }

    private static boolean isNonToolCategory(final MartialSoul soul) {
        return soul != null && soul.category() != MartialSoul.Category.TOOL;
    }

    /** Whether the soul is now armed: true for the souls that carry no tool at all. */
    private static boolean grantToolItem(final Player player, final MartialSoul soul) {
        final DeferredItem<Item> item = toolItemFor(soul);
        if (item == null) {
            return true;
        }
        return player.getInventory().add(item.get().getDefaultInstance());
    }

    /**
     * Takes back the tool granted on activation, wherever it landed: the whole inventory, not only
     * the main compartment, or a tool moved to the off-hand would survive being put away and the
     * next activation would mint a second copy of it.
     */
    private static void removeToolItem(final Player player, final MartialSoul soul) {
        final DeferredItem<Item> item = toolItemFor(soul);
        if (item == null) return;

        final Item target = item.get();
        final Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(target)) {
                inventory.setItem(slot, ItemStack.EMPTY);
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
        SoulRingSkills.expireBuff(player, data, gameTick);
    }

    public static void cast(final Player player, final CultivationData data) {
        SoulRingSkills.useSelectedRingSkill(player, data);
    }

    public static void selectNextRing(final Player player, final CultivationData data) {
        SoulRingSkills.selectNextRing(player, data);
    }
}
