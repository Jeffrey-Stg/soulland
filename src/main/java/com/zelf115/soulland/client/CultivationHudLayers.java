package com.zelf115.soulland.client;

import com.zelf115.soulland.SoulLand;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = SoulLand.MODID, value = Dist.CLIENT)
public final class CultivationHudLayers {
    private static final ResourceLocation CULTIVATION_HUD =
            ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "cultivation_hud");

    private CultivationHudLayers() {
    }

    @SubscribeEvent
    public static void registerGuiLayers(final RegisterGuiLayersEvent event) {
        event.registerAboveAll(CULTIVATION_HUD, new CultivationHudLayer());
    }
}
