package com.zelf115.soulland.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.zelf115.soulland.cultivation.CultivationGlide;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Lets a gliding cultivator spread their wings without an elytra in the chest slot. */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @ModifyExpressionValue(method = "tryToStartFallFlying", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean soulland$allowCultivationGlide(final boolean wearsWorkingElytra) {
        return wearsWorkingElytra || CultivationGlide.canGlide((Player) (Object) this);
    }
}
