package com.zelf115.soulland.item;

import com.zelf115.soulland.trial.GodTrial;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;

/** The Sea God relic: it calls a storm, then throws like any other trident. */
public class SeaGodTridentItem extends TridentItem {

    private static final double BONUS_DAMAGE = 100.0;
    private static final int THUNDERSTORM_TICKS = 12000;
    private static final int NO_CLEAR_TICKS = 0;
    private static final int STORM_COOLDOWN_TICKS = 200;

    public SeaGodTridentItem() {
        super(GodRelic.properties().attributes(GodRelic.attributes(BONUS_DAMAGE)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!GodRelic.isEntitled(player, GodTrial.SEA_GOD)) {
            GodRelic.refuse(player);
            return InteractionResultHolder.fail(stack);
        }

        if (level instanceof ServerLevel serverLevel && !player.getCooldowns().isOnCooldown(this)) {
            serverLevel.setWeatherParameters(NO_CLEAR_TICKS, THUNDERSTORM_TICKS, true, true);
            player.getCooldowns().addCooldown(this, STORM_COOLDOWN_TICKS);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltip,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("soulland.tooltip.relic.storm").withStyle(ChatFormatting.AQUA));
        GodRelic.appendOwnerTooltip(tooltip);
    }
}
