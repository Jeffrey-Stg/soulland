package com.zelf115.soulland.client;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.network.CultivationActionPayload;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = SoulLand.MODID, value = Dist.CLIENT)
public final class CultivationClientEvents {
    private CultivationClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        sendOnPress(CultivationKeyMappings.START_MEDITATION, CultivationActionPayload.START_MEDITATION);
        sendOnPress(CultivationKeyMappings.INCREASE_SPEED, CultivationActionPayload.INCREASE_SPEED);
        sendOnPress(CultivationKeyMappings.DECREASE_SPEED, CultivationActionPayload.DECREASE_SPEED);
        sendOnPress(CultivationKeyMappings.CYCLE_RING_DISPLAY, CultivationActionPayload.CYCLE_RING_DISPLAY);
        sendOnPress(CultivationKeyMappings.TOGGLE_EXTERNAL_BONE, CultivationActionPayload.TOGGLE_EXTERNAL_BONE);
    }

    private static void sendOnPress(final KeyMapping mapping, final int action) {
        while (mapping.consumeClick()) {
            PacketDistributor.sendToServer(new CultivationActionPayload(action));
        }
    }
}
