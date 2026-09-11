package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.spirit.Affinity;
import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalInt;

public enum MartialSoul {
    CLEAR_SKY_HAMMER(Category.TOOL, Origin.INNATE, "Clear Sky Hammer", toolAffinities()),
    SEVEN_KILL_SWORD(Category.TOOL, Origin.INNATE, "Seven Kill Sword", toolAffinities()),
    NINE_HEART_BEGONIA(Category.TOOL, Origin.INNATE, "Nine Heart Begonia", toolAffinities()),
    SEVEN_TREASURE_GLAZED_TILE_PAGODA(Category.TOOL, Origin.INNATE, "Seven Treasure Glazed Tile Pagoda", toolAffinities()),
    WHITE_TIGER(Category.BEAST, Origin.INNATE, "White Tiger", beastAffinities(Affinity.DARK, Affinity.LIGHT, Affinity.BEAST)),
    HELL_SPIRIT_CAT(Category.BEAST, Origin.INNATE, "Hell Spirit Cat", beastAffinities(Affinity.SPIRIT, Affinity.DARK, Affinity.LIGHT, Affinity.BEAST)),
    HOLY_ANGEL(Category.BEAST, Origin.INNATE, "Holy Angel", Map.of(Affinity.LIGHT, 2.0, Affinity.HOLY, 2.0, Affinity.BIRD, 2.0)),
    GOLDEN_DRAGON(Category.BEAST, Origin.INNATE, "Golden Dragon", beastAffinities(Affinity.DRAGON, Affinity.HOLY, Affinity.LIGHT)),
    SILVER_DRAGON(Category.BEAST, Origin.INNATE, "Silver Dragon", beastAffinities(Affinity.DRAGON, Affinity.FIRE, Affinity.ICE, Affinity.DARK, Affinity.LIGHT)),
    ICE_JADE_SCORPION(Category.BEAST, Origin.INNATE, "Ice Jade Scorpion", beastAffinities(Affinity.INSECT, Affinity.POISON, Affinity.ICE)),
    BLUE_LIGHTNING_DRAGON(Category.BEAST, Origin.INNATE, "Blue Lightning Dragon", beastAffinities(Affinity.LIGHTNING, Affinity.DRAGON)),
    PHOENIX(Category.BEAST, Origin.INNATE, "Phoenix", beastAffinities(Affinity.BIRD, Affinity.FIRE)),
    DEMON_SOUL_GREAT_WHITE_SHARK(Category.BEAST, Origin.INNATE, "Demon Soul Great White Shark", beastAffinities(Affinity.ICE, Affinity.BEAST, Affinity.DEMONIC)),
    BLUE_SILVER_GRASS(Category.BEAST, Origin.INNATE, "Blue Silver Grass", Map.of(Affinity.POISON, 3.0, Affinity.BEAST, 2.0)),
    SPIRIT_EYES(Category.BODY, Origin.INNATE, "Spirit Eyes", bodyAffinities()),
    SPIRIT_BODY(Category.BODY, Origin.INNATE, "Spirit Body", bodyAffinities()),
    SPIRIT_LEGS(Category.BODY, Origin.INNATE, "Spirit Legs", bodyAffinities()),
    SPIRIT_WINGS(Category.BODY, Origin.INNATE, "Spirit Wings", bodyAffinities()),
    BLUE_SILVER_EMPEROR(Category.BEAST, Origin.EVOLUTION, "Blue Silver Emperor", Map.of(Affinity.POISON, 4.0, Affinity.BEAST, 2.0)),
    NINE_TREASURE_GLAZED_TILE_PAGODA(Category.TOOL, Origin.EVOLUTION, "Nine Treasure Glazed Tile Pagoda", toolAffinities()),
    BLUE_LIGHTNING_TYRANT_DRAGON(Category.BEAST, Origin.EVOLUTION, "Blue Lightning Tyrant Dragon", Map.of(Affinity.LIGHTNING, 3.0, Affinity.DRAGON, 2.0)),
    GOLDEN_HOLY_DRAGON(Category.BEAST, Origin.EVOLUTION, "Golden Holy Dragon",
            Map.of(Affinity.LIGHTNING, 3.0, Affinity.DRAGON, 2.0, Affinity.LIGHT, 2.0, Affinity.HOLY, 2.0)),
    ABYSS_DEMON_DRAGON_SHARK(Category.BEAST, Origin.EVOLUTION, "Abyss Demon Dragon Shark",
            Map.of(Affinity.ICE, 2.0, Affinity.BEAST, 2.0, Affinity.DEMONIC, 2.0, Affinity.DARK, 2.0)),
    ABYSS_ICE_DEMON_DRAGON(Category.BEAST, Origin.EVOLUTION, "Abyss Ice Demon Dragon",
            Map.of(Affinity.ICE, 3.0, Affinity.BEAST, 2.0, Affinity.DEMONIC, 2.0, Affinity.DARK, 3.0)),
    SKY_BLUE_ICE_DEVOURING_DRAGON(Category.BEAST, Origin.EVOLUTION, "Sky Blue Ice Devouring Dragon",
            Map.of(Affinity.ICE, 4.0, Affinity.BEAST, 2.0, Affinity.DEMONIC, 2.0, Affinity.DARK, 4.0)),
    ICE_JADE_SCORPION_EMPEROR(Category.BEAST, Origin.EVOLUTION, "Ice Jade Scorpion Emperor",
            Map.of(Affinity.INSECT, 2.0, Affinity.POISON, 2.0, Affinity.ICE, 4.0)),
    TEN_HEADED_FIRE_PHOENIX(Category.BEAST, Origin.EVOLUTION, "Ten Headed Fire Phoenix",
            Map.of(Affinity.BIRD, 2.0, Affinity.FIRE, 3.0, Affinity.ICE, 3.0, Affinity.DARK, 3.0,
                    Affinity.HOLY, 3.0, Affinity.LIGHT, 3.0, Affinity.DEMONIC, 3.0)),
    GOLDEN_DRAGON_KING(Category.BEAST, Origin.EVOLUTION, "Golden Dragon King",
            Map.of(Affinity.DRAGON, 3.0, Affinity.HOLY, 3.0, Affinity.LIGHT, 3.0)),
    SILVER_DRAGON_KING(Category.BEAST, Origin.EVOLUTION, "Silver Dragon King",
            Map.of(Affinity.DRAGON, 3.0, Affinity.FIRE, 3.0, Affinity.ICE, 3.0, Affinity.DARK, 3.0, Affinity.LIGHT, 3.0)),
    SERAPHIM(Category.BEAST, Origin.EVOLUTION, "Seraphim", Map.of(Affinity.LIGHT, 4.0, Affinity.HOLY, 4.0, Affinity.BIRD, 2.0)),
    POLYCORIA_EYES(Category.BODY, Origin.EVOLUTION, "Polycoria Eyes", Map.of(Affinity.SPIRIT, 3.0, Affinity.BEAST, 2.0)),
    EYES_OF_ASURA(Category.BODY, Origin.EVOLUTION, "Eyes of Asura", Map.of(Affinity.SPIRIT, 5.0, Affinity.BEAST, 2.0));

