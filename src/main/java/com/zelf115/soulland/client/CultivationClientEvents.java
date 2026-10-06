package com.zelf115.soulland.client;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.client.screen.CultivationScreen;
import com.zelf115.soulland.network.CultivationActionPayload;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = SoulLand.MODID, value = Dist.CLIENT)
public final class CultivationClientEvents {
    private static final Set<KeyMapping> HELD_CAST_KEYS = new HashSet<>();

    private CultivationClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        sendOnPress(CultivationKeyMappings.START_MEDITATION, CultivationActionPayload.START_MEDITATION);
        sendOnPress(CultivationKeyMappings.CYCLE_RING_DISPLAY, CultivationActionPayload.CYCLE_RING_DISPLAY);
        sendOnPress(CultivationKeyMappings.TOGGLE_EXTERNAL_BONE, CultivationActionPayload.TOGGLE_EXTERNAL_BONE);
        sendOnPress(CultivationKeyMappings.ATTEMPT_BREAKTHROUGH, CultivationActionPayload.ATTEMPT_BREAKTHROUGH);
        sendOnPress(CultivationKeyMappings.USE_MARTIAL_SOUL, CultivationActionPayload.USE_MARTIAL_SOUL);
        sendWhileHeld(CultivationKeyMappings.CAST_MARTIAL_SOUL, CultivationActionPayload.CAST_MARTIAL_SOUL);
        sendWhileHeld(CultivationKeyMappings.CAST_BONE_SKILL, CultivationActionPayload.CAST_BONE_SKILL);
        sendOnPress(CultivationKeyMappings.SELECT_NEXT_BONE, CultivationActionPayload.SELECT_NEXT_BONE);
        sendOnPress(CultivationKeyMappings.OPEN_ALCHEMY_MENU, CultivationActionPayload.OPEN_ALCHEMY_MENU);
        sendOnPress(CultivationKeyMappings.SWITCH_MARTIAL_SOUL, CultivationActionPayload.SWITCH_MARTIAL_SOUL);
        sendOnPress(CultivationKeyMappings.SELECT_NEXT_RING, CultivationActionPayload.SELECT_NEXT_RING);
        sendOnPress(CultivationKeyMappings.DEMON_EYE, CultivationActionPayload.DEMON_EYE);
        sendOnPress(CultivationKeyMappings.DEMON_EYE_STRIKE, CultivationActionPayload.DEMON_EYE_STRIKE);
        sendOnPress(CultivationKeyMappings.SHADOW_STEP, CultivationActionPayload.SHADOW_STEP);
        sendOnPress(CultivationKeyMappings.TOGGLE_MOD_STATS, CultivationActionPayload.TOGGLE_MOD_STATS);
        openScreenOnPress();
    }

    /** Ring and HUD caches belong to one world; the next one starts empty, so glide never reads a stale level. */
    @SubscribeEvent
    public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
        RingDisplayClientData.clear();
        HudClientData.clear();
    }

    private static void openScreenOnPress() {
        while (CultivationKeyMappings.OPEN_CULTIVATION_SCREEN.consumeClick()) {
            CultivationScreen.open();
        }
    }

    private static void sendOnPress(final KeyMapping mapping, final int action) {
        while (mapping.consumeClick()) {
            PacketDistributor.sendToServer(new CultivationActionPayload(action));
        }
    }

    /**
     * Casts once when the key goes down and releases any channel when it comes up. Clicks while the
     * key is already held are key repeats, and would otherwise flip a toggle skill on and off.
     */
    private static void sendWhileHeld(final KeyMapping mapping, final int pressAction) {
        final boolean clicked = drainClicks(mapping);
        if (clicked && !HELD_CAST_KEYS.contains(mapping)) {
            PacketDistributor.sendToServer(new CultivationActionPayload(pressAction));
        }
        if (mapping.isDown()) {
            HELD_CAST_KEYS.add(mapping);
            return;
        }
        if (HELD_CAST_KEYS.remove(mapping) || clicked) {
            PacketDistributor.sendToServer(new CultivationActionPayload(CultivationActionPayload.RELEASE_CHANNEL));
        }
    }

    private static boolean drainClicks(final KeyMapping mapping) {
        boolean clicked = false;
        while (mapping.consumeClick()) {
            clicked = true;
        }
        return clicked;
    }
}
