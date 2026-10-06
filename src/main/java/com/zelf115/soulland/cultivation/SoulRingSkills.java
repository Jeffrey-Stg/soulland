package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import com.zelf115.soulland.cultivation.skill.Skill;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
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
    private static final double PAGODA_HEAL_PERCENT = 0.30;
    /** Effect levels, counted from zero; Berserk's level picks how fierce it is. */
    private static final int FIRST_LEVEL = 0;
    private static final int SECOND_LEVEL = 1;
    private static final int THIRD_LEVEL = 2;
    private static final double SUPPORT_RANGE = 15.0;

    private enum PagodaBuff {
        DAMAGE,
        DEFENSE,
        HEAL,
        BERSERK,
        GREATER_BERSERK,
        GREATEST_BERSERK;

        String translationKey() {
            return "soulland.pagoda_buff." + name().toLowerCase(Locale.ROOT);
        }
    }

    /** The pagoda's buffs, in the order its rings are absorbed. */
    private static final List<PagodaBuff> PAGODA_BUFFS = List.of(
            PagodaBuff.DAMAGE, PagodaBuff.DEFENSE, PagodaBuff.HEAL, PagodaBuff.BERSERK,
            PagodaBuff.DAMAGE, PagodaBuff.DEFENSE, PagodaBuff.HEAL,
            PagodaBuff.GREATER_BERSERK, PagodaBuff.GREATEST_BERSERK);

    private SoulRingSkills() {
    }

    public static void useSelectedRingSkill(final Player player, final CultivationData data) {
        final MartialSoul soul = data.getActiveMartialSoul();
        if (soul == null) return;
        if (!data.isMartialSoulActive()) {
            player.displayClientMessage(Component.translatable("soulland.cultivation.ring_skill.soul_inactive"), true);
            return;
        }
        if (soul == MartialSoul.NINE_HEART_BEGONIA) {
            player.displayClientMessage(Component.translatable("soulland.cultivation.ring_skill.blocked"), true);
            return;
        }

        final List<AbsorbedRing> rings = data.getRings(data.getActiveSoulSlot());
        if (rings.isEmpty()) {
            player.displayClientMessage(Component.translatable("soulland.cultivation.ring_skill.no_rings"), true);
            return;
        }
        final int ringIndex = Math.min(data.getSelectedRingIndex(), rings.size() - 1);
        final AbsorbedRing ring = rings.get(ringIndex);
        if (!isPagoda(soul) && ring.skill().isPresent()) {
            data.getSkillRuntime().cast(player, data, ring.skill().get());
            return;
        }
        if (data.getSpiritEnergy() < SKILL_ENERGY_COST) {
            player.displayClientMessage(Component.translatable("soulland.cultivation.ring_skill.no_energy"), true);
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
            player.displayClientMessage(Component.translatable("soulland.cultivation.ring_skill.no_rings"), true);
            return;
        }

        final boolean pagoda = isPagoda(data.getActiveMartialSoul());
        final OptionalInt next = IntStream.rangeClosed(1, rings.size())
                .map(offset -> (data.getSelectedRingIndex() + offset) % rings.size())
                .filter(index -> pagoda || !isPassive(rings.get(index)))
                .findFirst();
        if (next.isEmpty()) {
            player.displayClientMessage(Component.translatable("soulland.cultivation.ring_skill.none_castable"), true);
            return;
        }

        final AbsorbedRing ring = rings.get(next.getAsInt());
        data.setSelectedRingIndex(next.getAsInt());
        final String abilityKey = abilityKey(data.getActiveMartialSoul(), next.getAsInt(), ring);
        player.displayClientMessage(Component.translatable("soulland.cultivation.ring_skill.selected",
                next.getAsInt() + 1, abilityKey.isEmpty() ? ring.coloredSourceName() : Component.translatable(abilityKey)), true);
    }

    /**
     * The translation key of what casting this ring does: the pagoda's own buff for its ring, otherwise
     * the ring's skill, or empty when the ring carries none.
     */
    public static String abilityKey(final MartialSoul soul, final int ringIndex, final AbsorbedRing ring) {
        if (isPagoda(soul)) {
            return pagodaBuffFor(ringIndex).translationKey();
        }
        return ring.skill().map(Skill::translationKey).orElse("");
    }

    private static PagodaBuff pagodaBuffFor(final int ringIndex) {
        return PAGODA_BUFFS.get(Math.min(ringIndex, PAGODA_BUFFS.size() - 1));
    }

    private static boolean isPassive(final AbsorbedRing ring) {
        return ring.skill().map(Skill::isPassive).orElse(false);
    }

    public static void expireBuff(final Player player, final CultivationData data, final long gameTick) {
        if (data.getMartialSoulBuffUntil() > 0 && gameTick >= data.getMartialSoulBuffUntil()) {
            endBuff(player, data);
        }
    }

    public static void endBuff(final Player player, final CultivationData data) {
        Stats.removeTemporaryBonus(player, SKILL_BUFF_ID);
        data.setMartialSoulBuffUntil(0L);
    }

    private static boolean isPagoda(final MartialSoul soul) {
        return soul == MartialSoul.SEVEN_TREASURE_GLAZED_TILE_PAGODA
                || soul == MartialSoul.NINE_TREASURE_GLAZED_TILE_PAGODA;
    }

    private static void applyRingSkill(final Player player, final CultivationData data, final AbsorbedRing ring) {
        Stats.applyTemporaryStatPercentBonus(player, SKILL_BUFF_ID, RING_SKILL_PERCENT_PER_TIER * ring.tier());
        startBuffTimer(player, data);
        player.displayClientMessage(Component.translatable("soulland.cultivation.ring_skill.used", ring.coloredSourceName()), true);
    }

    private static void applyPagodaBuff(final Player caster, final int ringIndex) {
        final PagodaBuff buff = pagodaBuffFor(ringIndex);
        final Player target = supportTarget(caster);
        caster.displayClientMessage(Component.translatable("soulland.cultivation.pagoda.buffed",
                Component.translatable(buff.translationKey()), target.getDisplayName()), true);
        switch (buff) {
            case DAMAGE -> bless(target, SoulLand.DAMAGE_BLESSING_EFFECT, FIRST_LEVEL);
            case DEFENSE -> bless(target, SoulLand.DEFENSE_BLESSING_EFFECT, FIRST_LEVEL);
            case HEAL -> target.heal((float) (target.getMaxHealth() * PAGODA_HEAL_PERCENT));
            case BERSERK -> bless(target, SoulLand.BERSERK_EFFECT, FIRST_LEVEL);
            case GREATER_BERSERK -> bless(target, SoulLand.BERSERK_EFFECT, SECOND_LEVEL);
            case GREATEST_BERSERK -> bless(target, SoulLand.BERSERK_EFFECT, THIRD_LEVEL);
        }
    }

    /** The blessing is an effect, so the target sees it, its level and its time left. */
    private static void bless(final Player target, final Holder<MobEffect> blessing, final int level) {
        target.addEffect(new MobEffectInstance(blessing, BUFF_DURATION_TICKS, level));
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
