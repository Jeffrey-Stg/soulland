package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.skill.Skill;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

/**
 * The skills a player draws from the soul rings of the martial soul they have active.
 *
 * <p>Two souls break the common rule: the Nine Heart Begonia has no ring skills at all, and the
 * Glazed Tile Pagoda turns each of its rings into a fixed stat buff instead of a skill.
 */
public final class SoulRingSkills {

    private static final String SKILL_BUFF_ID = "soulland_soul_ring_skill";
    private static final int TICKS_PER_MINUTE = 1200;
    private static final int BUFF_DURATION_TICKS = 5 * TICKS_PER_MINUTE;
    private static final double SKILL_ENERGY_COST = 10.0;
    private static final double RING_SKILL_PERCENT_PER_TIER = 0.05;
    private static final double PAGODA_STAT_BUFF_PERCENT = 0.20;
    private static final double PAGODA_HEAL_PERCENT = 0.30;
    private static final double BERSERK_DAMAGE_PERCENT = 0.30;
    private static final double BERSERK_DEFENSE_PENALTY = -0.20;
    private static final double GREATER_BERSERK_DAMAGE_PERCENT = 0.60;
    private static final double GREATER_BERSERK_DEFENSE_PENALTY = -0.35;
    private static final double GREATEST_BERSERK_DAMAGE_PERCENT = 1.00;
    private static final double GREATEST_BERSERK_DEFENSE_PENALTY = -0.50;
    private static final double SUPPORT_RANGE = 15.0;

    private enum PagodaBuff {
        DAMAGE,
        DEFENSE,
        HEAL,
        BERSERK,
        GREATER_BERSERK,
        GREATEST_BERSERK
    }

    /** The pagoda's buffs, in the order its rings are absorbed. */
    private static final List<PagodaBuff> PAGODA_BUFFS = List.of(
            PagodaBuff.DAMAGE, PagodaBuff.DEFENSE, PagodaBuff.HEAL, PagodaBuff.BERSERK,
            PagodaBuff.DAMAGE, PagodaBuff.DEFENSE, PagodaBuff.HEAL,
            PagodaBuff.GREATER_BERSERK, PagodaBuff.GREATEST_BERSERK);

    private SoulRingSkills() {
    }

    public static void useSelectedRingSkill(final Player player, final CultivationData data) {
        if (!data.isMartialSoulActive()) return;

        final MartialSoul soul = data.getActiveMartialSoul();
        if (soul == null) return;
        if (soul == MartialSoul.NINE_HEART_BEGONIA) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.ring_skill.blocked"));
            return;
        }

