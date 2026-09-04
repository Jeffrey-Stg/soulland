package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.spirit.Affinity;
import java.util.Map;

public enum MartialSoul {
    CLEAR_SKY_HAMMER(Category.TOOL, "Clear Sky Hammer", toolAffinities()),
    SEVEN_KILL_SWORD(Category.TOOL, "Seven Kill Sword", toolAffinities()),
    NINE_HEART_BEGONIA(Category.TOOL, "Nine Heart Begonia", toolAffinities()),
    SEVEN_TREASURE_GLAZED_TILE_PAGODA(Category.TOOL, "Seven Treasure Glazed Tile Pagoda", toolAffinities()),
    WHITE_TIGER(Category.BEAST, "White Tiger", beastAffinities(Affinity.DARK, Affinity.LIGHT, Affinity.BEAST)),
    HELL_SPIRIT_CAT(Category.BEAST, "Hell Spirit Cat", beastAffinities(Affinity.SPIRIT, Affinity.DARK, Affinity.LIGHT, Affinity.BEAST)),
    HOLY_ANGEL(Category.BEAST, "Holy Angel", Map.of(Affinity.LIGHT, 2.0, Affinity.HOLY, 2.0, Affinity.BIRD, 2.0)),
    GOLDEN_DRAGON(Category.BEAST, "Golden Dragon", beastAffinities(Affinity.DRAGON, Affinity.HOLY, Affinity.LIGHT)),
    SILVER_DRAGON(Category.BEAST, "Silver Dragon", beastAffinities(Affinity.DRAGON, Affinity.FIRE, Affinity.ICE, Affinity.DARK, Affinity.LIGHT)),
    ICE_JADE_SCORPION(Category.BEAST, "Ice Jade Scorpion", beastAffinities(Affinity.INSECT, Affinity.POISON, Affinity.ICE)),
    BLUE_LIGHTNING_DRAGON(Category.BEAST, "Blue Lightning Dragon", beastAffinities(Affinity.LIGHTNING, Affinity.DRAGON)),
    PHOENIX(Category.BEAST, "Phoenix", beastAffinities(Affinity.BIRD, Affinity.FIRE)),
    DEMON_SOUL_GREAT_WHITE_SHARK(Category.BEAST, "Demon Soul Great White Shark", beastAffinities(Affinity.ICE, Affinity.BEAST, Affinity.DEMONIC)),
    BLUE_SILVER_GRASS(Category.BEAST, "Blue Silver Grass", Map.of(Affinity.POISON, 3.0, Affinity.BEAST, 2.0)),
    SPIRIT_EYES(Category.BODY, "Spirit Eyes", bodyAffinities()),
    SPIRIT_BODY(Category.BODY, "Spirit Body", bodyAffinities()),
    SPIRIT_LEGS(Category.BODY, "Spirit Legs", bodyAffinities()),
    SPIRIT_WINGS(Category.BODY, "Spirit Wings", bodyAffinities()),
    BLUE_SILVER_EMPEROR(Category.BEAST, "Blue Silver Emperor", Map.of(Affinity.POISON, 4.0, Affinity.BEAST, 2.0)),
    NINE_TREASURE_GLAZED_TILE_PAGODA(Category.TOOL, "Nine Treasure Glazed Tile Pagoda", toolAffinities()),
    BLUE_LIGHTNING_TYRANT_DRAGON(Category.BEAST, "Blue Lightning Tyrant Dragon", Map.of(Affinity.LIGHTNING, 3.0, Affinity.DRAGON, 2.0)),
    ABYSS_DEMON_DRAGON_SHARK(Category.BEAST, "Abyss Demon Dragon Shark", Map.of(Affinity.ICE, 2.0, Affinity.BEAST, 2.0, Affinity.DEMONIC, 2.0, Affinity.DARK, 3.0)),
    ABYSS_ICE_DEMON_DRAGON(Category.BEAST, "Abyss Ice Demon Dragon", Map.of(Affinity.ICE, 3.0, Affinity.BEAST, 2.0, Affinity.DEMONIC, 2.0, Affinity.DARK, 3.0)),
    SKY_BLUE_ICE_DEVOURING_DRAGON(Category.BEAST, "Sky Blue Ice Devouring Dragon", Map.of(Affinity.ICE, 4.0, Affinity.BEAST, 2.0, Affinity.DEMONIC, 2.0, Affinity.DARK, 4.0)),
    ICE_JADE_SCORPION_EMPEROR(Category.BEAST, "Ice Jade Scorpion Emperor", Map.of(Affinity.INSECT, 2.0, Affinity.POISON, 2.0, Affinity.ICE, 4.0)),
    TEN_HEADED_FIRE_PHOENIX(Category.BEAST, "Ten Headed Fire Phoenix", Map.of(Affinity.BIRD, 2.0, Affinity.FIRE, 3.0, Affinity.ICE, 2.0, Affinity.DARK, 2.0, Affinity.HOLY, 2.0, Affinity.LIGHT, 2.0, Affinity.DEMONIC, 2.0)),
    GOLDEN_DRAGON_KING(Category.BEAST, "Golden Dragon King", Map.of(Affinity.DRAGON, 3.0, Affinity.HOLY, 3.0, Affinity.LIGHT, 3.0)),
    SILVER_DRAGON_KING(Category.BEAST, "Silver Dragon King", Map.of(Affinity.DRAGON, 3.0, Affinity.FIRE, 3.0, Affinity.ICE, 3.0, Affinity.DARK, 3.0, Affinity.LIGHT, 3.0)),
    SERAPHIM(Category.BEAST, "Seraphim", Map.of(Affinity.LIGHT, 4.0, Affinity.HOLY, 4.0, Affinity.BIRD, 2.0)),
    POLYCORIA_EYES(Category.BODY, "Polycoria Eyes", Map.of(Affinity.SPIRIT, 3.0, Affinity.BEAST, 2.0)),
    EYES_OF_ASURA(Category.BODY, "Eyes of Asura", Map.of(Affinity.SPIRIT, 5.0, Affinity.BEAST, 2.0));

