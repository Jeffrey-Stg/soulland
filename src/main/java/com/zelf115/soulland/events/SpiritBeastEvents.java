package com.zelf115.soulland.events;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.spirit.SpiritBeastEntity;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import com.zelf115.soulland.spirit.SpiritBosses;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = SoulLand.MODID)
public final class SpiritBeastEvents {
    private SpiritBeastEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(final EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof SpiritBeastEntity spiritBeast) {
            SpiritBeastManager.ensureSpiritBeast(spiritBeast);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(final LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof SpiritBeastEntity spiritBeast)) {
            return;
        }

        spiritBeast.spawnAtLocation(SpiritBeastManager.createSoulRing(spiritBeast));
        if (SpiritBosses.isBoss(spiritBeast.getType())) {
            SpiritBeastManager.createBossBones(spiritBeast).forEach(spiritBeast::spawnAtLocation);
            return;
        }
        if (SpiritBeastManager.rollsSpiritBone(spiritBeast)) {
            spiritBeast.spawnAtLocation(SpiritBeastManager.createSpiritBone(spiritBeast));
        }
    }
}
