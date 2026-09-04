package com.zelf115.soulland.item;

import com.zelf115.soulland.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public final class MartialSoulSwordItem extends SwordItem {
    public MartialSoulSwordItem(final Properties properties) {
        super(Tiers.IRON, properties);
    }

    @Override
    public boolean hurtEnemy(final ItemStack stack, final LivingEntity target, final LivingEntity attacker) {
        final boolean result = super.hurtEnemy(stack, target, attacker);
        if (attacker instanceof Player player) {
            target.hurt(player.damageSources().playerAttack(player), (float) (Stats.getDamage(player) * 0.10));
        }
        return result;
    }
}
