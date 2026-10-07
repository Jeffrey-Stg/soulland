package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.network.OpenMartialSoulPickerPayload;
import com.zelf115.soulland.spirit.AffinitySystem;
import com.zelf115.soulland.trial.GodTrialManager;
import com.zelf115.soulland.trial.GodTrialReward;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Sends a cultivator back to level one, keeping only what a rebirth is meant to carry: the count
 * itself, which raises the innate stat, opens the level 100 gate and lifts every stat by 5% per
 * rebirth, and the god relics already won.
 *
 * <p>The martial souls go with the rest, so every rebirth puts the player through the picker again.
 *
 * <p>A relic stays wieldable forever once its trial is passed, but the stat rewards that trial paid
 * out do not: a rebirth strips them along with every other stat the cultivator built up.
 */
public final class Rebirth {

    private static final int REBIRTH_LEVEL = CultivationManager.MAX_LEVEL;
    private static final double BONUS_PER_REBIRTH = 0.05;

    private Rebirth() {
    }

    public static boolean perform(final ServerPlayer player, final CultivationData data) {
        if (data.getLevel() < REBIRTH_LEVEL) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.rebirth.locked", REBIRTH_LEVEL));
            return false;
        }

        data.setRebirthCount(data.getRebirthCount() + 1);
        resetCultivation(player, data);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.rebirth.done", data.getRebirthCount()));
        return true;
    }

    /**
     * Wipes everything a rebirth wipes without counting as one, then sends the player to the
     * martial soul picker.
     */
    public static void resetCultivation(final ServerPlayer player, final CultivationData data) {
        MartialSoulAbility.forceDeactivate(player, data);
        endModEffects(player, data);
        removeRingBonuses(player, data);
        data.clearAbsorbedRings();
        removeBoneBonuses(player, data);
        data.clearSpiritBones();
        data.setSelectedBoneIndex(0);
        removeGodTrialBonuses(player, data);
        GodTrialManager.clearTrial(data);
        Stats.resetEarnedStats(player);
        resetProgress(data);
        data.setTitle("");
        reapplyBonus(player, data);
        AffinitySystem.clearMartialSoulForRebirth(data);
        data.setActiveSoulSlot(SoulSlot.PRIMARY);
        data.setSelectedRingIndex(0);
        data.setMartialSoulCanReachLevel100(false);
        CultivationManager.applyFlightAbilities(player, data.getLevel());
        Stats.syncDerivedPlayerStats(player, data);
        RingDisplaySync.broadcast(player);
        PacketDistributor.sendToPlayer(player, new OpenMartialSoulPickerPayload());
    }

    /** Lifts every stat by 5% per rebirth so far; nothing at all before the first. */
    public static void reapplyBonus(final ServerPlayer player, final CultivationData data) {
        if (data.getRebirthCount() > 0) {
            Stats.applyRebirthBonus(player, data.getRebirthCount() * BONUS_PER_REBIRTH);
        }
    }

    private static void endModEffects(final ServerPlayer player, final CultivationData data) {
        SoulLand.MOB_EFFECTS.getEntries().forEach(player::removeEffect);
        SoulRingSkills.endBuff(player, data);
        data.getSkillRuntime().stopSustainedSkill();
    }

    private static void removeRingBonuses(final ServerPlayer player, final CultivationData data) {
        for (int index = 0; index < data.getSoulRingCount(); index++) {
            Stats.removeBonus(player, AbsorbedRing.modifierId(index));
        }
    }

    private static void removeBoneBonuses(final ServerPlayer player, final CultivationData data) {
        for (final AbsorbedBone bone : data.getSpiritBones().values()) {
            Stats.removeBonus(player, AbsorbedBone.modifierId(bone.slot()));
        }
    }

    private static void removeGodTrialBonuses(final ServerPlayer player, final CultivationData data) {
        for (int index = 0; index < data.getGodTrialRewards().size(); index++) {
            Stats.removeBonus(player, GodTrialReward.modifierId(index));
        }
        data.clearGodTrialRewards();
    }

    private static void resetProgress(final CultivationData data) {
        data.setLevel(1);
        data.setXp(0.0);
        data.setInBottleneck(false);
        data.setBreakthroughFailures(0);
        data.setSuccessfulBreakthroughCount(0);
        data.setBreakthroughCooldownUntil(0L);
    }
}
