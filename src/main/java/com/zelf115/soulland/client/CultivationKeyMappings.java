package com.zelf115.soulland.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "soulland", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class CultivationKeyMappings {
    public static final KeyMapping START_MEDITATION = new KeyMapping("key.soulland.start_meditation", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, KeyMapping.CATEGORY_MISC);
    public static final KeyMapping INCREASE_SPEED = new KeyMapping("key.soulland.increase_speed_usage", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, KeyMapping.CATEGORY_MISC);
    public static final KeyMapping DECREASE_SPEED = new KeyMapping("key.soulland.decrease_speed_usage", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, KeyMapping.CATEGORY_MISC);

    private CultivationKeyMappings() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        event.register(START_MEDITATION);
        event.register(INCREASE_SPEED);
        event.register(DECREASE_SPEED);
    }
}