    public enum Category {
        TOOL,
        BEAST,
        BODY
    }

    /** Whether a soul can be picked at awakening, or only reached by evolving another one. */
    public enum Origin {
        INNATE,
        EVOLUTION
    }

    private static final int SEVEN_TREASURE_RING_CAP = 7;
    private static final int NINE_TREASURE_RING_CAP = 9;

    private final Category category;
    private final Origin origin;
    private final String displayName;
    private final Map<Affinity, Double> affinityMultipliers;

    public boolean isEvolution() {
        return origin == Origin.EVOLUTION;
    }

    /** Seven/Nine Treasure Glazed Tile Pagoda cap their own ring track below the normal level formula. */
    public OptionalInt ringCapOverride() {
        return switch (this) {
            case SEVEN_TREASURE_GLAZED_TILE_PAGODA -> OptionalInt.of(SEVEN_TREASURE_RING_CAP);
            case NINE_TREASURE_GLAZED_TILE_PAGODA -> OptionalInt.of(NINE_TREASURE_RING_CAP);
            default -> OptionalInt.empty();
        };
    }

    MartialSoul(final Category category, final Origin origin, final String displayName,
                final Map<Affinity, Double> affinityMultipliers) {
        this.category = category;
        this.origin = origin;
        this.displayName = displayName;
        this.affinityMultipliers = affinityMultipliers;
    }

    public Category category() { return category; }
    public String displayName() { return displayName; }
    public Map<Affinity, Double> affinityMultipliers() { return affinityMultipliers; }

    private static Map<Affinity, Double> toolAffinities() {
        return Map.of(
                Affinity.LIGHTNING, 2.0, Affinity.LIGHT, 2.0, Affinity.DARK, 2.0,
                Affinity.ICE, 2.0, Affinity.FIRE, 2.0, Affinity.SPIRIT, 2.0,
                Affinity.HOLY, 2.0, Affinity.DEMONIC, 2.0, Affinity.WIND, 2.0,
                Affinity.POISON, 2.0);
    }

    private static Map<Affinity, Double> beastAffinities(final Affinity... affinities) {
        final Map<Affinity, Double> result = new EnumMap<>(Affinity.class);
        for (final Affinity affinity : affinities) {
            result.put(affinity, 2.0);
        }
        return Map.copyOf(result);
    }

    private static Map<Affinity, Double> bodyAffinities() {
        return Map.of(Affinity.SPIRIT, 2.0, Affinity.BEAST, 2.0);
    }
}