        final List<AbsorbedRing> rings = data.getRings(data.getActiveSoulSlot());
        if (rings.isEmpty()) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.ring_skill.no_rings"));
            return;
        }
        final int ringIndex = Math.min(data.getSelectedRingIndex(), rings.size() - 1);
        final AbsorbedRing ring = rings.get(ringIndex);
        if (!isPagoda(soul) && ring.skill().isPresent()) {
            data.getSkillRuntime().cast(player, data, ring.skill().get());
            return;
        }
        if (data.getSpiritEnergy() < SKILL_ENERGY_COST) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.ring_skill.no_energy"));
            return;
        }

        data.setSpiritEnergy(data.getSpiritEnergy() - SKILL_ENERGY_COST);
        if (isPagoda(soul)) {
            applyPagodaBuff(player, ringIndex);
            return;
        }
        applyRingSkill(player, data, ring);
    }

    /** Moves the cast key onto the next ring of the active soul's own track, passing over passive skills. */
    public static void selectNextRing(final Player player, final CultivationData data) {
        final List<AbsorbedRing> rings = data.getRings(data.getActiveSoulSlot());
        if (rings.isEmpty()) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.ring_skill.no_rings"));
            return;
        }

        final boolean pagoda = isPagoda(data.getActiveMartialSoul());
        final OptionalInt next = IntStream.rangeClosed(1, rings.size())
                .map(offset -> (data.getSelectedRingIndex() + offset) % rings.size())
                .filter(index -> pagoda || !isPassive(rings.get(index)))
                .findFirst();
        if (next.isEmpty()) {
            player.sendSystemMessage(Component.translatable("soulland.cultivation.ring_skill.none_castable"));
            return;
        }

        final AbsorbedRing ring = rings.get(next.getAsInt());
        data.setSelectedRingIndex(next.getAsInt());
        player.sendSystemMessage(Component.translatable("soulland.cultivation.ring_skill.selected",
                next.getAsInt() + 1, ring.skill().map(Skill::displayName).orElse(ring.coloredSourceName())));
    }

    private static boolean isPassive(final AbsorbedRing ring) {
        return ring.skill().map(Skill::isPassive).orElse(false);
    }

    public static void expireBuff(final Player player, final CultivationData data, final long gameTick) {
        if (data.getMartialSoulBuffUntil() > 0 && gameTick >= data.getMartialSoulBuffUntil()) {
            Stats.removeTemporaryBonus(player, SKILL_BUFF_ID);
            data.setMartialSoulBuffUntil(0L);
        }
    }

    private static boolean isPagoda(final MartialSoul soul) {
        return soul == MartialSoul.SEVEN_TREASURE_GLAZED_TILE_PAGODA
                || soul == MartialSoul.NINE_TREASURE_GLAZED_TILE_PAGODA;
    }

    private static void applyRingSkill(final Player player, final CultivationData data, final AbsorbedRing ring) {
        Stats.applyTemporaryStatPercentBonus(player, SKILL_BUFF_ID, RING_SKILL_PERCENT_PER_TIER * ring.tier());
        startBuffTimer(player, data);
        player.sendSystemMessage(Component.translatable("soulland.cultivation.ring_skill.used", ring.coloredSourceName()));
    }

    private static void applyPagodaBuff(final Player caster, final int ringIndex) {
        final PagodaBuff buff = PAGODA_BUFFS.get(Math.min(ringIndex, PAGODA_BUFFS.size() - 1));
        final Player target = supportTarget(caster);
        switch (buff) {
            case DAMAGE -> applyTimedPercent(target, Stats.DAMAGE, PAGODA_STAT_BUFF_PERCENT);
            case DEFENSE -> applyTimedPercent(target, Stats.DEFENSE, PAGODA_STAT_BUFF_PERCENT);
            case HEAL -> target.heal((float) (target.getMaxHealth() * PAGODA_HEAL_PERCENT));
            case BERSERK -> applyBerserk(target, BERSERK_DAMAGE_PERCENT, BERSERK_DEFENSE_PENALTY);
            case GREATER_BERSERK -> applyBerserk(target, GREATER_BERSERK_DAMAGE_PERCENT, GREATER_BERSERK_DEFENSE_PENALTY);
            case GREATEST_BERSERK -> applyBerserk(target, GREATEST_BERSERK_DAMAGE_PERCENT, GREATEST_BERSERK_DEFENSE_PENALTY);
        }
    }

    private static void applyBerserk(final Player target, final double damagePercent, final double defensePercent) {
        Stats.applyTemporaryPercentBonus(target, SKILL_BUFF_ID, Stats.DAMAGE, damagePercent);
        Stats.applyTemporaryPercentBonus(target, SKILL_BUFF_ID, Stats.DEFENSE, defensePercent);
        startBuffTimer(target, target.getData(CultivationAttachment.CULTIVATION_DATA.get()));
    }

    private static void applyTimedPercent(final Player target, final Holder<Attribute> attribute, final double percent) {
        Stats.applyTemporaryPercentBonus(target, SKILL_BUFF_ID, attribute, percent);
        startBuffTimer(target, target.getData(CultivationAttachment.CULTIVATION_DATA.get()));
    }

    private static void startBuffTimer(final Player target, final CultivationData targetData) {
        targetData.setMartialSoulBuffUntil(target.level().getGameTime() + BUFF_DURATION_TICKS);
    }

    /** The ally the pagoda supports: the nearest player in sight, or the caster when alone. */
    private static Player supportTarget(final Player caster) {
        return caster.level().getEntitiesOfClass(Player.class,
                        caster.getBoundingBox().inflate(SUPPORT_RANGE),
                        candidate -> candidate != caster && caster.hasLineOfSight(candidate)).stream()
                .min(Comparator.comparingDouble(caster::distanceToSqr))
                .orElse(caster);
    }
}
