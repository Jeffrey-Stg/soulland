package com.zelf115.soulland.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;

/** The Death God relic: raw damage and nothing else. */
public class AsuraSwordItem extends SwordItem {

    private static final double BONUS_DAMAGE = 125.0;

    public AsuraSwordItem() {
        super(Tiers.NETHERITE, GodRelic.properties().attributes(GodRelic.attributes(BONUS_DAMAGE)));
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltip,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        GodRelic.appendOwnerTooltip(tooltip);
    }
}
