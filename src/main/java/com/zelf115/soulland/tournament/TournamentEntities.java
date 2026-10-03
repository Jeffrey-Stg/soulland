package com.zelf115.soulland.tournament;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The entity types the tournament spawns; none of them spawn naturally. */
public final class TournamentEntities {

    private static final String SOUL_MASTER_ID = "soul_master";
    private static final float WIDTH = 0.6F;
    private static final float HEIGHT = 1.8F;
    private static final int TRACKING_RANGE = 10;

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, SoulLand.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<SoulMasterEntity>> SOUL_MASTER =
            ENTITY_TYPES.register(SOUL_MASTER_ID,
                    () -> EntityType.Builder.<SoulMasterEntity>of(SoulMasterEntity::new, MobCategory.MONSTER)
                            .sized(WIDTH, HEIGHT)
                            .clientTrackingRange(TRACKING_RANGE)
                            .build(SoulLand.MODID + ":" + SOUL_MASTER_ID));

    private TournamentEntities() {
    }

    public static void register(final IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
