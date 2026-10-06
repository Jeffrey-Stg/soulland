package com.zelf115.soulland.network;

import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.AbsorbedRing;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import com.zelf115.soulland.cultivation.CultivationManager;
import com.zelf115.soulland.cultivation.MartialSoul;
import com.zelf115.soulland.cultivation.SoulRingSkills;
import com.zelf115.soulland.cultivation.skill.Skill;
import com.zelf115.soulland.cultivation.skill.SpiritBoneSkills;
import com.zelf115.soulland.qi.QiManager;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Sends a player the current state of their cultivation HUD and screens. */
public final class HudSync {

    private HudSync() {
    }

    /** Also refreshes the player's copy of their cultivation record, which the cultivation screens read. */
    public static void send(final ServerPlayer player, final CultivationData data) {
        player.syncData(CultivationAttachment.CULTIVATION_DATA);
        // The player's own panel shows the active soul's rings whatever the display mode. visibleRings()
        // is the choice of what other players get to see, and it starts out hidden.
        final List<AbsorbedRing> rings = data.getRings(data.getActiveSoulSlot());
        final List<Integer> ringTiers = rings.stream().map(AbsorbedRing::tier).toList();
        final HudSyncPayload.Gauge xp = new HudSyncPayload.Gauge(data.getXp(), CultivationManager.xpRequiredForLevel(data.getLevel()));
        final HudSyncPayload.Gauge spiritEnergy = new HudSyncPayload.Gauge(data.getSpiritEnergy(), Stats.getMaxSpiritEnergy(player));
        final MartialSoul soul = data.getActiveMartialSoul();
        final int qi = QiManager.getQiAt(player.serverLevel(), player.blockPosition());
        PacketDistributor.sendToPlayer(player, new HudSyncPayload(
                data.getLevel(), xp, data.isInBottleneck(), spiritEnergy, qi,
                soul == null ? "" : soul.displayName(), selectedRing(data, rings), selectedBone(data), ringTiers));
    }

    private static HudSyncPayload.Selection selectedRing(final CultivationData data, final List<AbsorbedRing> rings) {
        if (rings.isEmpty()) {
            return HudSyncPayload.Selection.NONE;
        }
        final int index = Math.min(data.getSelectedRingIndex(), rings.size() - 1);
        final AbsorbedRing ring = rings.get(index);
        return new HudSyncPayload.Selection(String.valueOf(index + 1), ring.sourceName(), ring.tier(),
                SoulRingSkills.abilityKey(data.getActiveMartialSoul(), index, ring));
    }

    private static HudSyncPayload.Selection selectedBone(final CultivationData data) {
        return SpiritBoneSkills.selectedBone(data)
                .map(bone -> new HudSyncPayload.Selection(bone.slot(), bone.sourceName(), bone.tier(),
                        bone.skill().map(Skill::translationKey).orElse("")))
                .orElse(HudSyncPayload.Selection.NONE);
    }
}
