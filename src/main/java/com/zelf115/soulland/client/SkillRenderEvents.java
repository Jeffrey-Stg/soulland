package com.zelf115.soulland.client;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.cultivation.skill.SkillEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Skill projectiles draw nothing themselves; the particle trail the server sends is their look. */
@EventBusSubscriber(modid = SoulLand.MODID, value = Dist.CLIENT)
public final class SkillRenderEvents {
    private SkillRenderEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SkillEntities.SKILL_PROJECTILE.get(), NoopRenderer::new);
    }
}
