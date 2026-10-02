package com.zelf115.soulland.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.zelf115.soulland.cultivation.CultivationGlide;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Treats a mid-air jump by a gliding cultivator like one made wearing an elytra, so it starts the glide. */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean soulland$allowCultivationGlide(final boolean wearsWorkingElytra) {
        return wearsWorkingElytra || CultivationGlide.canGlide((LocalPlayer) (Object) this);
    }
}
