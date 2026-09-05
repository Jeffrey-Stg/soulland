package com.zelf115.soulland.item;

import com.zelf115.soulland.menu.AlchemyMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class PillFurnaceItem extends Item {
    public enum Tier {
        REGULAR,
        ENCHANTED,
        NETHER,
        STAR,
        DIVINE;

        private static final double DIVINE_COST_MULTIPLIER = 0.8;

        public int bonusPillCount() {
            return ordinal();
        }

        public double costMultiplier() {
            return this == DIVINE ? DIVINE_COST_MULTIPLIER : 1.0;
        }
    }

    private final Tier tier;

    public PillFurnaceItem(final Tier tier, final Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public Tier tier() {
        return tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            AlchemyMenu.open(serverPlayer, tier);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
