package com.zelf115.soulland.network;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.client.HudClientData;
import com.zelf115.soulland.client.OverreachConfirmScreen;
import com.zelf115.soulland.compat.CuriosCompat;
import com.zelf115.soulland.cultivation.BreakthroughManager;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.cultivation.ModStatsToggle;
import com.zelf115.soulland.cultivation.RingDisplayMode;
import com.zelf115.soulland.cultivation.RingDisplaySync;
import com.zelf115.soulland.cultivation.SoulRingAbsorption;
import com.zelf115.soulland.cultivation.technique.PurpleDemonEye;
import com.zelf115.soulland.cultivation.technique.ShadowStep;
import com.zelf115.soulland.cultivation.skill.SpiritBoneSkills;
import com.zelf115.soulland.item.PillFurnaceItem;
import com.zelf115.soulland.item.SoulRingItem;
import com.zelf115.soulland.cultivation.MartialSoul;
import com.zelf115.soulland.cultivation.MartialSoulAbility;
import com.zelf115.soulland.menu.AlchemyMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class CultivationNetwork {

    private CultivationNetwork() {
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(CultivationActionPayload.TYPE, CultivationActionPayload.STREAM_CODEC, CultivationNetwork::handleAction)
                .playToServer(OverreachConfirmPayload.TYPE, OverreachConfirmPayload.STREAM_CODEC, CultivationNetwork::handleOverreachConfirm)
                .playToClient(OverreachPromptPayload.TYPE, OverreachPromptPayload.STREAM_CODEC, CultivationNetwork::handleOverreachPrompt)
                .playToClient(HudSyncPayload.TYPE, HudSyncPayload.STREAM_CODEC, CultivationNetwork::handleHudSync)
                .playToServer(ChooseMartialSoulPayload.TYPE, ChooseMartialSoulPayload.STREAM_CODEC, CultivationNetwork::handleChooseMartialSoul)
                .playToClient(OpenMartialSoulPickerPayload.TYPE, OpenMartialSoulPickerPayload.STREAM_CODEC, CultivationNetwork::handleOpenMartialSoulPicker)
                .playToClient(RingDisplayPayload.TYPE, RingDisplayPayload.STREAM_CODEC, CultivationNetwork::handleRingDisplay)
                .playToServer(SetMovementUsagePayload.TYPE, SetMovementUsagePayload.STREAM_CODEC, CultivationNetwork::handleSetMovementUsage);
    }

    private static void handleHudSync(final HudSyncPayload payload, final IPayloadContext context) {
        // Resolved inside the lambda so the dedicated server never loads the client holder class.
        context.enqueueWork(() -> HudClientData.update(payload));
    }

    private static void handleRingDisplay(final RingDisplayPayload payload, final IPayloadContext context) {
        // Resolved inside the lambda so the dedicated server never loads the client holder class.
        context.enqueueWork(() -> com.zelf115.soulland.client.RingDisplayClientData.update(payload));
    }

    private static void handleOverreachPrompt(final OverreachPromptPayload payload, final IPayloadContext context) {
        // Resolved inside the lambda so the dedicated server never loads the client screen class.
        context.enqueueWork(() -> OverreachConfirmScreen.open(payload));
    }

    private static void handleOpenMartialSoulPicker(final OpenMartialSoulPickerPayload payload, final IPayloadContext context) {
        // Resolved inside the lambda so the dedicated server never loads the client screen class.
        context.enqueueWork(com.zelf115.soulland.client.screen.CultivationScreen::open);
    }

    private static void handleSetMovementUsage(final SetMovementUsagePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
            data.setMovementUsagePercent(payload.percent());
            Stats.syncDerivedPlayerStats(player, data);
            HudSync.send(player, data);
        });
    }

    private static void handleChooseMartialSoul(final ChooseMartialSoulPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            final MartialSoul[] souls = MartialSoul.values();
            if (payload.martialSoulOrdinal() < 0 || payload.martialSoulOrdinal() >= souls.length) {
                return;
            }
            final MartialSoul chosen = souls[payload.martialSoulOrdinal()];
            final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
            if (data.getMartialSoul() == null) {
                com.zelf115.soulland.spirit.AffinitySystem.chooseMartialSoul(player, chosen);
            } else if (data.isSecondMartialSoulPending()) {
                com.zelf115.soulland.spirit.AffinitySystem.chooseSecondMartialSoul(player, chosen);
            }
            HudSync.send(player, data);
        });
    }

    private static void handleAction(final CultivationActionPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            final CultivationData data = player.getData(CultivationAttachment.CULTIVATION_DATA.get());
            switch (payload.action()) {
                case CultivationActionPayload.START_MEDITATION -> startMeditation(player);
                case CultivationActionPayload.CYCLE_RING_DISPLAY -> cycleRingDisplay(player, data);
                case CultivationActionPayload.TOGGLE_EXTERNAL_BONE -> toggleExternalBone(player, data);
                case CultivationActionPayload.ATTEMPT_BREAKTHROUGH -> BreakthroughManager.attemptBreakthrough(player, data, player.level().getGameTime());
                case CultivationActionPayload.USE_MARTIAL_SOUL -> MartialSoulAbility.toggle(player, data);
                case CultivationActionPayload.CAST_MARTIAL_SOUL -> MartialSoulAbility.cast(player, data);
                case CultivationActionPayload.OPEN_ALCHEMY_MENU -> openAlchemyMenu(player);
                case CultivationActionPayload.SWITCH_MARTIAL_SOUL -> MartialSoulAbility.switchActiveSoul(player, data);
                case CultivationActionPayload.SELECT_NEXT_RING -> MartialSoulAbility.selectNextRing(player, data);
                case CultivationActionPayload.DEMON_EYE -> PurpleDemonEye.use(player, data);
                case CultivationActionPayload.DEMON_EYE_STRIKE -> PurpleDemonEye.strike(player, data);
                case CultivationActionPayload.SHADOW_STEP -> ShadowStep.use(player, data);
                case CultivationActionPayload.SELECT_NEXT_BONE -> SpiritBoneSkills.selectNextBone(player, data);
                case CultivationActionPayload.CAST_BONE_SKILL -> SpiritBoneSkills.useSelectedBoneSkill(player, data);
                case CultivationActionPayload.RELEASE_CHANNEL -> data.getSkillRuntime().releaseChannel();
                case CultivationActionPayload.TOGGLE_MOD_STATS -> ModStatsToggle.toggle(player, data);
                default -> SoulLand.LOGGER.warn("Ignoring unknown cultivation action {}", payload.action());
            }
            HudSync.send(player, data);
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
            player.displayClientMessage(Component.translatable("soulland.cultivation.meditation.already_active"), true);
            return;
        }

        player.addEffect(new MobEffectInstance(SoulLand.MEDITATION_EFFECT, CultivationManager.MEDITATION_DURATION_TICKS));
        player.displayClientMessage(Component.translatable("soulland.cultivation.meditation.started"), true);
    }

    private static void cycleRingDisplay(final ServerPlayer player, final CultivationData data) {
        final RingDisplayMode next = data.getRingDisplayMode().next(data.getSecondaryMartialSoul() != null);
        data.setRingDisplayMode(next);
        RingDisplaySync.broadcast(player);
        player.displayClientMessage(Component.translatable("soulland.soul_ring.display.set",
                Component.translatable(next.translationKey())), true);
    }

    private static void toggleExternalBone(final ServerPlayer player, final CultivationData data) {
        data.setExternalBoneVisible(!data.isExternalBoneVisible());
        player.displayClientMessage(Component.translatable(data.isExternalBoneVisible()
                ? "soulland.spirit_bone.external.shown"
                : "soulland.spirit_bone.external.hidden"), true);
    }

    private static void openAlchemyMenu(final ServerPlayer player) {
        // The hotkey only works from a worn Curios slot; right-clicking a furnace item always
        // works and doesn't go through this payload at all.
        if (!CuriosCompat.isLoaded()) {
            player.displayClientMessage(Component.translatable("soulland.alchemy.curios_not_loaded"), true);
            return;
        }

        CuriosCompat.findEquippedPillFurnace(player)
                .map(stack -> (PillFurnaceItem) stack.getItem())
                .ifPresentOrElse(
                        furnace -> AlchemyMenu.open(player, furnace.tier()),
                        () -> player.displayClientMessage(Component.translatable("soulland.alchemy.no_furnace_equipped"), true));
    }
}
