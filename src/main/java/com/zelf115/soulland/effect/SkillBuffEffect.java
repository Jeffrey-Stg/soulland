package com.zelf115.soulland.effect;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/** A timed skill buff raising one stat by one point per effect level. */
public final class SkillBuffEffect extends MobEffect {

    private static final double POINTS_PER_LEVEL = 1.0;

    public SkillBuffEffect(final int color, final Holder<Attribute> stat, final String name) {
        super(MobEffectCategory.BENEFICIAL, color);
        addAttributeModifier(stat, ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "effect." + name),
                POINTS_PER_LEVEL, AttributeModifier.Operation.ADD_VALUE);
    }
}
