package com.zelf115.soulland.client;

import com.zelf115.soulland.network.HudSyncPayload;

/** Client-side cache of the latest {@link HudSyncPayload}, read by {@link CultivationHudLayer}. */
public final class HudClientData {

    private static volatile HudSyncPayload latest = null;

    private HudClientData() {
    }

    public static void update(final HudSyncPayload payload) {
        latest = payload;
    }

    public static HudSyncPayload latest() {
        return latest;
    }
}
