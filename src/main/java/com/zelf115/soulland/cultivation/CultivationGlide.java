package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.client.HudClientData;
import com.zelf115.soulland.network.HudSyncPayload;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Cultivators between {@link CultivationManager#GLIDE_LEVEL} and
 * {@link CultivationManager#CREATIVE_FLIGHT_LEVEL} glide as if they wore an elytra: a second jump in
 * mid-air spreads the wings, exactly as vanilla does for the item.
 */
public final class CultivationGlide {

    private static final int UNKNOWN_LEVEL = 0;

    private CultivationGlide() {
    }

    public static boolean canGlide(final LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        final int level = cultivationLevel(player);
        return level >= CultivationManager.GLIDE_LEVEL && level < CultivationManager.CREATIVE_FLIGHT_LEVEL;
    }

    private static int cultivationLevel(final Player player) {
        if (player.level().isClientSide()) {
            return localPlayerLevel();
        }
        return player.getData(CultivationAttachment.CULTIVATION_DATA.get()).getLevel();
    }

    // The client never receives the attachment, so the HUD sync is its only copy of its own level.
    private static int localPlayerLevel() {
        final HudSyncPayload hud = HudClientData.latest();
        return hud == null ? UNKNOWN_LEVEL : hud.level();
    }
}
