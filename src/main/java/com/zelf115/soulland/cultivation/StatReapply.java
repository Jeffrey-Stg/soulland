package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.StatBand;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.trial.GodTrialRewards;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/**
 * Rebuilds every stat bonus a player holds from their cultivation record: strips each modifier the
 * mod granted, pulls absorbed rings and bones into the stat band of their colour, then grants rings,
 * bones, god rewards and the rebirth bonus again.
 *
 * <p>Base values are left alone: herbs, pills and meditation raised them, and nothing records how much.
 */
public final class StatReapply {

    private StatReapply() {
    }

    public static void perform(final ServerPlayer player, final CultivationData data) {
        MartialSoulAbility.forceDeactivate(player, data);
        SoulRingSkills.endBuff(player, data);
        Stats.removeOwnBonuses(player);
        clampRingsIntoBand(data);
        SoulRingAbsorption.reapplyRingBonuses(player, data);
        reapplyBones(player, data);
        GodTrialRewards.reapplyStatRewards(player, data);
        Rebirth.reapplyBonus(player, data);
        Stats.syncDerivedPlayerStats(player, data);
    }

    private static void clampRingsIntoBand(final CultivationData data) {
        final List<AbsorbedRing> rings = data.getAbsorbedRings();
        for (int index = 0; index < rings.size(); index++) {
            final AbsorbedRing ring = rings.get(index);
            data.replaceRing(index, ring.withBonus(StatBand.clamp(ring.tier(), ring.bonus())));
        }
    }

    private static void reapplyBones(final ServerPlayer player, final CultivationData data) {
        for (final AbsorbedBone bone : List.copyOf(data.getSpiritBones().values())) {
            final AbsorbedBone clamped = bone.withBonus(StatBand.clamp(bone.tier(), bone.bonus()));
            data.putBone(clamped);
            Stats.applyBonus(player, AbsorbedBone.modifierId(clamped.slot()), clamped.bonus());
        }
    }
}
