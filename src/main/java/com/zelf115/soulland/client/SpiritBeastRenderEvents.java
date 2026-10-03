package com.zelf115.soulland.client;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.spirit.SpiritBeastEntities;
import com.zelf115.soulland.tournament.TournamentEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = SoulLand.MODID, value = Dist.CLIENT)
public final class SpiritBeastRenderEvents {
    private SpiritBeastRenderEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        for (final var spiritBeastType : SpiritBeastEntities.ALL) {
            event.registerEntityRenderer(spiritBeastType.get(), SpiritBeastRenderer::new);
        }
        event.registerEntityRenderer(TournamentEntities.SOUL_MASTER.get(), SoulMasterRenderer::new);
    }
}
