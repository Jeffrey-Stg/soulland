package com.zelf115.soulland.menu;

import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.item.PillFurnaceItem;
import com.zelf115.soulland.recipe.AlchemyPillRecipe;
import com.zelf115.soulland.recipe.SoulLandRecipes;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * The alchemy furnace's menu. It has no real slots - pills are bought straight out of the
 * player's inventory by clicking a recipe row, so there is nothing to place in a grid.
 */
public final class AlchemyMenu extends AbstractContainerMenu {
    private final PillFurnaceItem.Tier tier;
    private final List<RecipeHolder<AlchemyPillRecipe>> recipes;

    public AlchemyMenu(final int containerId, final Inventory playerInventory, final PillFurnaceItem.Tier tier) {
        super(SoulLandMenus.ALCHEMY.get(), containerId);
        this.tier = tier;
        this.recipes = recipesFor(playerInventory.player);
    }

    public AlchemyMenu(final int containerId, final Inventory playerInventory, final RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readEnum(PillFurnaceItem.Tier.class));
    }

    public static void open(final ServerPlayer player, final PillFurnaceItem.Tier tier) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, opener) -> new AlchemyMenu(containerId, inventory, tier),
                Component.translatable("container.soulland.alchemy")),
                buf -> buf.writeEnum(tier));
    }

    private static List<RecipeHolder<AlchemyPillRecipe>> recipesFor(final Player player) {
        return player.level().getRecipeManager().getAllRecipesFor(SoulLandRecipes.ALCHEMY_PILL_TYPE.get());
    }

    public PillFurnaceItem.Tier tier() {
        return tier;
    }

    public List<RecipeHolder<AlchemyPillRecipe>> recipes() {
        return recipes;
    }

    @Override
    public boolean clickMenuButton(final Player player, final int id) {
        if (id < 0 || id >= recipes.size()) {
            return false;
        }
        if (!player.level().isClientSide()) {
            craftPill(player, recipes.get(id).value());
        }
        return true;
    }

    private void craftPill(final Player player, final AlchemyPillRecipe recipe) {
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        if (!recipe.allowsLevel(data.getLevel())) {
            return;
        }

        final double costMultiplier = tier.costMultiplier();
        if (!recipe.hasIngredients(player, costMultiplier)) {
            return;
        }

        recipe.consumeIngredients(player, costMultiplier);
        final ItemStack result = recipe.getResultItem(player.level().registryAccess());
        result.setCount(result.getCount() + tier.bonusPillCount());
        if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final Player player) {
        return !player.isRemoved();
    }
}
