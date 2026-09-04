package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.StatBonus;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.spirit.AffinitySystem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * The rules a soul ring must satisfy before a player can take it in.
 *
 * <p>Shared by the item itself and by the network handler that answers the overreach confirmation,
 * so the server validates the same way whichever path the player took.
 */
public final class SoulRingAbsorption {

    public enum Result {
        ABSORBED,
        /** The player has already taken every ring their cultivation level allows. */
        LEVEL_LIMIT,
        /** Their spirit stat has no room left for another ring of this colour. */
        SPIRIT_CAPACITY,
        /** The ring outranks their spirit stat; only a confirmed overreach can take it. */
        OVERREACH_REQUIRED,
        /** The overreach roll failed and the ring destroyed the player. */
        OVERREACH_FAILED,
        /** The stack carries no soul ring data. */
        NOT_A_RING
    }

    private SoulRingAbsorption() {
    }

    /** Absorbs the ring if it is within the player's limits, refusing anything that overreaches. */
    public static Result absorb(final Player player, final ItemStack stack) {
        final CompoundTag tag = ringData(stack);
        if (tag == null) {
            return Result.NOT_A_RING;
        }

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        if (data.getSoulRingCount() >= CultivationManager.maxSoulRingCountForLevel(data.getLevel())) {
            return Result.LEVEL_LIMIT;
        }

        final double spirit = Stats.getSpirit(player);
        final int tier = tierOf(tag);
        if (tier > CultivationManager.maxAbsorbableTier(spirit)) {
            return Result.OVERREACH_REQUIRED;
        }
        if (!SoulRingCapacity.hasRoomFor(spirit, data.getAbsorbedRings(), tier)) {
            return Result.SPIRIT_CAPACITY;
        }

        grant(player, data, stack, tag);
        return Result.ABSORBED;
    }

    /**
     * Takes the gamble: the ring outranks the player, and failing kills them.
     *
     * <p>Succeeding bypasses the spirit capacity check — overreaching is already the price paid.
     */
    public static Result absorbByOverreach(final Player player, final ItemStack stack) {
        final CompoundTag tag = ringData(stack);
        if (tag == null) {
            return Result.NOT_A_RING;
        }

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        if (data.getSoulRingCount() >= CultivationManager.maxSoulRingCountForLevel(data.getLevel())) {
            return Result.LEVEL_LIMIT;
        }

        final int tier = tierOf(tag);
        final int allowedTier = CultivationManager.maxAbsorbableTier(Stats.getSpirit(player));
        if (tier <= allowedTier) {
            return absorb(player, stack);
        }

        final double chance = CultivationManager.overreachSuccessChance(allowedTier, tier, data.getRebirthCount());
        if (player.getRandom().nextDouble() >= chance) {
            // Destroy before the kill: once the player dies their inventory is already on the ground.
            destroy(stack);
            player.kill();
            return Result.OVERREACH_FAILED;
        }

        grant(player, data, stack, tag);
        return Result.ABSORBED;
    }

    /** The success chance shown on the confirmation screen and rolled on confirmation. */
    public static double overreachChanceFor(final Player player, final int ringTier) {
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        final int allowedTier = CultivationManager.maxAbsorbableTier(Stats.getSpirit(player));
        return CultivationManager.overreachSuccessChance(allowedTier, ringTier, data.getRebirthCount());
    }

    public static CompoundTag ringData(final ItemStack stack) {
        final CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData == null ? null : customData.copyTag();
    }

    public static int tierOf(final CompoundTag tag) {
        return tag.getInt(AbsorbedRing.TIER_KEY);
    }

    private static void grant(final Player player, final CultivationData data, final ItemStack stack,
                              final CompoundTag tag) {
        final AbsorbedRing ring = new AbsorbedRing(
                tag.getString(AbsorbedRing.SOURCE_NAME_KEY), tierOf(tag), tag.getInt(AbsorbedRing.YEARS_KEY), StatBonus.readFrom(tag));
        data.addRing(ring);
        Stats.applyBonus(player, AbsorbedRing.modifierId(data.getSoulRingCount() - 1),
            ring.bonus().scaled(AffinitySystem.ringMultiplier(player, tag)));
        MartialSoulEvolution.tryEvolve(player, data);
        Stats.syncDerivedPlayerStats(player, data);
        destroy(stack);
    }

    /**
     * Destroys the ring an absorption used up, whether it fused or shattered.
     *
     * <p>This ignores creative mode on purpose: a ring is not spent like an ingredient, it is
     * consumed into the cultivator, and a player who keeps holding it could absorb it again.
     */
    private static void destroy(final ItemStack stack) {
        stack.shrink(1);
    }
}
