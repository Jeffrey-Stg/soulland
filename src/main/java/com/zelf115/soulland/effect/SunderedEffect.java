package com.zelf115.soulland.effect;

import com.zelf115.soulland.DerivedStats;
import com.zelf115.soulland.SoulLand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Strips the armor one defense point grants for every effect level. */
public final class SunderedEffect extends MobEffect {

    private static final double DEFENSE_LOST_PER_LEVEL = 1.0;

    public SunderedEffect(final int color) {
        super(MobEffectCategory.HARMFUL, color);
        addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "effect.sundered"),
                -DerivedStats.armorFor(DEFENSE_LOST_PER_LEVEL), AttributeModifier.Operation.ADD_VALUE);
    }
}
