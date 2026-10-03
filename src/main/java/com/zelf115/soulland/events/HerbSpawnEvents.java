package com.zelf115.soulland.events;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Wild spirit herbs sprout around cultivators who walk the mod's biomes: once a second each player
 * has a small chance of an herb appearing nearby, until enough already grow around them.
 */
@EventBusSubscriber(modid = SoulLand.MODID)
public final class HerbSpawnEvents {

    public static final TagKey<Biome> HERB_BIOMES =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, "herb_biomes"));

    private static final int TICKS_PER_ROLL = 20;
    private static final float SPAWN_CHANCE = 0.05F;
    private static final int RADIUS = 16;
    private static final int VERTICAL_RANGE = 8;
    private static final int NEARBY_CAP = 10;
    private static final int PLACEMENT_ATTEMPTS = 8;

    private HerbSpawnEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }

        final ServerLevel level = player.serverLevel();
        final BlockPos center = player.blockPosition();
        // Offset by the player id so every player's roll does not land on the same tick.
        if ((level.getGameTime() + player.getId()) % TICKS_PER_ROLL != 0) return;
        if (!level.getBiome(center).is(HERB_BIOMES)) return;
        if (level.random.nextFloat() >= SPAWN_CHANCE) return;
        if (countNearbyHerbs(level, center) >= NEARBY_CAP) return;

        trySpawnHerb(level, center, level.random);
    }

    private static int countNearbyHerbs(final ServerLevel level, final BlockPos center) {
        int count = 0;
        for (final BlockPos pos : BlockPos.betweenClosed(
                center.offset(-RADIUS, -VERTICAL_RANGE, -RADIUS), center.offset(RADIUS, VERTICAL_RANGE, RADIUS))) {
            if (level.getBlockState(pos).is(SoulLand.SPIRIT_HERB.get()) && ++count >= NEARBY_CAP) {
                return count;
            }
        }
        return count;
    }

    private static void trySpawnHerb(final ServerLevel level, final BlockPos center, final RandomSource random) {
        final BlockState herb = SoulLand.SPIRIT_HERB.get().defaultBlockState();
        for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
            final BlockPos columnTop = center.offset(
                    random.nextIntBetweenInclusive(-RADIUS, RADIUS), VERTICAL_RANGE,
                    random.nextIntBetweenInclusive(-RADIUS, RADIUS));
            final BlockPos spot = findSpot(level, columnTop, herb);
            if (spot != null) {
                level.setBlockAndUpdate(spot, herb);
                return;
            }
        }
    }

    /**
     * The highest free block in the column near the player where the herb can root. Walking down
     * from just above the player, rather than reading a heightmap, also works under the Nether and
     * End ceilings.
     */
    private static BlockPos findSpot(final ServerLevel level, final BlockPos columnTop, final BlockState herb) {
        if (!level.isLoaded(columnTop)) {
            return null;
        }

        final BlockPos.MutableBlockPos pos = columnTop.mutable();
        for (int step = 0; step <= 2 * VERTICAL_RANGE; step++, pos.move(Direction.DOWN)) {
            if (level.isEmptyBlock(pos) && herb.canSurvive(level, pos) && level.getBiome(pos).is(HERB_BIOMES)) {
                return pos.immutable();
            }
        }
        return null;
    }
}
