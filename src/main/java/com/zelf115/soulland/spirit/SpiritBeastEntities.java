package com.zelf115.soulland.spirit;

import com.zelf115.soulland.SoulLand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SpiritBeastEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, SoulLand.MODID);
    private static final List<DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>>> ALL_INTERNAL = new ArrayList<>();
    public static final List<DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>>> ALL = Collections.unmodifiableList(ALL_INTERNAL);

    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> WHITE_TIGER = register("white_tiger");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> HELL_SPIRIT_CAT = register("hell_spirit_cat");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> GOLDEN_DRAGON = register("golden_dragon");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> SILVER_DRAGON = register("silver_dragon");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_JADE_SCORPION = register("ice_jade_scorpion");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> BLUE_LIGHTNING_DRAGON = register("blue_lightning_dragon");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> PHOENIX = register("phoenix");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> FIRE_PHOENIX = register("fire_phoenix");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> EVIL_PHOENIX = register("evil_phoenix");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> RADIANT_PHOENIX = register("radiant_phoenix");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_PHOENIX = register("ice_phoenix");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> BLACK_FLAME_PHOENIX = register("black_flame_phoenix");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> EVIL_FIRE_PHOENIX = register("evil_fire_phoenix");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> HEAVENLY_PHOENIX = register("heavenly_phoenix");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> DARKNESS_PHOENIX = register("darkness_phoenix");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> EMERALD_PHOENIX = register("emerald_phoenix");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> DEMON_WHITE_SHARK = register("demon_white_shark");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ROMANTI_SNAKE = register("romanti_snake");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> MANFACED_DEMON_SPIDER = register("manfaced_demon_spider");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> PHANTOM_TIGER = register("phantom_tiger");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> PIT_DEMON_SPIDER = register("pit_demon_spider");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> DARK_DEVILGOD_TIGER = register("dark_devilgod_tiger");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> EVIL_SPIRIT_ORCA = register("evil_spirit_orca");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> NINE_KNOT_ICHTHYOSAUR = register("nine_knot_ichthyosaur");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> WIND_SPIRIT_WOLF = register("wind_spirit_wolf");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> WIND_BABOON = register("wind_baboon");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> DARK_GOLD_TERROR_CLAW_BEAR = register("dark_gold_terror_claw_bear");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ELEMENTAL_SILVER_WOLF = register("elemental_silver_wolf");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_SILK_WORM = register("ice_silk_worm");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> FLAME_LION = register("flame_lion");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> BLOOD_DEMONIC_BEAR = register("blood_demonic_bear");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> GOLD_WOLF = register("gold_wolf");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> SILVERBRAVE = register("silverbrave");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> SCARLET_FIRE_MONKEY = register("scarlet_fire_monkey");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> THREE_EYED_GOLDEN_LION = register("three_eyed_golden_lion");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ABYSS_DEMON_DRAGON = register("abyss_demon_dragon");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> EVILEYE_TYRANT = register("evileye_tyrant");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> GOLDEN_EYED_BLACK_DRAGON = register("golden_eyed_black_dragon");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_DEVIL_TITAN = register("ice_devil_titan");
    // Canon spirit beast name from Soul Land.
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> STAR_ANISE = register("star_anise");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_BEAR = register("ice_bear");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> BLAZING_DEMON_LION = register("blazing_demon_lion");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_FIRE_DEMONIC_TIGER = register("ice_fire_demonic_tiger");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> POISON_QUILL_PORCUPINE = register("poison_quill_porcupine");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> EMERALD_DEMON_BIRD = register("emerald_demon_bird");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> TYRANT_DRAGON = register("tyrant_dragon");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> THREE_EYED_DEMON_APE = register("three_eyed_demon_ape");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> GIANT_OCTOPUS = register("giant_octopus");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> TITANOHIPPO = register("titanohippo");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> LAVA_HOUND = register("lava_hound");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> GOLD_EYED_LEOPARD = register("gold_eyed_leopard");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> GOLD_SILK_APE = register("gold_silk_ape");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> PURPLE_THUNDER_BEAR = register("purple_thunder_bear");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> YING_YANG_CHAOS_BIRD = register("ying_yang_chaos_bird");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> LIGHTNING_BLUE_LEOPARD = register("lightning_blue_leopard");
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> THUNDER_DRAGON = register("thunder_dragon");

    private SpiritBeastEntities() {
    }

    public static void register(final IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    private static DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> register(final String id) {
        final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> holder = ENTITY_TYPES.register(id,
                // Placeholder size until species-specific models and dimensions are added.
                () -> EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.MONSTER)
                        .sized(0.9F, 1.8F)
                        .clientTrackingRange(8)
                        .build(SoulLand.MODID + ":" + id));
        ALL_INTERNAL.add(holder);
        return holder;
    }
}
