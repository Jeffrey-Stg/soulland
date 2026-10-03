package com.zelf115.soulland.spirit;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Lets spirit beasts appear in the world on their own.
 *
 * <p>Which biome holds which beast is handled elsewhere; every beast uses the standard monster rules
 * here, sea beasts on the ocean floor and the rest on dry ground, so that the biome modifiers can
 * place them at all.
 */
@EventBusSubscriber(modid = SoulLand.MODID)
public final class SpiritBeastSpawns {

    private static final double BOSS_EXCLUSION_RADIUS = 128.0;

    private SpiritBeastSpawns() {
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(final RegisterSpawnPlacementsEvent event) {
        for (final var spiritBeastType : SpiritBeastEntities.ALL) {
            if (SpiritBeastEntities.AMPHIBIOUS.contains(spiritBeastType)) {
                registerInWater(event, spiritBeastType.get());
            } else {
                registerOnGround(event, spiritBeastType.get());
            }
        }
    }

    private static void registerOnGround(
            final RegisterSpawnPlacementsEvent event, final EntityType<SpiritBeastEntity> spiritBeastType) {
        event.register(spiritBeastType,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                aloneIfBoss(spiritBeastType, Monster::checkMonsterSpawnRules),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static void registerInWater(
            final RegisterSpawnPlacementsEvent event, final EntityType<SpiritBeastEntity> spiritBeastType) {
        event.register(spiritBeastType,
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.OCEAN_FLOOR,
                aloneIfBoss(spiritBeastType, SpiritBeastSpawns::checkInWaterSpawnRules),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    /** A boss never spawns while another of its kind is still alive nearby. */
    private static SpawnPlacements.SpawnPredicate<SpiritBeastEntity> aloneIfBoss(
            final EntityType<SpiritBeastEntity> spiritBeastType, final SpawnPlacements.SpawnPredicate<SpiritBeastEntity> rules) {
        if (!SpiritBosses.isBoss(spiritBeastType)) return rules;
        return (type, level, spawnType, pos, random) ->
                rules.test(type, level, spawnType, pos, random) && hasNoLivingTwin(type, level, pos);
    }

    private static boolean hasNoLivingTwin(
            final EntityType<SpiritBeastEntity> bossType, final ServerLevelAccessor level, final BlockPos pos) {
        return level.getEntitiesOfClass(SpiritBeastEntity.class, new AABB(pos).inflate(BOSS_EXCLUSION_RADIUS),
                beast -> beast.getType() == bossType).isEmpty();
    }

    private static boolean checkInWaterSpawnRules(
            final EntityType<SpiritBeastEntity> spiritBeastType,
            final ServerLevelAccessor level,
            final MobSpawnType spawnType,
            final BlockPos pos,
            final RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.WATER)
                && Monster.checkMonsterSpawnRules(spiritBeastType, level, spawnType, pos, random);
    }
}
