package com.zelf115.soulland.events;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.spirit.SpiritBeastManager;
import net.minecraft.world.entity.monster.Monster;
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
        if (event.getEntity() instanceof Monster monster) {
            SpiritBeastManager.ensureSpiritBeast(monster);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(final LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof Monster monster)) {
            return;
        }

        // For now every hostile mob acts as a spirit beast placeholder until dedicated beast entities are added.
        monster.spawnAtLocation(SpiritBeastManager.createSoulRing(monster));
        if (SpiritBeastManager.getTier(monster) >= 4 && monster.getRandom().nextDouble() < 0.02D) {
            monster.spawnAtLocation(SpiritBeastManager.createSpiritBone(monster));
        }
    }
}
