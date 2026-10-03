package com.zelf115.soulland.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.zelf115.soulland.SoulLand;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = SoulLand.MODID, value = Dist.CLIENT)
public final class CultivationKeyMappings {
    private static final String CATEGORY = "key.categories.soulland";

    public static final KeyMapping START_MEDITATION = new KeyMapping("key.soulland.start_meditation", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY);
    public static final KeyMapping INCREASE_SPEED = new KeyMapping("key.soulland.increase_speed_usage", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, CATEGORY);
    public static final KeyMapping DECREASE_SPEED = new KeyMapping("key.soulland.decrease_speed_usage", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, CATEGORY);
    public static final KeyMapping CYCLE_RING_DISPLAY = new KeyMapping("key.soulland.cycle_ring_display", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping TOGGLE_EXTERNAL_BONE = new KeyMapping("key.soulland.toggle_external_bone", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY);
    public static final KeyMapping ATTEMPT_BREAKTHROUGH = new KeyMapping("key.soulland.breakthrough", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);
    public static final KeyMapping USE_MARTIAL_SOUL = new KeyMapping("key.soulland.use_martial_soul", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);
    public static final KeyMapping CAST_MARTIAL_SOUL = new KeyMapping("key.soulland.cast_martial_soul", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY);
    public static final KeyMapping OPEN_ALCHEMY_MENU = new KeyMapping("key.soulland.open_alchemy_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY);
    public static final KeyMapping OPEN_MARTIAL_SOUL_MENU = new KeyMapping("key.soulland.open_martial_soul_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);
    public static final KeyMapping SWITCH_MARTIAL_SOUL = new KeyMapping("key.soulland.switch_martial_soul", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);
    public static final KeyMapping SELECT_NEXT_RING = new KeyMapping("key.soulland.select_next_ring", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY);
    public static final KeyMapping SELECT_NEXT_BONE = new KeyMapping("key.soulland.select_next_bone", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, CATEGORY);
    public static final KeyMapping CAST_BONE_SKILL = new KeyMapping("key.soulland.cast_bone_skill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, CATEGORY);
    public static final KeyMapping DEMON_EYE = new KeyMapping("key.soulland.demon_eye", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY);
    public static final KeyMapping DEMON_EYE_STRIKE = new KeyMapping("key.soulland.demon_eye_strike", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY);
    public static final KeyMapping SHADOW_STEP = new KeyMapping("key.soulland.shadow_step", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
    public static final KeyMapping TOGGLE_HUD = new KeyMapping("key.soulland.toggle_hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, CATEGORY);

    private CultivationKeyMappings() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        event.register(START_MEDITATION);
        event.register(INCREASE_SPEED);
        event.register(DECREASE_SPEED);
        event.register(CYCLE_RING_DISPLAY);
        event.register(TOGGLE_EXTERNAL_BONE);
        event.register(ATTEMPT_BREAKTHROUGH);
        event.register(USE_MARTIAL_SOUL);
        event.register(CAST_MARTIAL_SOUL);
        event.register(OPEN_ALCHEMY_MENU);
        event.register(OPEN_MARTIAL_SOUL_MENU);
        event.register(SWITCH_MARTIAL_SOUL);
        event.register(DEMON_EYE);
        event.register(DEMON_EYE_STRIKE);
        event.register(SHADOW_STEP);
        event.register(SELECT_NEXT_RING);
        event.register(SELECT_NEXT_BONE);
        event.register(CAST_BONE_SKILL);
        event.register(TOGGLE_HUD);
    }
}
