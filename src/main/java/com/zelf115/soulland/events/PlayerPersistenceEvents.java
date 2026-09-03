package com.zelf115.soulland.events;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Carries cultivation stats onto the player entity that replaces one who died or changed dimension.
 *
 * <p>The cultivation attachment copies itself, but the mod attributes live on the entity attribute
 * map, which vanilla rebuilds empty — without this a death resets every stat the player earned.
 */
@EventBusSubscriber(modid = SoulLand.MODID)
public final class PlayerPersistenceEvents {

    private PlayerPersistenceEvents() {
    }

    @SubscribeEvent
    public static void onPlayerClone(final PlayerEvent.Clone event) {
        final Player original = event.getOriginal();
        final Player clone = event.getEntity();
        if (clone.level().isClientSide()) {
            return;
        }

        original.revive();
        Stats.transferTo(original, clone);
        // The vanilla attributes and flight abilities the stats drive are re-derived by the player
        // tick, which does not depend on whether the attachment copy has run yet.
    }
}
