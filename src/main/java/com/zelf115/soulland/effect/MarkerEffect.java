package com.zelf115.soulland.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** An effect that does nothing on its own; skill code checks for it and reads its level. */
public final class MarkerEffect extends MobEffect {
    public MarkerEffect(final MobEffectCategory category, final int color) {
        super(category, color);
    }
}
