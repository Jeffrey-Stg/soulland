package com.zelf115.soulland.events;

import com.zelf115.soulland.SoulLand;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;

/** Hides technique books in structure chests. */
@EventBusSubscriber(modid = SoulLand.MODID)
public final class TechniqueLootEvents {

    private static final String POOL_NAME = "soulland_technique_books";
    private static final float BOOK_CHANCE = 0.08F;
    private static final Set<ResourceLocation> STRUCTURE_CHESTS = Stream.of(
                    BuiltInLootTables.SIMPLE_DUNGEON,
                    BuiltInLootTables.ABANDONED_MINESHAFT,
                    BuiltInLootTables.DESERT_PYRAMID,
                    BuiltInLootTables.JUNGLE_TEMPLE,
                    BuiltInLootTables.STRONGHOLD_LIBRARY,
                    BuiltInLootTables.ANCIENT_CITY,
                    BuiltInLootTables.WOODLAND_MANSION,
                    BuiltInLootTables.BASTION_TREASURE)
            .map(ResourceKey::location)
            .collect(Collectors.toUnmodifiableSet());

    private TechniqueLootEvents() {
    }

    @SubscribeEvent
    public static void onLootTableLoad(final LootTableLoadEvent event) {
        if (!STRUCTURE_CHESTS.contains(event.getName())) return;

        event.getTable().addPool(LootPool.lootPool()
                .name(POOL_NAME)
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(BOOK_CHANCE))
                .add(LootItem.lootTableItem(SoulLand.PURPLE_DEMON_EYE_BOOK.get()))
                .add(LootItem.lootTableItem(SoulLand.GHOSTLY_SHADOW_STEP_BOOK.get()))
                .add(LootItem.lootTableItem(SoulLand.MYSTERIOUS_HAVEN_BOOK.get()))
                .build());
    }
}
