package com.zelf115.soulland;

import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.skill.PassiveSkills;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Stats {
    private static final double DEFAULT_PLAYER_MAX_HEALTH = 20.0;
    private static final double DEFAULT_PLAYER_ATTACK_DAMAGE = 1.0;
    private static final double DEFAULT_PLAYER_MOVEMENT_SPEED = 0.1;
    private static final double DEFAULT_PLAYER_ATTACK_SPEED = 4.0;
    private static final double DEFAULT_PLAYER_SWIM_SPEED = 1.0;
    private static final double DEFAULT_PLAYER_ARMOR = 0.0;
    private static final double PERCENT = 100.0;
    private static final double PLAYER_MAX_HEALTH_CAP = 1_000_000.0;
    private static final ResourceLocation REBIRTH_BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "rebirth_bonus");

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
        return valueOf(player, HEALTH);
    }

    public static void addHealth(Player player, double amount) {
        addToBaseValue(player, HEALTH, amount);
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
        return valueOf(player, DEFENSE);
    }

    public static void addDefense(Player player, double amount) {
        addToBaseValue(player, DEFENSE, amount);
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
        return valueOf(player, DAMAGE);
    }

    public static void addDamage(Player player, double amount) {
        addToBaseValue(player, DAMAGE, amount);
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
        return valueOf(player, SPEED);
    }

    public static void addSpeed(Player player, double amount) {
        addToBaseValue(player, SPEED, amount);
    }
    //endregion

    //region Spirit
    public static final Holder<Attribute> SPIRIT = ATTRIBUTES.register(
            "spirit",
            () -> new RangedAttribute(
                    String.format("attribute.%s.spirit",SoulLand.MODID), // Translation key
                    100.0,                          // Default value
                    0.0,                          // Minimum value
                    Integer.MAX_VALUE             // Maximum value
            ).setSyncable(true)                   // Sync to client if needed
    );
    public static double getSpirit(Player player) {
        return valueOf(player, SPIRIT);
    }
    public static void addSpirit(Player player, double amount) {
        addToBaseValue(player, SPIRIT, amount);
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
        return valueOf(player, CULTIVATION_SPEED);
    }
    public static void addCultivationSpeed(Player player, double amount) {
        addToBaseValue(player, CULTIVATION_SPEED, amount);
    }
    //endregion

    /** Every attribute this mod owns. */
    public static final List<Holder<Attribute>> ALL =
            List.of(DAMAGE, HEALTH, DEFENSE, SPEED, SPIRIT, CULTIVATION_SPEED);

    /** The five stats cultivation levels raise. */
    public static final List<Holder<Attribute>> CULTIVATION_STATS =
            List.of(DAMAGE, HEALTH, DEFENSE, SPEED, SPIRIT);

    /**
     * Raises every cultivation stat by the same amount.
     *
     * <p>Only base values move: what a soul ring or spirit bone lends the player is a modifier, and
     * levelling must not absorb it into the stats the player earned themselves.
     */
    public static void addToCultivationStats(final Player player, final double amount) {
        for (final Holder<Attribute> attribute : CULTIVATION_STATS) {
            addToBaseValue(player, attribute, amount);
        }
    }

    /**
     * Vanilla stops max health at 1024, which cultivators pass long before their Health stat stops
     * growing; past it every extra point of Health would silently do nothing.
     */
    public static void liftMaxHealthCap() {
        ((RangedAttribute) Attributes.MAX_HEALTH.value()).maxValue = PLAYER_MAX_HEALTH_CAP;
    }

    public static double getMaxSpiritEnergy(final Player player) {
        return DerivedStats.maxSpiritEnergy(getSpirit(player));
    }

    public static double getSpiritEnergyRegenPerSecond(final Player player) {
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        return DerivedStats.spiritEnergyRegenPerSecond(getSpirit(player)) * PassiveSkills.spiritRegenMultiplier(data);
    }

    public static void syncDerivedPlayerStats(final Player player, final CultivationData data) {
        if (player.level().isClientSide()) {
            return;
        }

        final double healthStat = getHealth(player);
        final double damageStat = getDamage(player);
        final double defenseStat = getDefense(player);
        final double speedStat = getSpeed(player);
        final double usedSpeedStat = speedStat * data.getMovementUsagePercent() / PERCENT;
        setVanillaBaseValue(player, Attributes.MAX_HEALTH, DerivedStats.maxHealth(DEFAULT_PLAYER_MAX_HEALTH, healthStat));
        setVanillaBaseValue(player, Attributes.ATTACK_DAMAGE, DerivedStats.attackDamage(DEFAULT_PLAYER_ATTACK_DAMAGE, damageStat));
        setVanillaBaseValue(player, Attributes.ARMOR, DerivedStats.armor(DEFAULT_PLAYER_ARMOR, defenseStat));
        setVanillaBaseValue(player, Attributes.MOVEMENT_SPEED,
                DerivedStats.movementSpeed(DEFAULT_PLAYER_MOVEMENT_SPEED, usedSpeedStat));
        setVanillaBaseValue(player, NeoForgeMod.SWIM_SPEED,
                DerivedStats.swimSpeed(DEFAULT_PLAYER_SWIM_SPEED, usedSpeedStat));
        setVanillaBaseValue(player, Attributes.ATTACK_SPEED, DEFAULT_PLAYER_ATTACK_SPEED);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }

        final double maxSpiritEnergy = getMaxSpiritEnergy(player);
        if (data.getSpiritEnergy() < 0.0 || data.getSpiritEnergy() > maxSpiritEnergy) {
            data.setSpiritEnergy(maxSpiritEnergy);
        }
    }

    // ---- Removable stat bonuses ----

    /**
     * Grants a stat bonus as attribute modifiers keyed by {@code id}, replacing any bonus already
     * registered under that id.
     *
     * <p>Modifiers rather than base values, so a later breakthrough multiplier (which scales base
     * values) never compounds a ring or bone, and removing the bonus takes back exactly what it gave.
     */
    public static void applyBonus(final Player player, final ResourceLocation id, final StatBonus bonus) {
        if (player.level().isClientSide()) {
            return;
        }

        putModifier(player, DAMAGE, id, bonus.damage());
        putModifier(player, HEALTH, id, bonus.health());
        putModifier(player, DEFENSE, id, bonus.defense());
        putModifier(player, SPEED, id, bonus.speed());
        putModifier(player, SPIRIT, id, bonus.spirit());
        putModifier(player, CULTIVATION_SPEED, id, bonus.cultivationSpeed());
    }

    /**
     * Raises every mod stat by {@code fraction} of its final value, replacing the previous rebirth
     * bonus so repeated rebirths never stack duplicate modifiers.
     */
    public static void applyRebirthBonus(final Player player, final double fraction) {
        for (final Holder<Attribute> attribute : ALL) {
            final AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(REBIRTH_BONUS_ID, fraction,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
        syncDerivedPlayerStats(player, player.getData(com.zelf115.soulland.cultivation.CultivationAttachment.CULTIVATION_DATA.get()));
    }

    /** Returns every stat the player earned back to what a fresh player starts with, leaving bonuses alone. */
    public static void resetEarnedStats(final Player player) {
        for (final Holder<Attribute> attribute : ALL) {
            final AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.setBaseValue(attribute.value().getDefaultValue());
            }
        }
    }

    /** Raises a single attribute by a percentage, under an id the caller can take back later. */
    public static void applyTemporaryPercentBonus(final Player player, final String id,
                                                  final Holder<Attribute> attribute, final double percent) {
        final AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, id), percent,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        syncDerivedPlayerStats(player, player.getData(com.zelf115.soulland.cultivation.CultivationAttachment.CULTIVATION_DATA.get()));
    }

    public static void applyTemporaryStatPercentBonus(final Player player, final String id, final double percent) {
        final ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, id);
        for (final Holder<Attribute> attribute : CULTIVATION_STATS) {
            final AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(modifierId, percent,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
        syncDerivedPlayerStats(player, player.getData(com.zelf115.soulland.cultivation.CultivationAttachment.CULTIVATION_DATA.get()));
    }

    public static void removeTemporaryBonus(final Player player, final String id) {
        removeBonus(player, ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, id));
    }

    /** Takes back every modifier registered under the given id, whatever attributes it touched. */
    public static void removeBonus(final Player player, final ResourceLocation id) {
        for (final Holder<Attribute> attribute : ALL) {
            final AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) instance.removeModifier(id);
        }
        syncDerivedPlayerStats(player, player.getData(com.zelf115.soulland.cultivation.CultivationAttachment.CULTIVATION_DATA.get()));
    }

    /**
     * Carries every mod attribute, base values and bonus modifiers alike, onto a freshly created
     * player entity. Vanilla hands the respawned player a blank attribute map.
     */
    public static void transferTo(final Player original, final Player clone) {
        for (final Holder<Attribute> attribute : ALL) {
            final AttributeInstance source = original.getAttribute(attribute);
            final AttributeInstance target = clone.getAttribute(attribute);
            if (source != null && target != null) {
                target.replaceFrom(source);
            }
        }
    }

    private static double valueOf(final Player player, final Holder<Attribute> attribute) {
        final AttributeInstance instance = player.getAttribute(attribute);
        return instance != null ? instance.getValue() : 0.0;
    }

    private static void addToBaseValue(final Player player, final Holder<Attribute> attribute, final double amount) {
        if (player.level().isClientSide()) return; // Always mutate on server

        final AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(Math.max(0.0, instance.getBaseValue() + amount));
        }
    }

    private static void putModifier(final Player player, final Holder<Attribute> attribute,
                                    final ResourceLocation id, final double amount) {
        final AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (amount == 0.0) {
            instance.removeModifier(id);
            return;
        }
        instance.addOrReplacePermanentModifier(
                new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
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
