package com.zelf115.soulland;

import com.zelf115.soulland.effect.MeditationEffect;
import com.zelf115.soulland.block.CrystalBuddingBlock;
import com.zelf115.soulland.block.PillFurnaceBlock;
import com.zelf115.soulland.events.AttributeEvents;
import com.zelf115.soulland.fluid.SoulLandFluids;
import com.zelf115.soulland.item.SoulRingItem;
import com.zelf115.soulland.item.SpiritBoneItem;
import com.zelf115.soulland.item.HerbItem;
import com.zelf115.soulland.item.MartialSoulSwordItem;
import com.zelf115.soulland.item.AlchemyItem;
import com.zelf115.soulland.item.PillFurnaceItem;
import com.zelf115.soulland.network.CultivationNetwork;
import com.zelf115.soulland.spirit.SpiritBeastEntities;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import com.zelf115.soulland.feature.SoulLandFeatures;
import com.zelf115.soulland.qi.QiCommand;
import com.zelf115.soulland.worldgen.SoulLandRegions;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SoulLand.MODID)
public class SoulLand {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "soulland";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "soulland" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "soulland" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "soulland" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);

    // Creates a new Block with the id "soulland:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "soulland:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);
    public static final DeferredBlock<Block> FIRE_CRYSTAL = BLOCKS.registerSimpleBlock("fire_crystal",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.5F).sound(SoundType.AMETHYST));
        public static final DeferredItem<BlockItem> FIRE_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem("fire_crystal", FIRE_CRYSTAL);
    public static final DeferredBlock<Block> ICE_CRYSTAL = BLOCKS.registerSimpleBlock("ice_crystal",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(1.5F).sound(SoundType.AMETHYST));
        public static final DeferredItem<BlockItem> ICE_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem("ice_crystal", ICE_CRYSTAL);
            public static final DeferredBlock<AmethystClusterBlock> FIRE_CRYSTAL_CLUSTER = BLOCKS.register("fire_crystal_cluster",
                () -> new AmethystClusterBlock(7.0F, 3.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.AMETHYST_CLUSTER)));
            public static final DeferredItem<BlockItem> FIRE_CRYSTAL_CLUSTER_ITEM = ITEMS.registerSimpleBlockItem("fire_crystal_cluster", FIRE_CRYSTAL_CLUSTER);
            public static final DeferredBlock<AmethystClusterBlock> FIRE_CRYSTAL_SMALL_BUD = registerCrystalBud("fire_crystal_small_bud", 3.0F, 4.0F, MapColor.COLOR_RED);
            public static final DeferredBlock<AmethystClusterBlock> FIRE_CRYSTAL_MEDIUM_BUD = registerCrystalBud("fire_crystal_medium_bud", 4.0F, 3.0F, MapColor.COLOR_RED);
            public static final DeferredBlock<AmethystClusterBlock> FIRE_CRYSTAL_LARGE_BUD = registerCrystalBud("fire_crystal_large_bud", 5.0F, 3.0F, MapColor.COLOR_RED);
            public static final DeferredBlock<CrystalBuddingBlock> BUDDING_FIRE_CRYSTAL = BLOCKS.register("budding_fire_crystal",
                () -> new CrystalBuddingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BUDDING_AMETHYST)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.AMETHYST_CLUSTER),
                    FIRE_CRYSTAL_SMALL_BUD, FIRE_CRYSTAL_MEDIUM_BUD, FIRE_CRYSTAL_LARGE_BUD, FIRE_CRYSTAL_CLUSTER));
            public static final DeferredItem<BlockItem> BUDDING_FIRE_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem("budding_fire_crystal", BUDDING_FIRE_CRYSTAL);
            public static final DeferredItem<BlockItem> FIRE_CRYSTAL_SMALL_BUD_ITEM = ITEMS.registerSimpleBlockItem("fire_crystal_small_bud", FIRE_CRYSTAL_SMALL_BUD);
            public static final DeferredItem<BlockItem> FIRE_CRYSTAL_MEDIUM_BUD_ITEM = ITEMS.registerSimpleBlockItem("fire_crystal_medium_bud", FIRE_CRYSTAL_MEDIUM_BUD);
            public static final DeferredItem<BlockItem> FIRE_CRYSTAL_LARGE_BUD_ITEM = ITEMS.registerSimpleBlockItem("fire_crystal_large_bud", FIRE_CRYSTAL_LARGE_BUD);
            public static final DeferredBlock<AmethystClusterBlock> ICE_CRYSTAL_CLUSTER = BLOCKS.register("ice_crystal_cluster",
                () -> new AmethystClusterBlock(7.0F, 3.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER)
                    .mapColor(MapColor.COLOR_CYAN).sound(SoundType.AMETHYST_CLUSTER)));
            public static final DeferredItem<BlockItem> ICE_CRYSTAL_CLUSTER_ITEM = ITEMS.registerSimpleBlockItem("ice_crystal_cluster", ICE_CRYSTAL_CLUSTER);
            public static final DeferredBlock<AmethystClusterBlock> ICE_CRYSTAL_SMALL_BUD = registerCrystalBud("ice_crystal_small_bud", 3.0F, 4.0F, MapColor.COLOR_CYAN);
            public static final DeferredBlock<AmethystClusterBlock> ICE_CRYSTAL_MEDIUM_BUD = registerCrystalBud("ice_crystal_medium_bud", 4.0F, 3.0F, MapColor.COLOR_CYAN);
            public static final DeferredBlock<AmethystClusterBlock> ICE_CRYSTAL_LARGE_BUD = registerCrystalBud("ice_crystal_large_bud", 5.0F, 3.0F, MapColor.COLOR_CYAN);
            public static final DeferredBlock<CrystalBuddingBlock> BUDDING_ICE_CRYSTAL = BLOCKS.register("budding_ice_crystal",
                () -> new CrystalBuddingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BUDDING_AMETHYST)
                    .mapColor(MapColor.COLOR_CYAN).sound(SoundType.AMETHYST_CLUSTER),
                    ICE_CRYSTAL_SMALL_BUD, ICE_CRYSTAL_MEDIUM_BUD, ICE_CRYSTAL_LARGE_BUD, ICE_CRYSTAL_CLUSTER));
            public static final DeferredItem<BlockItem> BUDDING_ICE_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem("budding_ice_crystal", BUDDING_ICE_CRYSTAL);
            public static final DeferredItem<BlockItem> ICE_CRYSTAL_SMALL_BUD_ITEM = ITEMS.registerSimpleBlockItem("ice_crystal_small_bud", ICE_CRYSTAL_SMALL_BUD);
            public static final DeferredItem<BlockItem> ICE_CRYSTAL_MEDIUM_BUD_ITEM = ITEMS.registerSimpleBlockItem("ice_crystal_medium_bud", ICE_CRYSTAL_MEDIUM_BUD);
            public static final DeferredItem<BlockItem> ICE_CRYSTAL_LARGE_BUD_ITEM = ITEMS.registerSimpleBlockItem("ice_crystal_large_bud", ICE_CRYSTAL_LARGE_BUD);
            public static final DeferredItem<Item> FIRE_CRYSTAL_SHARD = ITEMS.registerSimpleItem("fire_crystal_shard");
            public static final DeferredItem<Item> ICE_CRYSTAL_SHARD = ITEMS.registerSimpleItem("ice_crystal_shard");
    public static final DeferredItem<Item> SOUL_RING_ITEM = ITEMS.register("soul_ring", () -> new SoulRingItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SPIRIT_BONE_ITEM = ITEMS.register("spirit_bone", () -> new SpiritBoneItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CLEAR_SKY_HAMMER = ITEMS.register("clear_sky_hammer", () -> new MartialSoulSwordItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SEVEN_KILL_SWORD = ITEMS.register("seven_kill_sword", () -> new MartialSoulSwordItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> NINE_HEART_BEGONIA = ITEMS.registerSimpleItem("nine_heart_begonia");
    public static final DeferredItem<Item> SEVEN_TREASURE_GLAZED_TILE_PAGODA = ITEMS.registerSimpleItem("seven_treasure_glazed_tile_pagoda");
        public static final DeferredBlock<Block> PILL_FURNACE_BLOCK = BLOCKS.register("pill_furnace",
            () -> new FurnaceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE)));
        public static final DeferredItem<BlockItem> PILL_FURNACE = ITEMS.registerSimpleBlockItem("pill_furnace", PILL_FURNACE_BLOCK);
    public static final DeferredBlock<Block> ENCHANTED_PILL_FURNACE_BLOCK = registerPillFurnaceBlock("enchanted_pill_furnace", PillFurnaceItem.Tier.ENCHANTED);
    public static final DeferredItem<BlockItem> ENCHANTED_PILL_FURNACE = ITEMS.registerSimpleBlockItem("enchanted_pill_furnace", ENCHANTED_PILL_FURNACE_BLOCK);
    public static final DeferredBlock<Block> NETHER_PILL_FURNACE_BLOCK = registerPillFurnaceBlock("nether_pill_furnace", PillFurnaceItem.Tier.NETHER);
    public static final DeferredItem<BlockItem> NETHER_PILL_FURNACE = ITEMS.registerSimpleBlockItem("nether_pill_furnace", NETHER_PILL_FURNACE_BLOCK);
    public static final DeferredBlock<Block> STAR_PILL_FURNACE_BLOCK = registerPillFurnaceBlock("star_pill_furnace", PillFurnaceItem.Tier.STAR);
    public static final DeferredItem<BlockItem> STAR_PILL_FURNACE = ITEMS.registerSimpleBlockItem("star_pill_furnace", STAR_PILL_FURNACE_BLOCK);
    public static final DeferredBlock<Block> DIVINE_PILL_FURNACE_BLOCK = registerPillFurnaceBlock("divine_pill_furnace", PillFurnaceItem.Tier.DIVINE);
    public static final DeferredItem<BlockItem> DIVINE_PILL_FURNACE = ITEMS.registerSimpleBlockItem("divine_pill_furnace", DIVINE_PILL_FURNACE_BLOCK);
    public static final DeferredItem<Item> MYSTERIOUS_WATER_PILL = registerPill("mysterious_water_pill", AlchemyItem.Effect.MYSTERIOUS_WATER, 0);
    public static final DeferredItem<Item> SPIRIT_ASCENSION_PILL = registerPill("spirit_ascension_pill", AlchemyItem.Effect.SPIRIT_ASCENSION, 0);
    public static final DeferredItem<Item> QI_GATHERING_PILL_TIER_1 = registerPill("qi_gathering_pill_tier_1", AlchemyItem.Effect.QI_GATHERING, 1);
    public static final DeferredItem<Item> QI_GATHERING_PILL_TIER_2 = registerPill("qi_gathering_pill_tier_2", AlchemyItem.Effect.QI_GATHERING, 2);
    public static final DeferredItem<Item> QI_GATHERING_PILL_TIER_3 = registerPill("qi_gathering_pill_tier_3", AlchemyItem.Effect.QI_GATHERING, 3);
    public static final DeferredItem<Item> QI_GATHERING_PILL_TIER_4 = registerPill("qi_gathering_pill_tier_4", AlchemyItem.Effect.QI_GATHERING, 4);
    public static final DeferredItem<Item> QI_GATHERING_PILL_TIER_5 = registerPill("qi_gathering_pill_tier_5", AlchemyItem.Effect.QI_GATHERING, 5);
    public static final DeferredItem<Item> BEAUTIFUL_SILK_TULIP = registerHerb("beautiful_silk_tulip", HerbItem.Effect.EVOLVE_PAGODA);
    public static final DeferredItem<Item> BLACK_JADE_DIVINE_BAMBOO = registerHerb("black_jade_divine_bamboo", HerbItem.Effect.SPIRIT_HUNDRED);
    public static final DeferredItem<Item> COMMON_SPIRIT_HERB = registerHerb("common_spirit_herb", HerbItem.Effect.COMMON_SPIRIT);
    public static final DeferredItem<Item> DRAGONSCALE_FRUIT = registerHerb("dragonscale_fruit", HerbItem.Effect.ALL_BUT_SPIRIT_AND_INNATE_ONE_SOUL_END);
    public static final DeferredItem<Item> EIGHT_PETAL_IMMORTAL_ORCHID = registerHerb("eight_petal_immortal_orchid", HerbItem.Effect.CULTIVATION_LEVEL_ONE);
    public static final DeferredItem<Item> FULL_MOON_WEARING_AUTUMN_DEW = registerHerb("full_moon_wearing_autumn_dew", HerbItem.Effect.SPIRIT_FIFTY);
    public static final DeferredItem<Item> ICE_CRYSTAL_FRUIT = registerHerb("ice_crystal_fruit", HerbItem.Effect.ICE_SPIRIT_TEN);
    public static final DeferredItem<Item> INFERNAL_DELICATE_APRICOT = registerHerb("infernal_delicate_apricot", HerbItem.Effect.FIRE_CULTIVATION_SPEED_TWO);
    public static final DeferredItem<Item> OCTAGONAL_MYSTERIOUS_ICE_GRASS = registerHerb("octagonal_mysterious_ice_grass", HerbItem.Effect.ICE_CULTIVATION_SPEED_TWO);
    public static final DeferredItem<Item> ORIGIN_ENERGY_IMMORTAL_GRASS = registerHerb("origin_energy_immortal_grass", HerbItem.Effect.CULTIVATION_LEVEL_ONE);
    public static final DeferredItem<Item> SACRED_SOUL_GRASS = registerHerb("sacred_soul_grass", HerbItem.Effect.CULTIVATION_LEVEL_HALF);
    public static final DeferredItem<Item> SCARLET_FLAME_FRUIT = registerHerb("scarlet_flame_fruit", HerbItem.Effect.FIRE_SPIRIT_TEN);
    public static final DeferredItem<Item> SINGULAR_VELVET_SKY_CHRYSANTHEMUM = registerHerb("singular_velvet_sky_chrysanthemum", HerbItem.Effect.INNATE_ONE);
    public static final DeferredItem<Item> WATER_CRYSTAL_PEACH = registerHerb("water_crystal_peach", HerbItem.Effect.ALL_BUT_SPIRIT_ONE);
    public static final DeferredItem<Item> YEARNING_HEARTBROKEN_RED = registerHerb("yearning_heartbroken_red", HerbItem.Effect.INNATE_TWO);
    public static final DeferredHolder<MobEffect, MobEffect> MEDITATION_EFFECT = MOB_EFFECTS.register("meditation", MeditationEffect::new);

    private static DeferredItem<Item> registerHerb(String name, HerbItem.Effect effect) {
        return ITEMS.register(name, () -> new HerbItem(effect,
                new Item.Properties().food(HerbItem.foodProperties(effect))));
    }

    private static DeferredBlock<Block> registerPillFurnaceBlock(final String name, final PillFurnaceItem.Tier tier) {
        return BLOCKS.register(name, () -> new PillFurnaceBlock(tier,
                BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE)));
    }

    private static DeferredItem<Item> registerPill(final String name, final AlchemyItem.Effect effect, final int qiLevel) {
        return ITEMS.register(name, () -> new AlchemyItem(effect, qiLevel, AlchemyItem.pillProperties()));
    }

    private static DeferredBlock<AmethystClusterBlock> registerCrystalBud(String name, float height, float width, MapColor color) {
        return BLOCKS.register(name, () -> new AmethystClusterBlock(height, width,
                BlockBehaviour.Properties.ofFullCopy(Blocks.SMALL_AMETHYST_BUD).mapColor(color)
                        .sound(SoundType.AMETHYST_CLUSTER)));
    }

    /*
     Creates a new food item with the id "soulland:example_id", nutrition 1 and saturation 2
     public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));
    */
    /*
     Creates a creative tab with the id "soulland:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.soulland")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get());// Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());
    */

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public SoulLand(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        SoulLandFluids.FLUID_TYPES.register(modEventBus);
        SoulLandFluids.FLUIDS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        SpiritBeastEntities.register(modEventBus);
        // Register the Deferred Register to the mod event bus so worldgen features get registered
        SoulLandFeatures.FEATURES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (SoulLand) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        // Register Attributes
        modEventBus.addListener(AttributeEvents::modifyPlayerAttributes);
        modEventBus.addListener(CultivationNetwork::register);
        Stats.register(modEventBus);
        Cultivation.register(modEventBus);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(SoulLandRegions::register);

        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
            event.accept(FIRE_CRYSTAL_ITEM);
            event.accept(ICE_CRYSTAL_ITEM);
            event.accept(FIRE_CRYSTAL_CLUSTER_ITEM);
            event.accept(BUDDING_FIRE_CRYSTAL_ITEM);
            event.accept(FIRE_CRYSTAL_SMALL_BUD_ITEM);
            event.accept(FIRE_CRYSTAL_MEDIUM_BUD_ITEM);
            event.accept(FIRE_CRYSTAL_LARGE_BUD_ITEM);
            event.accept(ICE_CRYSTAL_CLUSTER_ITEM);
            event.accept(BUDDING_ICE_CRYSTAL_ITEM);
            event.accept(ICE_CRYSTAL_SMALL_BUD_ITEM);
            event.accept(ICE_CRYSTAL_MEDIUM_BUD_ITEM);
            event.accept(ICE_CRYSTAL_LARGE_BUD_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(SOUL_RING_ITEM);
            event.accept(SPIRIT_BONE_ITEM);
            event.accept(FIRE_CRYSTAL_SHARD);
            event.accept(ICE_CRYSTAL_SHARD);
            event.accept(SoulLandFluids.RED_WATER_BUCKET);
            event.accept(SoulLandFluids.CYAN_WATER_BUCKET);
            event.accept(NINE_HEART_BEGONIA);
            event.accept(SEVEN_TREASURE_GLAZED_TILE_PAGODA);
            event.accept(BEAUTIFUL_SILK_TULIP);
            event.accept(BLACK_JADE_DIVINE_BAMBOO);
            event.accept(COMMON_SPIRIT_HERB);
            event.accept(DRAGONSCALE_FRUIT);
            event.accept(EIGHT_PETAL_IMMORTAL_ORCHID);
            event.accept(FULL_MOON_WEARING_AUTUMN_DEW);
            event.accept(ICE_CRYSTAL_FRUIT);
            event.accept(INFERNAL_DELICATE_APRICOT);
            event.accept(OCTAGONAL_MYSTERIOUS_ICE_GRASS);
            event.accept(ORIGIN_ENERGY_IMMORTAL_GRASS);
            event.accept(SACRED_SOUL_GRASS);
            event.accept(SCARLET_FLAME_FRUIT);
            event.accept(SINGULAR_VELVET_SKY_CHRYSANTHEMUM);
            event.accept(WATER_CRYSTAL_PEACH);
            event.accept(YEARNING_HEARTBROKEN_RED);
            event.accept(PILL_FURNACE);
            event.accept(ENCHANTED_PILL_FURNACE);
            event.accept(NETHER_PILL_FURNACE);
            event.accept(STAR_PILL_FURNACE);
            event.accept(DIVINE_PILL_FURNACE);
            event.accept(MYSTERIOUS_WATER_PILL);
            event.accept(SPIRIT_ASCENSION_PILL);
            event.accept(QI_GATHERING_PILL_TIER_1);
            event.accept(QI_GATHERING_PILL_TIER_2);
            event.accept(QI_GATHERING_PILL_TIER_3);
            event.accept(QI_GATHERING_PILL_TIER_4);
            event.accept(QI_GATHERING_PILL_TIER_5);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(final RegisterCommandsEvent event) {
        QiCommand.register(event.getDispatcher());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