    public enum Category {
        TOOL,
        BEAST,
        BODY
    }

    private final Category category;
    private final String displayName;
    private final Map<Affinity, Double> affinityMultipliers;

    public boolean isEvolution() {
        return switch (this) {
            case BLUE_SILVER_GRASS, SEVEN_TREASURE_GLAZED_TILE_PAGODA, BLUE_LIGHTNING_DRAGON,
                    DEMON_SOUL_GREAT_WHITE_SHARK, ICE_JADE_SCORPION, PHOENIX, GOLDEN_DRAGON,
                    SILVER_DRAGON, HOLY_ANGEL, SPIRIT_EYES -> false;
            default -> this == BLUE_SILVER_EMPEROR || this == NINE_TREASURE_GLAZED_TILE_PAGODA
                    || this == BLUE_LIGHTNING_TYRANT_DRAGON || this == ABYSS_DEMON_DRAGON_SHARK
                    || this == ABYSS_ICE_DEMON_DRAGON || this == SKY_BLUE_ICE_DEVOURING_DRAGON
                    || this == ICE_JADE_SCORPION_EMPEROR || this == TEN_HEADED_FIRE_PHOENIX
                    || this == GOLDEN_DRAGON_KING || this == SILVER_DRAGON_KING || this == SERAPHIM
                    || this == POLYCORIA_EYES || this == EYES_OF_ASURA;
        };
    }

    MartialSoul(final Category category, final String displayName,
                final Map<Affinity, Double> affinityMultipliers) {
        this.category = category;
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
        final Map<Affinity, Double> result = new java.util.EnumMap<>(Affinity.class);
        for (final Affinity affinity : affinities) {
            result.put(affinity, 2.0);
        }
        return Map.copyOf(result);
    }

    private static Map<Affinity, Double> bodyAffinities() {
        return Map.of(Affinity.SPIRIT, 2.0, Affinity.BEAST, 2.0);
    }
}
