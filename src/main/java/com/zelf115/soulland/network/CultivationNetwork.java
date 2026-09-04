package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.client.OverreachConfirmScreen;
import com.zelf115.soulland.cultivation.BreakthroughManager;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.cultivation.RingDisplayMode;
import com.zelf115.soulland.cultivation.SoulRingAbsorption;
import com.zelf115.soulland.item.SoulRingItem;
import com.zelf115.soulland.cultivation.MartialSoul;
import com.zelf115.soulland.cultivation.MartialSoulAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class CultivationNetwork {
    private static final int SPEED_STEP_PERCENT = 10;

    private CultivationNetwork() {
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(CultivationActionPayload.TYPE, CultivationActionPayload.STREAM_CODEC, CultivationNetwork::handleAction)
                .playToServer(OverreachConfirmPayload.TYPE, OverreachConfirmPayload.STREAM_CODEC, CultivationNetwork::handleOverreachConfirm)
                .playToClient(OverreachPromptPayload.TYPE, OverreachPromptPayload.STREAM_CODEC, CultivationNetwork::handleOverreachPrompt);
    }

    private static void handleOverreachPrompt(final OverreachPromptPayload payload, final IPayloadContext context) {
        // Resolved inside the lambda so the dedicated server never loads the client screen class.
        context.enqueueWork(() -> OverreachConfirmScreen.open(payload));
    }

    private static void handleAction(final CultivationActionPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
            switch (payload.action()) {
                case CultivationActionPayload.START_MEDITATION -> startMeditation(player);
                case CultivationActionPayload.INCREASE_SPEED -> adjustSpeed(player, data, SPEED_STEP_PERCENT);
                case CultivationActionPayload.DECREASE_SPEED -> adjustSpeed(player, data, -SPEED_STEP_PERCENT);
                case CultivationActionPayload.CYCLE_RING_DISPLAY -> cycleRingDisplay(player, data);
                case CultivationActionPayload.TOGGLE_EXTERNAL_BONE -> toggleExternalBone(player, data);
                case CultivationActionPayload.ATTEMPT_BREAKTHROUGH -> BreakthroughManager.attemptBreakthrough(player, data, player.level().getGameTime());
                case CultivationActionPayload.USE_MARTIAL_SOUL -> MartialSoulAbility.toggle(player, data);
                case CultivationActionPayload.CAST_MARTIAL_SOUL -> MartialSoulAbility.cast(player, data);
                default -> SoulLand.LOGGER.warn("Ignoring unknown cultivation action {}", payload.action());
            }
        });
    }

    private static void handleOverreachConfirm(final OverreachConfirmPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            final InteractionHand hand = handOf(payload.hand());
            final ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(SoulLand.SOUL_RING_ITEM.get())) {
                return;
            }

            SoulRingItem.reportAbsorption(player, SoulRingAbsorption.absorbByOverreach(player, stack));
        });
    }

    private static InteractionHand handOf(final int ordinal) {
        return ordinal == InteractionHand.OFF_HAND.ordinal() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private static void startMeditation(final ServerPlayer player) {
        if (player.hasEffect(SoulLand.MEDITATION_EFFECT)) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.meditation.already_active"));
            return;
        }

        player.addEffect(new MobEffectInstance(SoulLand.MEDITATION_EFFECT, CultivationManager.MEDITATION_DURATION_TICKS));
        player.sendSystemMessage(Component.translatable("soulland.cultivation.meditation.started"));
    }

    private static void adjustSpeed(final ServerPlayer player, final CultivationData data, final int deltaPercent) {
        final int previousPercent = data.getMovementUsagePercent();
        data.setMovementUsagePercent(previousPercent + deltaPercent);
        if (previousPercent == data.getMovementUsagePercent()) {
            return;
        }

        Stats.syncDerivedPlayerStats(player, data);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.speed_usage.set", data.getMovementUsagePercent()));
    }

    private static void cycleRingDisplay(final ServerPlayer player, final CultivationData data) {
        // The "all rings" mode belongs to players with a second martial soul, which issue #3 owns;
        // until then it stays out of the rotation.
        final RingDisplayMode next = data.getRingDisplayMode().next(false);
        data.setRingDisplayMode(next);
        player.sendSystemMessage(Component.translatable("soulland.soul_ring.display.set",
                Component.translatable(next.translationKey())));
    }

    private static void toggleExternalBone(final ServerPlayer player, final CultivationData data) {
        data.setExternalBoneVisible(!data.isExternalBoneVisible());
        player.sendSystemMessage(Component.translatable(data.isExternalBoneVisible()
                ? "soulland.spirit_bone.external.shown"
                : "soulland.spirit_bone.external.hidden"));
    }
}
