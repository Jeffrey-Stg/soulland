package com.zelf115.soulland.events;

import com.zelf115.soulland.SoulLand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;

@EventBusSubscriber(modid = SoulLand.MODID)
public final class AlchemyLootEvents {
    private static final String POOL_PREFIX = "soulland_alchemy_";

    private AlchemyLootEvents() {
    }

    @SubscribeEvent
    public static void onLootTableLoad(final LootTableLoadEvent event) {
        final ResourceLocation table = event.getName();
        if (table.equals(BuiltInLootTables.STRONGHOLD_LIBRARY.location())
                || table.equals(BuiltInLootTables.STRONGHOLD_CROSSING.location())
                || table.equals(BuiltInLootTables.STRONGHOLD_CORRIDOR.location())) {
            addFurnace(event, SoulLand.ENCHANTED_PILL_FURNACE.get(), "enchanted");
        } else if (table.equals(BuiltInLootTables.BASTION_TREASURE.location())
                || table.equals(BuiltInLootTables.BASTION_OTHER.location())
                || table.equals(BuiltInLootTables.BASTION_BRIDGE.location())
                || table.equals(BuiltInLootTables.BASTION_HOGLIN_STABLE.location())
                || table.equals(ResourceLocation.withDefaultNamespace("chests/nether_bridge"))) {
            addFurnace(event, SoulLand.NETHER_PILL_FURNACE.get(), "nether");
        } else if (table.equals(BuiltInLootTables.END_CITY_TREASURE.location())) {
            addFurnace(event, SoulLand.STAR_PILL_FURNACE.get(), "star");
        }
    }

    private static void addFurnace(final LootTableLoadEvent event, final net.minecraft.world.level.ItemLike furnace,
                                   final String tier) {
        event.getTable().addPool(LootPool.lootPool()
                .name(POOL_PREFIX + tier)
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(furnace).setWeight(1))
                .build());
    }
}
