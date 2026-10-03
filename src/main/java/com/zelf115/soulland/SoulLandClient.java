package com.zelf115.soulland;

import com.zelf115.soulland.client.AlchemyScreen;
import com.zelf115.soulland.fluid.SoulLandFluids;
import com.zelf115.soulland.cultivation.SoulRingAbsorption;
import com.zelf115.soulland.menu.SoulLandMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@Mod(value = SoulLand.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = SoulLand.MODID, value = Dist.CLIENT)
public class SoulLandClient {
    public SoulLandClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        SoulLand.LOGGER.info("HELLO FROM CLIENT SETUP");
        SoulLand.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        event.enqueueWork(() -> ItemProperties.register(SoulLand.SOUL_RING_ITEM.get(),
                ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "soul_ring_years"),
                (ItemStack stack, net.minecraft.client.multiplayer.ClientLevel level,
                 net.minecraft.world.entity.LivingEntity entity, int seed) -> {
                    final var tag = SoulRingAbsorption.ringData(stack);
                    return tag == null ? 0.0F : tag.getInt("Years");
                }));
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(SoulLandMenus.ALCHEMY.get(), AlchemyScreen::new);
    }

    @SubscribeEvent
    static void registerFluidClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_still");
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_flow");
            }

            @Override
            public int getTintColor() {
                return 0xFFFF2020;
            }
        }, SoulLandFluids.RED_WATER_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_still");
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_flow");
            }

            @Override
            public int getTintColor() {
                return 0xFF20E0E0;
            }
        }, SoulLandFluids.CYAN_WATER_TYPE.get());
    }
}
