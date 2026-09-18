package com.zelf115.soulland.item;

import com.zelf115.soulland.trial.GodTrial;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;

/** The Rakshasa God relic: it poisons whatever it cuts. */
public class RakshasaDaggerItem extends SwordItem {

    private static final double BONUS_DAMAGE = 110.0;
    private static final int POISON_TICKS = 200;
    /** Poison X, counted from zero the way the effect system does. */
    private static final int POISON_AMPLIFIER = 9;

    public RakshasaDaggerItem() {
        super(Tiers.NETHERITE, GodRelic.properties().attributes(GodRelic.attributes(BONUS_DAMAGE)));
    }

    @Override
    public boolean hurtEnemy(final ItemStack stack, final LivingEntity target, final LivingEntity attacker) {
        if (attacker instanceof Player player && !GodRelic.isEntitled(player, GodTrial.RAKSHASA)) {
            return false;
        }

        target.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_TICKS, POISON_AMPLIFIER));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltip,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("soulland.tooltip.relic.poison").withStyle(ChatFormatting.DARK_GREEN));
        GodRelic.appendOwnerTooltip(tooltip);
    }
}
