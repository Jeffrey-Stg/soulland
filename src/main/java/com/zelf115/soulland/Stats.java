package com.zelf115.soulland;

import com.zelf115.soulland.cultivation.CultivationData;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
public class Stats {
    private static final double DEFAULT_PLAYER_MAX_HEALTH = 20.0;
    private static final double DEFAULT_PLAYER_ATTACK_DAMAGE = 1.0;
    private static final double DEFAULT_PLAYER_MOVEMENT_SPEED = 0.1;
    private static final double DEFAULT_PLAYER_ATTACK_SPEED = 4.0;

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
        final AttributeInstance instance = player.getAttribute(HEALTH);
        return instance != null ? instance.getValue() : 100.0;
    }

    public static void addHealth(Player player, double amount) {
        if (player.level().isClientSide()) return; // Always mutate on server

        final AttributeInstance instance = player.getAttribute(HEALTH);
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
        final AttributeInstance instance = player.getAttribute(DEFENSE);
        return instance != null ? instance.getValue() : 100.0;
    }

    public static void addDefense(Player player, double amount) {
        if (player.level().isClientSide()) return; // Always mutate on server

        final AttributeInstance instance = player.getAttribute(DEFENSE);
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
        final AttributeInstance instance = player.getAttribute(DAMAGE);
        return instance != null ? instance.getValue() : 100.0;
    }

    public static void addDamage(Player player, double amount) {
        if (player.level().isClientSide()) return; // Always mutate on server

        final AttributeInstance instance = player.getAttribute(DAMAGE);
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
        final AttributeInstance instance = player.getAttribute(SPEED);
        return instance != null ? instance.getValue() : 100.0;
    }

    public static void addSpeed(Player player, double amount) {
        if (player.level().isClientSide()) return; // Always mutate on server

        final AttributeInstance instance = player.getAttribute(SPEED);
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
        final AttributeInstance instance = player.getAttribute(SPIRIT);
        return instance != null ? instance.getValue() : 100.0;
    }
    public static void addSpirit(Player player, double amount) {
        if (player.level().isClientSide()) return;
        final AttributeInstance instance = player.getAttribute(SPIRIT);
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
        final AttributeInstance instance = player.getAttribute(CULTIVATION_SPEED);
        return instance != null ? instance.getValue() : 100.0;
    }
    public static void addCultivationSpeed(Player player, double amount) {
        if (player.level().isClientSide()) return;
        final AttributeInstance instance = player.getAttribute(CULTIVATION_SPEED);
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() + amount);
        }
    }
    //endregion

    public static double getMaxSpiritEnergy(final Player player) {
        return Math.max(0.0, getSpirit(player) / 10.0);
    }

    public static double getSpiritEnergyRegenPerSecond(final Player player) {
        return getMaxSpiritEnergy(player) * (getSpirit(player) / 10000.0);
    }

    public static void syncDerivedPlayerStats(final Player player, final CultivationData data) {
        if (player.level().isClientSide()) {
            return;
        }

        final double healthStat = getHealth(player);
        final double damageStat = getDamage(player);
        final double defenseStat = getDefense(player);
        final double speedStat = getSpeed(player);
        final double maxHealth = (DEFAULT_PLAYER_MAX_HEALTH + healthStat / 20.0) * (1.0 + healthStat / 10000.0);
        final double movementSpeed = DEFAULT_PLAYER_MOVEMENT_SPEED * (1.0 + speedStat / 2500.0) * (data.getMovementUsagePercent() / 100.0);
        final double attackSpeed = DEFAULT_PLAYER_ATTACK_SPEED * (1.0 + speedStat / 10000.0);
        setVanillaBaseValue(player, Attributes.MAX_HEALTH, maxHealth);
        setVanillaBaseValue(player, Attributes.ATTACK_DAMAGE, DEFAULT_PLAYER_ATTACK_DAMAGE + damageStat / 20.0);
        setVanillaBaseValue(player, Attributes.ARMOR, defenseStat / 50.0);
        setVanillaBaseValue(player, Attributes.MOVEMENT_SPEED, movementSpeed);
        setVanillaBaseValue(player, Attributes.ATTACK_SPEED, attackSpeed);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }

        final double maxSpiritEnergy = getMaxSpiritEnergy(player);
        if (data.getSpiritEnergy() < 0.0 || data.getSpiritEnergy() > maxSpiritEnergy) {
            data.setSpiritEnergy(maxSpiritEnergy);
        }
    }

    public static double applyOutgoingDamageBonus(final double baseDamage, final double damageStat) {
        final double multiplicativeBonus = 1.0 + damageStat / 10000.0;
        return baseDamage * multiplicativeBonus;
    }

    public static float applyDefenseReduction(final float incomingDamage, final double defenseStat) {
        final double reduction = Math.min(0.90, defenseStat / 10000.0);
        return (float) (incomingDamage * (1.0 - reduction));
    }

    private static void setVanillaBaseValue(final Player player, final Holder<Attribute> attribute, final double value) {
        final AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
    }


}
