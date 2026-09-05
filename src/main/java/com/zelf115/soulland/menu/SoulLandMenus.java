package com.zelf115.soulland.menu;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SoulLandMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, SoulLand.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<AlchemyMenu>> ALCHEMY =
            MENU_TYPES.register("alchemy", () -> IMenuTypeExtension.create(AlchemyMenu::new));

    private SoulLandMenus() {
    }

    public static void register(final IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
