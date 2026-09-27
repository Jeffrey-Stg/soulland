package com.zelf115.soulland.client;

import com.zelf115.soulland.network.RingDisplayPayload;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Client-side cache of every tracked player's shown soul rings, read by {@link SoulRingRenderer}. */
public final class RingDisplayClientData {

    private static final Map<UUID, List<Integer>> RING_TIERS_BY_PLAYER = new ConcurrentHashMap<>();

    private RingDisplayClientData() {
    }

    public static void update(final RingDisplayPayload payload) {
        if (payload.ringTiers().isEmpty()) {
            RING_TIERS_BY_PLAYER.remove(payload.playerId());
            return;
        }
        RING_TIERS_BY_PLAYER.put(payload.playerId(), List.copyOf(payload.ringTiers()));
    }

    public static List<Integer> ringsFor(final UUID playerId) {
        return RING_TIERS_BY_PLAYER.getOrDefault(playerId, List.of());
    }

    public static void clear() {
        RING_TIERS_BY_PLAYER.clear();
    }
}
