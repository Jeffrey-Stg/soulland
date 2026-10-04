package com.zelf115.soulland.events;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.MartialSoulAbility;
import com.zelf115.soulland.item.MartialSoulSwordItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * A martial soul's tool belongs to its owner alone: one placed in any container is taken back and
 * the soul put away, the same as dropping it.
 */
@EventBusSubscriber(modid = SoulLand.MODID)
public final class SoulToolContainerEvents {

    private SoulToolContainerEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        final Player player = event.getEntity();
        if (player.level().isClientSide() || player.containerMenu == player.inventoryMenu) return;
        if (removeSoulToolsFromContainers(player.containerMenu)) {
            MartialSoulAbility.forceDeactivate(player, player.getData(CultivationAttachment.CULTIVATION_DATA.get()));
        }
    }

    /** Whether any soul tool was found outside a player inventory. */
    private static boolean removeSoulToolsFromContainers(final AbstractContainerMenu menu) {
        boolean removedAny = false;
        for (final Slot slot : menu.slots) {
            if (isSoulToolInContainer(slot)) {
                slot.set(ItemStack.EMPTY);
                removedAny = true;
            }
        }
        return removedAny;
    }

    private static boolean isSoulToolInContainer(final Slot slot) {
        return !(slot.container instanceof Inventory) && slot.getItem().getItem() instanceof MartialSoulSwordItem;
    }
}
