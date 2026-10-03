package com.zelf115.soulland.mixin;

import com.zelf115.soulland.cultivation.CultivationGlide;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Keeps a cultivator's glide going. Vanilla ends any glide each tick unless the chest slot holds a
 * working elytra, which would cancel the glide the moment it started.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    private static final int FALL_FLYING_FLAG = 7;

    @ModifyArg(method = "updateFallFlying", index = 1, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setSharedFlag(IZ)V"))
    private boolean soulland$keepCultivationGlide(final int flag, final boolean stillGliding) {
        return stillGliding || (flag == FALL_FLYING_FLAG && isCultivationGliding((LivingEntity) (Object) this));
    }

    private static boolean isCultivationGliding(final LivingEntity entity) {
        return entity.isFallFlying()
                && !entity.onGround()
                && !entity.isPassenger()
                && !entity.hasEffect(MobEffects.LEVITATION)
                && CultivationGlide.canGlide(entity);
    }
}
