package com.zelf115.soulland.client;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.network.CultivationActionPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = SoulLand.MODID, value = Dist.CLIENT)
public final class CultivationClientEvents {
    private CultivationClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        while (CultivationKeyMappings.START_MEDITATION.consumeClick()) {
            ClientPacketDistributor.sendToServer(new CultivationActionPayload(CultivationActionPayload.START_MEDITATION));
        }
        while (CultivationKeyMappings.INCREASE_SPEED.consumeClick()) {
            ClientPacketDistributor.sendToServer(new CultivationActionPayload(CultivationActionPayload.INCREASE_SPEED));
        }
        while (CultivationKeyMappings.DECREASE_SPEED.consumeClick()) {
            ClientPacketDistributor.sendToServer(new CultivationActionPayload(CultivationActionPayload.DECREASE_SPEED));
        }
    }
}
