package com.zelf115.soulland.cultivation.skill;

import com.zelf115.soulland.SoulLand;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The entity types skills launch into the world. */
public final class SkillEntities {

    private static final String SKILL_PROJECTILE_ID = "skill_projectile";
    private static final float PROJECTILE_SIZE = 0.3F;
    private static final int TRACKING_RANGE = 4;
    private static final int UPDATE_INTERVAL = 10;

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, SoulLand.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<SkillProjectileEntity>> SKILL_PROJECTILE =
            ENTITY_TYPES.register(SKILL_PROJECTILE_ID,
                    () -> EntityType.Builder.<SkillProjectileEntity>of(SkillProjectileEntity::new, MobCategory.MISC)
                            .sized(PROJECTILE_SIZE, PROJECTILE_SIZE)
                            .clientTrackingRange(TRACKING_RANGE)
                            .updateInterval(UPDATE_INTERVAL)
                            .build(SoulLand.MODID + ":" + SKILL_PROJECTILE_ID));

    private SkillEntities() {
    }

    public static void register(final IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
