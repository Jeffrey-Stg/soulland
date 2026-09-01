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
        // Add to players (using EntityType.PLAYER)
        if (!event.has(EntityType.PLAYER, Stats.DAMAGE)) {
            event.add(EntityType.PLAYER, Stats.DAMAGE);
        }
        if (!event.has(EntityType.PLAYER, Stats.DEFENSE)) {
            event.add(EntityType.PLAYER, Stats.DEFENSE);
        }
        if (!event.has(EntityType.PLAYER, Stats.HEALTH)) {
            event.add(EntityType.PLAYER, Stats.HEALTH);
        }
        if (!event.has(EntityType.PLAYER, Stats.SPEED)) {
            event.add(EntityType.PLAYER, Stats.SPEED);
        }
        if (!event.has(EntityType.PLAYER, Stats.SPIRIT)) {
            event.add(EntityType.PLAYER, Stats.SPIRIT);
        }
        if (!event.has(EntityType.PLAYER, Stats.CULTIVATION_SPEED)) {
            event.add(EntityType.PLAYER, Stats.CULTIVATION_SPEED);
        }
    }

}
