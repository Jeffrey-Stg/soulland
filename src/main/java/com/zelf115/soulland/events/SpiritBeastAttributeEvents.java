package com.zelf115.soulland.events;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.spirit.SpiritBeastEntities;
import com.zelf115.soulland.spirit.SpiritBeastEntity;
import com.zelf115.soulland.tournament.SoulMasterEntity;
import com.zelf115.soulland.tournament.TournamentEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = SoulLand.MODID)
public final class SpiritBeastAttributeEvents {
    private SpiritBeastAttributeEvents() {
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(final EntityAttributeCreationEvent event) {
        for (final var spiritBeastType : SpiritBeastEntities.ALL) {
            event.put(spiritBeastType.get(), SpiritBeastEntity.createAttributes().build());
        }
        event.put(TournamentEntities.SOUL_MASTER.get(), SoulMasterEntity.createAttributes().build());
    }
}
