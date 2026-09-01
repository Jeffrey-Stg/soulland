package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class CultivationNetwork {
    private CultivationNetwork() {
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(CultivationActionPayload.TYPE, CultivationActionPayload.STREAM_CODEC, CultivationNetwork::handleAction);
    }

    private static void handleAction(final CultivationActionPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
            switch (payload.action()) {
                case CultivationActionPayload.START_MEDITATION -> startMeditation(player);
                case CultivationActionPayload.INCREASE_SPEED -> adjustSpeed(player, data, 10);
                case CultivationActionPayload.DECREASE_SPEED -> adjustSpeed(player, data, -10);
                default -> SoulLand.LOGGER.warn("Ignoring unknown cultivation action {}", payload.action());
            }
        });
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
}
