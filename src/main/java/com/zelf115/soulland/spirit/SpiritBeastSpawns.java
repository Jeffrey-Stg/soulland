package com.zelf115.soulland.spirit;

import com.zelf115.soulland.SoulLand;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Lets spirit beasts appear in the world on their own.
 *
 * <p>Which biome holds which beast is issue #25; every beast uses the standard monster rules here
 * so that the biome modifier can place them at all.
 */
@EventBusSubscriber(modid = SoulLand.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class SpiritBeastSpawns {

    private SpiritBeastSpawns() {
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(final RegisterSpawnPlacementsEvent event) {
        for (final var spiritBeastType : SpiritBeastEntities.ALL) {
            event.register(spiritBeastType.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Monster::checkMonsterSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }
}
