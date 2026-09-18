package com.zelf115.soulland.menu;

import com.zelf115.soulland.item.PillFurnaceItem;
import com.zelf115.soulland.recipe.AlchemyPillRecipe;
import com.zelf115.soulland.recipe.SoulLandRecipes;
import java.util.Comparator;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * The alchemy furnace's menu. It shows no slots - pills are bought straight out of the player's
 * inventory by clicking a recipe row, so there is nothing to place in a grid.
 */
public final class AlchemyMenu extends AbstractContainerMenu {
    private static final float BREW_VOLUME = 0.7F;
    private static final float BREW_PITCH = 1.2F;

    private final PillFurnaceItem.Tier tier;
    private final List<RecipeHolder<AlchemyPillRecipe>> recipes;

    public AlchemyMenu(final int containerId, final Inventory playerInventory, final PillFurnaceItem.Tier tier) {
        super(SoulLandMenus.ALCHEMY.get(), containerId);
        this.tier = tier;
        this.recipes = recipesFor(playerInventory.player);
        mirrorPlayerInventory(playerInventory);
    }

    public AlchemyMenu(final int containerId, final Inventory playerInventory, final RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readEnum(PillFurnaceItem.Tier.class));
    }

    /**
     * The menu shows no slots, but the server only replays inventory changes for the slots the open
     * menu holds. Without these the client's copy of the inventory freezes the moment the furnace
     * opens, and the ingredient counts stop moving as the player picks things up or brews. They sit
     * off-screen and refuse every interaction, so they exist purely to keep the two sides in step.
     */
    private void mirrorPlayerInventory(final Inventory playerInventory) {
        for (int slot = 0; slot < playerInventory.getContainerSize(); slot++) {
            addSlot(new InertSlot(playerInventory, slot));
        }
    }

    public static void open(final ServerPlayer player, final PillFurnaceItem.Tier tier) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, opener) -> new AlchemyMenu(containerId, inventory, tier),
                Component.translatable("container.soulland.alchemy")),
                buf -> buf.writeEnum(tier));
    }

    /**
     * The recipe manager hands recipes back in load order, which is effectively arbitrary, so the
     * menu sorts by pill name: that keeps a numbered family such as the Qi Gathering pills together
     * and in sequence.
     */
    private static List<RecipeHolder<AlchemyPillRecipe>> recipesFor(final Player player) {
        return player.level().getRecipeManager().getAllRecipesFor(SoulLandRecipes.ALCHEMY_PILL_TYPE.get())
                .stream()
                .sorted(Comparator.comparing(holder -> holder.value().result().item().getDescriptionId()))
                .toList();
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

    /**
     * Brews one pill, telling the player why whenever nothing comes of the click.
     *
     * <p>A recipe's level band says which cultivators the pill will do anything for once eaten,
     * not who is allowed to brew it: an alchemist may stock pills they have long outgrown.
     */
    private void craftPill(final Player player, final AlchemyPillRecipe recipe) {
        final double costMultiplier = tier.costMultiplier();
        if (!recipe.hasIngredients(player, costMultiplier)) {
            player.sendSystemMessage(Component.translatable("soulland.alchemy.missing_ingredients"));
            return;
        }

        recipe.consumeIngredients(player, costMultiplier);
        final ItemStack result = recipe.getResultItem(player.level().registryAccess());
        result.setCount(result.getCount() + tier.bonusPillCount());
        announceBrew(player, result);
        if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }
    }

    /** Names the pill before it is handed over: adding it to the inventory empties the stack. */
    private static void announceBrew(final Player player, final ItemStack result) {
        player.sendSystemMessage(Component.translatable("soulland.alchemy.brewed",
                result.getCount(), result.getHoverName()));
        player.level().playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW,
                SoundSource.PLAYERS, BREW_VOLUME, BREW_PITCH);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final Player player) {
        return !player.isRemoved();
    }

    /** A mirrored inventory slot: drawn nowhere, takes nothing, gives nothing. */
    private static final class InertSlot extends Slot {
        /** Far enough outside the screen that no click can ever land on it. */
        private static final int OFF_SCREEN = -2000;

        private InertSlot(final Inventory inventory, final int slot) {
            super(inventory, slot, OFF_SCREEN, OFF_SCREEN);
        }

        @Override
        public boolean mayPickup(final Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(final ItemStack stack) {
            return false;
        }

        @Override
        public boolean isActive() {
            return false;
        }
    }
}
