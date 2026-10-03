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
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** The Seraphim God relic: it calls the daylight back. */
public class AngelGodSwordItem extends SwordItem {

    private static final double BONUS_DAMAGE = 100.0;
    private static final long DAY_LENGTH = 24000L;
    private static final long MORNING_TIME = 1000L;
    private static final int CLEAR_WEATHER_TICKS = 12000;
    private static final int NO_THUNDER_TICKS = 0;
    private static final int DAYLIGHT_COOLDOWN_TICKS = 200;

    public AngelGodSwordItem() {
        super(Tiers.NETHERITE, GodRelic.properties().attributes(GodRelic.attributes(BONUS_DAMAGE)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!GodRelic.isEntitled(player, GodTrial.SERAPHIM)) {
            GodRelic.refuse(player);
            return InteractionResultHolder.fail(stack);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.success(stack);
        }

        serverLevel.setDayTime(serverLevel.getDayTime() - serverLevel.getDayTime() % DAY_LENGTH + MORNING_TIME);
        serverLevel.setWeatherParameters(CLEAR_WEATHER_TICKS, NO_THUNDER_TICKS, false, false);
        player.getCooldowns().addCooldown(this, DAYLIGHT_COOLDOWN_TICKS);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltip,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("soulland.tooltip.relic.daylight").withStyle(ChatFormatting.YELLOW));
        GodRelic.appendOwnerTooltip(tooltip);
    }
}
