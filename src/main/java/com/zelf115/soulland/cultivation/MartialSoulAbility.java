package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.SoulLand;
import com.zelf115.soulland.Stats;
import net.minecraft.world.entity.player.Player;
import java.util.Comparator;

public final class MartialSoulAbility {
    private static final int TICKS_PER_SECOND = 20;
    private static final int TICKS_PER_MINUTE = 1200;
    private static final double ENERGY_COST_PER_SECOND = 1.0;
    private static final String TEMPORARY_BONUS_ID = "soulland_martial_soul_buff";

    private MartialSoulAbility() {
    }

    public static void toggle(final Player player, final CultivationData data) {
        if (data.getMartialSoul() == null) return;
        if (data.isMartialSoulActive()) {
            if (data.getMartialSoul().category() != MartialSoul.Category.TOOL) {
                Stats.removeTemporaryBonus(player, TEMPORARY_BONUS_ID);
            }
            data.setMartialSoulActive(false);
            return;
        }
        if (data.getSpiritEnergy() >= ENERGY_COST_PER_SECOND) {
            data.setMartialSoulActive(true);
            if (data.getMartialSoul() == MartialSoul.CLEAR_SKY_HAMMER) {
                player.getInventory().add(SoulLand.CLEAR_SKY_HAMMER.get().getDefaultInstance());
            } else if (data.getMartialSoul() == MartialSoul.SEVEN_KILL_SWORD) {
                player.getInventory().add(SoulLand.SEVEN_KILL_SWORD.get().getDefaultInstance());
            }
            if (data.getMartialSoul().category() == MartialSoul.Category.BEAST
                    || data.getMartialSoul().category() == MartialSoul.Category.BODY) {
                Stats.applyTemporaryStatPercentBonus(player, TEMPORARY_BONUS_ID, 0.10);
            }
        }
    }

    public static void tick(final Player player, final CultivationData data, final long gameTick) {
        if (data.isMartialSoulActive() && gameTick % TICKS_PER_SECOND == 0) {
            data.setSpiritEnergy(data.getSpiritEnergy() - ENERGY_COST_PER_SECOND);
            if (data.getSpiritEnergy() <= 0.0) data.setMartialSoulActive(false);
        }
        if (data.getMartialSoulBuffUntil() > 0 && gameTick >= data.getMartialSoulBuffUntil()) {
            Stats.removeTemporaryBonus(player, TEMPORARY_BONUS_ID);
            data.setMartialSoulBuffUntil(0L);
        }
    }

    public static void cast(final Player player, final CultivationData data) {
        if (!data.isMartialSoulActive()) return;
        final Player target = player.level().getEntitiesOfClass(Player.class,
                player.getBoundingBox().inflate(15.0), candidate -> candidate != player
                    && player.hasLineOfSight(candidate)).stream()
            .min(Comparator.comparingDouble(player::distanceToSqr)).orElse(player);
        final int levelTens = data.getLevel() / 10;
        if (data.getMartialSoul() == MartialSoul.NINE_HEART_BEGONIA) {
            target.heal(target.getMaxHealth() * 0.10F * levelTens);
        } else if (data.getMartialSoul() == MartialSoul.SEVEN_TREASURE_GLAZED_TILE_PAGODA) {
            final CultivationData targetData = target.getData(CultivationAttachment.CULTIVATION_DATA.get());
            Stats.applyTemporaryPercentBonus(target, TEMPORARY_BONUS_ID, 0.02 * levelTens);
            targetData.setMartialSoulBuffUntil(player.level().getGameTime() + 5 * TICKS_PER_MINUTE);
        }
    }
}
