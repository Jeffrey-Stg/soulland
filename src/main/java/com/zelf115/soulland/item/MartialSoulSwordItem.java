package com.zelf115.soulland.item;

import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.MartialSoulAbility;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Unbreakable;

public class MartialSoulSwordItem extends SwordItem {
    private static final double BONUS_DAMAGE_PERCENT_OF_STAT = 0.10;
    private static final double PERCENT = 100.0;
    private static final float BASE_ATTACK_DAMAGE_BONUS = 3.0F;
    private static final float BASE_ATTACK_SPEED_MODIFIER = -2.4F;

    public MartialSoulSwordItem(final Properties properties) {
        super(Tiers.IRON, properties
                .attributes(SwordItem.createAttributes(Tiers.IRON, BASE_ATTACK_DAMAGE_BONUS, BASE_ATTACK_SPEED_MODIFIER))
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true)));
    }

    @Override
    public boolean hurtEnemy(final ItemStack stack, final LivingEntity target, final LivingEntity attacker) {
        final boolean result = super.hurtEnemy(stack, target, attacker);
        if (attacker instanceof Player player) {
            // The swing that called this has just armed the target's invulnerability window, which
            // would drop the follow-up hit outright (or shave it down to the difference). Reopening
            // the window is what makes the advertised bonus actually land.
            target.invulnerableTime = 0;
            target.hurt(player.damageSources().playerAttack(player), (float) (Stats.getDamage(player) * BONUS_DAMAGE_PERCENT_OF_STAT));
        }
        return result;
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("soulland.tooltip.martial_soul_sword.bonus_damage",
                String.valueOf((int) (BONUS_DAMAGE_PERCENT_OF_STAT * PERCENT))).withStyle(ChatFormatting.RED));
    }

    /** Keeps the tool out of bundles and shulker boxes, which would carry it away from its owner. */
    @Override
    public boolean canFitInsideContainerItems(final ItemStack stack) {
        return false;
    }

    /** A martial soul's tool isn't a possession to throw away — dropping it just puts the soul away. */
    @Override
    public boolean onDroppedByPlayer(final ItemStack item, final Player player) {
        deactivateOwner(player);
        return false;
    }

    private static void deactivateOwner(final Player player) {
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        MartialSoulAbility.forceDeactivate(player, data);
    }
}
