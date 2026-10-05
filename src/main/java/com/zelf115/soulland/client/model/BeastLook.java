package com.zelf115.soulland.client.model;

import com.zelf115.soulland.spirit.SpiritBeastEntity;
import net.minecraft.client.model.EntityModel;

/** How a spirit beast is drawn: its model, the shadow under it and how much the model is scaled. */
public record BeastLook(EntityModel<SpiritBeastEntity> model, float shadowRadius, float scale) {
    private static final float FULL_SIZE = 1.0F;

    public BeastLook(final EntityModel<SpiritBeastEntity> model, final float shadowRadius) {
        this(model, shadowRadius, FULL_SIZE);
    }
}
