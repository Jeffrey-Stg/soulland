package com.zelf115.soulland.item;

import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Nine Heart Begonia's tool: a healing soul that fights no one.
 *
 * <p>It answers a right click by mending the wielder and whatever living thing they are looking at,
 * which is the whole of its offering — the soul itself can use no soul ring skill.
 */
public final class NineHeartBegoniaItem extends MartialSoulSwordItem {

    private static final double HEAL_PERCENT_PER_TEN_LEVELS = 0.10;
    private static final int LEVELS_PER_HEAL_STEP = 10;
    private static final double HEAL_RANGE = 15.0;
    private static final double SEARCH_MARGIN = 1.0;
    private static final int COOLDOWN_TICKS = 100;

    public NineHeartBegoniaItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        final double healFraction = HEAL_PERCENT_PER_TEN_LEVELS * (data.getLevel() / LEVELS_PER_HEAL_STEP);
        healFraction(player, healFraction);

        final LivingEntity target = lookedAtLiving(player);
        if (target != null) {
            healFraction(target, healFraction);
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return InteractionResultHolder.success(stack);
    }

    private static void healFraction(final LivingEntity entity, final double fraction) {
        entity.heal((float) (entity.getMaxHealth() * fraction));
    }

    /** The living thing in the player's crosshair, or null when they are looking at nothing. */
    private static LivingEntity lookedAtLiving(final Player player) {
        final Vec3 eye = player.getEyePosition();
        final Vec3 look = player.getLookAngle().scale(HEAL_RANGE);
        final AABB search = player.getBoundingBox().expandTowards(look).inflate(SEARCH_MARGIN);
        final EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, eye.add(look), search,
                entity -> entity instanceof LivingEntity && entity != player, HEAL_RANGE * HEAL_RANGE);
        return hit == null ? null : (LivingEntity) hit.getEntity();
    }
}
