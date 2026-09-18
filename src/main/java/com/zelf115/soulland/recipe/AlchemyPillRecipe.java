package com.zelf115.soulland.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * A pill recipe crafted through the alchemy menu rather than a crafting grid.
 * <p>
 * A crafting grid has 9 slots, which cannot express costs like "64 herbs" - so this
 * recipe type is never matched against a grid. It exists purely to be data-driven and
 * datapack-reloadable; {@link com.zelf115.soulland.menu.AlchemyMenu} queries and validates
 * it directly instead of going through the vanilla recipe-matching pipeline.
 */
public record AlchemyPillRecipe(List<CostEntry> ingredients, Result result, int minLevel, int maxLevel)
        implements Recipe<AlchemyPillRecipe.Input> {

    private static final int MINIMUM_COST = 1;

    public static final MapCodec<AlchemyPillRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CostEntry.CODEC.codec().listOf().fieldOf("ingredients").forGetter(AlchemyPillRecipe::ingredients),
            Result.CODEC.fieldOf("result").forGetter(AlchemyPillRecipe::result),
            Codec.INT.optionalFieldOf("min_level", 1).forGetter(AlchemyPillRecipe::minLevel),
            Codec.INT.optionalFieldOf("max_level", Integer.MAX_VALUE).forGetter(AlchemyPillRecipe::maxLevel)
    ).apply(instance, AlchemyPillRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyPillRecipe> STREAM_CODEC = StreamCodec.composite(
            CostEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), AlchemyPillRecipe::ingredients,
            Result.STREAM_CODEC, AlchemyPillRecipe::result,
            ByteBufCodecs.VAR_INT, AlchemyPillRecipe::minLevel,
            ByteBufCodecs.VAR_INT, AlchemyPillRecipe::maxLevel,
            AlchemyPillRecipe::new);

    public boolean hasIngredients(final Player player, final double costMultiplier) {
        for (final CostEntry entry : ingredients) {
            if (countMatching(player, entry.ingredient()) < scaledCount(entry.count(), costMultiplier)) {
                return false;
            }
        }
        return true;
    }

    public void consumeIngredients(final Player player, final double costMultiplier) {
        for (final CostEntry entry : ingredients) {
            consumeMatching(player, entry.ingredient(), scaledCount(entry.count(), costMultiplier));
        }
    }

    /** How many of one ingredient the player is carrying, for the menu to show against its cost. */
    public static int carriedCount(final Player player, final CostEntry entry) {
        return countMatching(player, entry.ingredient());
    }

    /** What one ingredient actually costs at this furnace tier, for the menu to show. */
    public static int costFor(final CostEntry entry, final double costMultiplier) {
        return scaledCount(entry.count(), costMultiplier);
    }

    /**
     * A discount may never round an ingredient away: a furnace that cuts costs by a fifth would
     * otherwise ask for none of anything a recipe wants a single one of, and hand the pill over free.
     */
    private static int scaledCount(final int baseCount, final double costMultiplier) {
        if (baseCount <= 0) {
            return 0;
        }
        return Math.max(MINIMUM_COST, (int) Math.floor(baseCount * costMultiplier));
    }

    private static int countMatching(final Player player, final Ingredient ingredient) {
        int found = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            final ItemStack stack = player.getInventory().getItem(slot);
            if (ingredient.test(stack)) {
                found += stack.getCount();
            }
        }
        return found;
    }

    private static void consumeMatching(final Player player, final Ingredient ingredient, final int amount) {
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            final ItemStack stack = player.getInventory().getItem(slot);
            if (!ingredient.test(stack)) {
                continue;
            }
            final int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
        }
    }

    // Recipe<Input> plumbing below is never exercised by vanilla crafting - this recipe type
    // has no grid shape and is only ever looked up and validated directly, above.

    @Override
    public boolean matches(final Input input, final Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(final Input input, final HolderLookup.Provider registries) {
        return getResultItem(registries);
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(final HolderLookup.Provider registries) {
        return new ItemStack(result.item(), result.count());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SoulLandRecipes.ALCHEMY_PILL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return SoulLandRecipes.ALCHEMY_PILL_TYPE.get();
    }

    public record CostEntry(Ingredient ingredient, int count) {
        private static final int DEFAULT_COUNT = 1;

        public static final MapCodec<CostEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(CostEntry::ingredient),
                Codec.INT.optionalFieldOf("count", DEFAULT_COUNT).forGetter(CostEntry::count)
        ).apply(instance, CostEntry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CostEntry> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, CostEntry::ingredient,
                ByteBufCodecs.VAR_INT, CostEntry::count,
                CostEntry::new);
    }

    public record Result(Item item, int count) {
        private static final int DEFAULT_COUNT = 1;

        public static final MapCodec<Result> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(Result::item),
                Codec.INT.optionalFieldOf("count", DEFAULT_COUNT).forGetter(Result::count)
        ).apply(instance, Result::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Result> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.ITEM), Result::item,
                ByteBufCodecs.VAR_INT, Result::count,
                Result::new);
    }

    /**
     * This recipe type is never matched against a real inventory, so its input carries no slots.
     */
    public record Input() implements RecipeInput {
        @Override
        public ItemStack getItem(final int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return 0;
        }
    }
}
