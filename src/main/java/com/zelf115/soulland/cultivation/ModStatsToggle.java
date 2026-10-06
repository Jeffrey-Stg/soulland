package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.Stats;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Lets a player set aside what the mod's stats do to their health, damage, armor and speed, and take
 * it back up later; the stats themselves are kept either way.
 */
public final class ModStatsToggle {

    private ModStatsToggle() {
    }

    public static void toggle(final Player player, final CultivationData data) {
        data.setModStatsEnabled(!data.areModStatsEnabled());
        Stats.syncDerivedPlayerStats(player, data);
        player.displayClientMessage(Component.translatable(data.areModStatsEnabled()
                ? "soulland.cultivation.mod_stats.enabled"
                : "soulland.cultivation.mod_stats.disabled"), true);
    }
}
