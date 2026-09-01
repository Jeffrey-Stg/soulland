package com.zelf115.soulland;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
public class Stats {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, SoulLand.MODID);

    //region Health
    public static final Holder<Attribute> HEALTH = ATTRIBUTES.register(
            "health",
            () -> new RangedAttribute(
                    String.format("attribute.%s.health",SoulLand.MODID), // Translation key
                    0.0,                          // Default value
                    0.0,                          // Minimum value
                    Integer.MAX_VALUE             // Maximum value
            ).setSyncable(true)                   // Sync to client if needed
    );
    public static double getHealth(Player player) {
        AttributeInstance instance = player.getAttribute(HEALTH);
        return instance != null ? instance.getValue() : 100.0;
    }

    public static void AddHealth(Player player, double amount) {
        if (player.level().isClientSide()) return; // Always mutate on server

        AttributeInstance instance = player.getAttribute(HEALTH);
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() + amount);
        }
    }
    //endregion

    //region Defense
    public static final Holder<Attribute> DEFENSE = ATTRIBUTES.register(
            "defense",
            () -> new RangedAttribute(
                    String.format("attribute.%s.defense",SoulLand.MODID), // Translation key
                    0.0,                          // Default value
                    0.0,                          // Minimum value
                    Integer.MAX_VALUE             // Maximum value
            ).setSyncable(true)                   // Sync to client if needed
    );
    public static double getDefense(Player player) {
        AttributeInstance instance = player.getAttribute(DEFENSE);
        return instance != null ? instance.getValue() : 100.0;
    }

    public static void AddDefense(Player player, double amount) {
        if (player.level().isClientSide()) return; // Always mutate on server

        AttributeInstance instance = player.getAttribute(DEFENSE);
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() + amount);
        }
    }
    //endregion

    //region Damage
    public static final Holder<Attribute> DAMAGE = ATTRIBUTES.register(
            "damage",
            () -> new RangedAttribute(
                    String.format("attribute.%s.damage",SoulLand.MODID), // Translation key
                    0.0,                          // Default value
                    0.0,                          // Minimum value
                    Integer.MAX_VALUE             // Maximum value
            ).setSyncable(true)                   // Sync to client if needed
    );
    public static double getDamage(Player player) {
        AttributeInstance instance = player.getAttribute(DAMAGE);
        return instance != null ? instance.getValue() : 100.0;
    }

    public static void addDamage(Player player, double amount) {
        if (player.level().isClientSide()) return; // Always mutate on server

        AttributeInstance instance = player.getAttribute(DAMAGE);
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() + amount);
        }
    }
    //endregion

    //region Speed
    public static final Holder<Attribute> SPEED = ATTRIBUTES.register(
            "speed",
            () -> new RangedAttribute(
                    String.format("attribute.%s.speed",SoulLand.MODID), // Translation key
                    0.0,                          // Default value
                    0.0,                          // Minimum value
                    Integer.MAX_VALUE             // Maximum value
            ).setSyncable(true)                   // Sync to client if needed
    );
    public static double getSpeed(Player player) {
        AttributeInstance instance = player.getAttribute(SPEED);
        return instance != null ? instance.getValue() : 100.0;
    }

    public static void addSpeed(Player player, double amount) {
        if (player.level().isClientSide()) return; // Always mutate on server

        AttributeInstance instance = player.getAttribute(SPEED);
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() + amount);
        }
    }
    //endregion

    //region Spirit
    public static final Holder<Attribute> SPIRIT = ATTRIBUTES.register(
            "spirit",
            () -> new RangedAttribute(
                    String.format("attribute.%s.spirit",SoulLand.MODID), // Translation key
                    0.0,                          // Default value
                    0.0,                          // Minimum value
                    Integer.MAX_VALUE             // Maximum value
            ).setSyncable(true)                   // Sync to client if needed
    );
    public static double getSpirit(Player player) {
        AttributeInstance instance = player.getAttribute(SPIRIT);
        return instance != null ? instance.getValue() : 100.0;
    }
    public static void addSpirit(Player player, double amount) {
        if (player.level().isClientSide()) return;
        AttributeInstance instance = player.getAttribute(SPIRIT);
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() + amount);
        }
    }
    //endregion

    //region Cultivation_Speed
    public static final Holder<Attribute> CULTIVATION_SPEED = ATTRIBUTES.register(
            "cultivation_speed",
            () -> new RangedAttribute(
                    String.format("attribute.%s.cultivation_speed",SoulLand.MODID), // Translation key
                    0.0,                          // Default value
                    0.0,                          // Minimum value
                    Integer.MAX_VALUE             // Maximum value
            ).setSyncable(true)                   // Sync to client if needed
    );

    public static double getCultivationSpeed(Player player) {
        AttributeInstance instance = player.getAttribute(CULTIVATION_SPEED);
        return instance != null ? instance.getValue() : 100.0;
    }
    public static void addCultivationSpeed(Player player, double amount) {
        if (player.level().isClientSide()) return;
        AttributeInstance instance = player.getAttribute(CULTIVATION_SPEED);
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() + amount);
        }
    }
    //endregion

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
    }


}
