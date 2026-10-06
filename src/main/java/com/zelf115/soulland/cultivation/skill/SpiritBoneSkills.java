package com.zelf115.soulland.cultivation.skill;

import com.zelf115.soulland.cultivation.AbsorbedBone;
import com.zelf115.soulland.cultivation.CultivationData;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** The skills a player draws from absorbed spirit bones, whatever their martial soul or its affinities. */
public final class SpiritBoneSkills {

    private SpiritBoneSkills() {
    }

    public static void useSelectedBoneSkill(final Player player, final CultivationData data) {
        selectedBone(data).ifPresentOrElse(
                bone -> data.getSkillRuntime().cast(player, data, bone.skill().orElseThrow()),
                () -> player.displayClientMessage(Component.translatable("soulland.cultivation.bone_skill.none"), true));
    }

    /** The bone the cast key uses, or none when no bone carries a skill to cast. */
    public static Optional<AbsorbedBone> selectedBone(final CultivationData data) {
        final List<AbsorbedBone> bones = castableBones(data);
        if (bones.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(bones.get(Math.min(data.getSelectedBoneIndex(), bones.size() - 1)));
    }

    public static void selectNextBone(final Player player, final CultivationData data) {
        final List<AbsorbedBone> bones = castableBones(data);
        if (bones.isEmpty()) {
            player.displayClientMessage(Component.translatable("soulland.cultivation.bone_skill.none"), true);
            return;
        }
        final int next = (data.getSelectedBoneIndex() + 1) % bones.size();
        data.setSelectedBoneIndex(next);
        final AbsorbedBone bone = bones.get(next);
        player.displayClientMessage(Component.translatable("soulland.cultivation.bone_skill.selected",
                bone.slot(), bone.skill().orElseThrow().displayName()), true);
    }

    private static List<AbsorbedBone> castableBones(final CultivationData data) {
        return data.getSpiritBones().values().stream()
                .filter(bone -> bone.skill().filter(skill -> !skill.isPassive()).isPresent())
                .toList();
    }
}
