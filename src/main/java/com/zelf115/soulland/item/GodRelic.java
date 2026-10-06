package com.zelf115.soulland.item;

import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.trial.GodTrial;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;

/** What the four god relics share: who may use them, and how their raw power is expressed. */
public final class GodRelic {

    /** Relics swing like the heavy weapons they are. */
    private static final float ATTACK_SPEED = -2.8F;

    private GodRelic() {
    }

    public static Item.Properties properties() {
        return new Item.Properties().stacksTo(1).component(DataComponents.UNBREAKABLE, new Unbreakable(true));
    }

    /**
     * Built by hand rather than through {@code SwordItem.createAttributes}, which would add the
     * tool tier's own damage on top and leave the relic short of the exact figure it promises.
     */
    public static ItemAttributeModifiers attributes(final double bonusDamage) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, bonusDamage,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, ATTACK_SPEED,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    public static boolean isEntitled(final Player player, final GodTrial trial) {
        final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
        return data.hasRelicEntitlement(trial);
    }

    /**
     * Whether a relic's use must be turned away. The client never receives the cultivation data, so
     * it lets every use through and leaves the decision to the server.
     */
    public static boolean refusesUse(final Player player, final GodTrial trial) {
        return !player.level().isClientSide() && !isEntitled(player, trial);
    }

    /** Whether the weapon the player is swinging is a relic they never earned. */
    public static boolean isUnearnedRelic(final Player player) {
        final GodTrial trial = GodTrial.forRelic(player.getMainHandItem().getItem());
        return trial != null && !isEntitled(player, trial);
    }

    public static void refuse(final Player player) {
        player.displayClientMessage(Component.translatable("soulland.trial.relic.not_earned"), true);
    }

    public static void appendOwnerTooltip(final List<Component> tooltip) {
        tooltip.add(Component.translatable("soulland.tooltip.relic.owner_bound").withStyle(ChatFormatting.GRAY));
    }
}
