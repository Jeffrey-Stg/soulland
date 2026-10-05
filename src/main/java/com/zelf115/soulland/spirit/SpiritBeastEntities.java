package com.zelf115.soulland.spirit;

import com.zelf115.soulland.SoulLand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final List<DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>>> AMPHIBIOUS_INTERNAL = new ArrayList<>();
    public static final List<DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>>> AMPHIBIOUS = Collections.unmodifiableList(AMPHIBIOUS_INTERNAL);
    private static final Map<String, SpiritBeastShape> SHAPES = new HashMap<>();

    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> WHITE_TIGER = register("white_tiger", SpiritBeastShape.BIG_CAT);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> HELL_SPIRIT_CAT = register("hell_spirit_cat", SpiritBeastShape.BIG_CAT);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> GOLDEN_DRAGON = register("golden_dragon", SpiritBeastShape.DRAGON);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> SILVER_DRAGON = register("silver_dragon", SpiritBeastShape.DRAGON);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_JADE_SCORPION = register("ice_jade_scorpion", SpiritBeastShape.SCORPION);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> BLUE_LIGHTNING_DRAGON = register("blue_lightning_dragon", SpiritBeastShape.DRAGON);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> PHOENIX = register("phoenix", SpiritBeastShape.BIRD);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> FIRE_PHOENIX = register("fire_phoenix", SpiritBeastShape.BIRD);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> EVIL_PHOENIX = register("evil_phoenix", SpiritBeastShape.BIRD);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> RADIANT_PHOENIX = register("radiant_phoenix", SpiritBeastShape.BIRD);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_PHOENIX = register("ice_phoenix", SpiritBeastShape.BIRD);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> BLACK_FLAME_PHOENIX = register("black_flame_phoenix", SpiritBeastShape.BIRD);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> EVIL_FIRE_PHOENIX = register("evil_fire_phoenix", SpiritBeastShape.BIRD);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> HEAVENLY_PHOENIX = register("heavenly_phoenix", SpiritBeastShape.BIRD);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> DARKNESS_PHOENIX = register("darkness_phoenix", SpiritBeastShape.BIRD);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> EMERALD_PHOENIX = register("emerald_phoenix", SpiritBeastShape.BIRD);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> DEMON_WHITE_SHARK = registerAmphibious("demon_white_shark", SpiritBeastShape.SHARK);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ROMANTI_SNAKE = register("romanti_snake", SpiritBeastShape.SERPENT);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> MANFACED_DEMON_SPIDER = register("manfaced_demon_spider", SpiritBeastShape.SPIDER);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> PHANTOM_TIGER = register("phantom_tiger", SpiritBeastShape.BIG_CAT);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> PIT_DEMON_SPIDER = register("pit_demon_spider", SpiritBeastShape.SPIDER);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> DARK_DEVILGOD_TIGER = register("dark_devilgod_tiger", SpiritBeastShape.BIG_CAT);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> EVIL_SPIRIT_ORCA = registerAmphibious("evil_spirit_orca", SpiritBeastShape.ORCA);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> NINE_KNOT_ICHTHYOSAUR = registerAmphibious("nine_knot_ichthyosaur", SpiritBeastShape.SERPENT);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> WIND_SPIRIT_WOLF = register("wind_spirit_wolf", SpiritBeastShape.CANINE);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> WIND_BABOON = register("wind_baboon", SpiritBeastShape.APE);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> DARK_GOLD_TERROR_CLAW_BEAR = register("dark_gold_terror_claw_bear", SpiritBeastShape.BEAR);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ELEMENTAL_SILVER_WOLF = register("elemental_silver_wolf", SpiritBeastShape.CANINE);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_SILK_WORM = register("ice_silk_worm", SpiritBeastShape.SERPENT);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> FLAME_LION = register("flame_lion", SpiritBeastShape.LION);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> BLOOD_DEMONIC_BEAR = register("blood_demonic_bear", SpiritBeastShape.BEAR);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> GOLD_WOLF = register("gold_wolf", SpiritBeastShape.CANINE);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> SILVERBRAVE = register("silverbrave", SpiritBeastShape.CANINE);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> SCARLET_FIRE_MONKEY = register("scarlet_fire_monkey", SpiritBeastShape.APE);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> THREE_EYED_GOLDEN_LION = register("three_eyed_golden_lion", SpiritBeastShape.LION);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ABYSS_DEMON_DRAGON = register("abyss_demon_dragon", SpiritBeastShape.DRAGON);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> EVILEYE_TYRANT = register("evileye_tyrant", SpiritBeastShape.FLOATING_EYE);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> GOLDEN_EYED_BLACK_DRAGON = register("golden_eyed_black_dragon", SpiritBeastShape.DRAGON);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_DEVIL_TITAN = register("ice_devil_titan", SpiritBeastShape.HORNED_APE);
    // Canon spirit beast name from Soul Land.
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> STAR_ANISE = register("star_anise", SpiritBeastShape.BEAR);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_BEAR = register("ice_bear", SpiritBeastShape.BEAR);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> BLAZING_DEMON_LION = register("blazing_demon_lion", SpiritBeastShape.LION);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_FIRE_DEMONIC_TIGER = register("ice_fire_demonic_tiger", SpiritBeastShape.BIG_CAT);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> POISON_QUILL_PORCUPINE = register("poison_quill_porcupine", SpiritBeastShape.PORCUPINE);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> EMERALD_DEMON_BIRD = register("emerald_demon_bird", SpiritBeastShape.BIRD);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> TYRANT_DRAGON = register("tyrant_dragon", SpiritBeastShape.DRAGON);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> THREE_EYED_DEMON_APE = register("three_eyed_demon_ape", SpiritBeastShape.APE);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> GIANT_OCTOPUS = registerAmphibious("giant_octopus", SpiritBeastShape.OCTOPUS);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> TITANOHIPPO = registerAmphibious("titanohippo", SpiritBeastShape.HEAVY);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> LAVA_HOUND = register("lava_hound", SpiritBeastShape.CANINE);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> GOLD_EYED_LEOPARD = register("gold_eyed_leopard", SpiritBeastShape.BIG_CAT);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> GOLD_SILK_APE = register("gold_silk_ape", SpiritBeastShape.APE);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> PURPLE_THUNDER_BEAR = register("purple_thunder_bear", SpiritBeastShape.BEAR);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> YIN_YANG_CHAOS_BIRD = register("yin_yang_chaos_bird", SpiritBeastShape.BIRD);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> LIGHTNING_BLUE_LEOPARD = register("lightning_blue_leopard", SpiritBeastShape.BIG_CAT);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> THUNDER_DRAGON = register("thunder_dragon", SpiritBeastShape.DRAGON);

    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_JADE_SCORPION_EMPRESS = register("ice_jade_scorpion_empress", SpiritBeastShape.SCORPION);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> ICE_BEAR_KING = register("ice_bear_king", SpiritBeastShape.BEAR);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> SKYDREAM_ICE_WORM = register("skydream_ice_worm", SpiritBeastShape.SERPENT);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> THREE_EYED_GOLDEN_LION_BOSS = register("three_eyed_golden_lion_boss", SpiritBeastShape.LION);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> SKY_AZURE_BULL_PYTHON = register("sky_azure_bull_python", SpiritBeastShape.SERPENT);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> TITAN_GIANT_APE = register("titan_giant_ape", SpiritBeastShape.HORNED_APE);
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> EVIL_SPIRIT_ORCA_KING = registerAmphibious("evil_spirit_orca_king", SpiritBeastShape.ORCA);

    private SpiritBeastEntities() {
    }

    public static void register(final IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    public static SpiritBeastShape shapeOf(final EntityType<? extends SpiritBeastEntity> type) {
        return SHAPES.get(EntityType.getKey(type).getPath());
    }

    private static DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> register(final String id, final SpiritBeastShape shape) {
        return register(id, shape, SpiritBeastEntity::new);
    }

    private static DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> registerAmphibious(
            final String id, final SpiritBeastShape shape) {
        final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> holder =
                register(id, shape, AmphibiousSpiritBeastEntity::new);
        AMPHIBIOUS_INTERNAL.add(holder);
        return holder;
    }

    private static DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> register(
            final String id, final SpiritBeastShape shape, final EntityType.EntityFactory<SpiritBeastEntity> factory) {
        SHAPES.put(id, shape);
        final DeferredHolder<EntityType<?>, EntityType<SpiritBeastEntity>> holder = ENTITY_TYPES.register(id,
                () -> EntityType.Builder.of(factory, MobCategory.MONSTER)
                        .sized(shape.width(), shape.height())
                        .clientTrackingRange(8)
                        .build(SoulLand.MODID + ":" + id));
        ALL_INTERNAL.add(holder);
        return holder;
    }
}
