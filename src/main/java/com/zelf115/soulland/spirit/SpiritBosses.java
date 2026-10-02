package com.zelf115.soulland.spirit;

import com.zelf115.soulland.cultivation.skill.Skill;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

/**
 * Every boss beast, keyed by entity path: its fixed age, the skill its soul ring carries and the
 * spirit bones it always drops. No regular beast, ring pool or ordinary bone carries these skills.
 */
public final class SpiritBosses {

    public record BossBone(String slot, Skill skill) {
    }

    public record BossProfile(int years, Skill ringSkill, List<BossBone> bones) {
    }

    private static final String TORSO = "Torso Bone";
    private static final String LEFT_ARM = "Left Arm Bone";
    private static final String RIGHT_ARM = "Right Arm Bone";

    private static final Map<String, BossProfile> BY_PATH = Map.of(
            "ice_jade_scorpion_empress", new BossProfile(400_000, Skill.ICE_EMPRESS_PINCER, List.of(
                    new BossBone(TORSO, Skill.PERMAFROST_DOMAIN),
                    new BossBone(LEFT_ARM, Skill.ICE_EXPLOSION))),
            "ice_bear_king", new BossProfile(300_000, Skill.ICE_BEAR_KING_BLIZZARD, List.of()),
            "skydream_ice_worm", new BossProfile(1_000_000, Skill.SPIRITUAL_SHOCK, List.of()),
            "three_eyed_golden_lion_boss", new BossProfile(1_000_000, Skill.SPIRITUAL_DISPOSSESSION, List.of()),
            "sky_azure_bull_python", new BossProfile(300_000, Skill.DRAGON_COIL, List.of(
                    new BossBone(RIGHT_ARM, Skill.SKY_AZURE_THUNDERCLAP))),
            "titan_giant_ape", new BossProfile(200_000, Skill.TITAN_SMASH, List.of(
                    new BossBone(LEFT_ARM, Skill.GRAVITY_CONTROL))),
            "evil_spirit_orca_king", new BossProfile(199_000, Skill.ORCA_DEVILS_ABSORPTION, List.of()));

    private SpiritBosses() {
    }

    public static Optional<BossProfile> of(final EntityType<?> type) {
        final ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return Optional.ofNullable(BY_PATH.get(id.getPath()));
    }

    public static boolean isBoss(final EntityType<?> type) {
        return of(type).isPresent();
    }

    public static Set<String> paths() {
        return BY_PATH.keySet();
    }
}
