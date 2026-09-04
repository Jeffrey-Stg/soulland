package com.zelf115.soulland.events;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@EventBusSubscriber(modid = SoulLand.MODID)
public class AttributeEvents {
    @SubscribeEvent
    public static void modifyPlayerAttributes(EntityAttributeModificationEvent event) {
        for (final var attribute : Stats.ALL) {
            if (!event.has(EntityType.PLAYER, attribute)) {
                event.add(EntityType.PLAYER, attribute);
            }
        }
    }
}
