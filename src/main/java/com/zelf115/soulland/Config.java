package com.zelf115.soulland;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    public static final ModConfigSpec.IntValue SPIRIT_BEAST_MAX_EFFECTIVE_LEVEL = BUILDER
            .comment("Highest level a spirit beast derives from its age. Its stats scale with this,",
                    "so raising it makes ancient beasts sharply deadlier; tune by play-testing.")
            .defineInRange("spiritBeast.maxEffectiveLevel", 1000, 1, 100_000);

    public static final ModConfigSpec.IntValue SPIRIT_BEAST_YEARS_PER_LEVEL = BUILDER
            .comment("Years of age worth one level of spirit beast stats.")
            .defineInRange("spiritBeast.yearsPerLevel", 100, 1, 100_000);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
