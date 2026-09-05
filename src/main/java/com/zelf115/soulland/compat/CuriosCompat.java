package com.zelf115.soulland.compat;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.item.PillFurnaceItem;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * The only class in this mod allowed to reference Curios API types. Every call into it is
 * guarded by {@link #isLoaded()} first, so the JVM never has to resolve those types when
 * Curios isn't installed - it only verifies this class's bytecode the first time one of its
 * methods actually runs.
 */
public final class CuriosCompat {
    private static final String CURIOS_MODID = "curios";

    private CuriosCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(CURIOS_MODID);
    }

    public static void registerCurios(final RegisterCapabilitiesEvent event) {
        CuriosApi.registerCurio(SoulLand.PILL_FURNACE.get(), new ICurioItem() {
        });
        CuriosApi.registerCurio(SoulLand.ENCHANTED_PILL_FURNACE.get(), new ICurioItem() {
        });
        CuriosApi.registerCurio(SoulLand.NETHER_PILL_FURNACE.get(), new ICurioItem() {
        });
        CuriosApi.registerCurio(SoulLand.STAR_PILL_FURNACE.get(), new ICurioItem() {
        });
        CuriosApi.registerCurio(SoulLand.DIVINE_PILL_FURNACE.get(), new ICurioItem() {
        });
    }

    public static Optional<ItemStack> findEquippedPillFurnace(final Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inventory -> inventory.findFirstCurio(stack -> stack.getItem() instanceof PillFurnaceItem))
                .map(slotResult -> slotResult.stack());
    }
}
