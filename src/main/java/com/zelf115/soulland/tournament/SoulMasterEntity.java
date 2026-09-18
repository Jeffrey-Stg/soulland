package com.zelf115.soulland.tournament;

import com.zelf115.soulland.DerivedStats;
import net.minecraft.core.Holder;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** A tournament opponent: a soul master simulated at a level near the challenger's own. */
public class SoulMasterEntity extends Monster {

    private static final EntityDataAccessor<Integer> SIMULATED_LEVEL =
            SynchedEntityData.defineId(SoulMasterEntity.class, EntityDataSerializers.INT);

    private static final String CHALLENGER_KEY = "soulland_tournament_challenger";
    private static final String ROUND_KEY = "soulland_tournament_round";

    private static final double BASE_MAX_HEALTH = 20.0;
    private static final double BASE_ATTACK_DAMAGE = 2.0;
    private static final double BASE_ARMOR = 0.0;
    private static final double BASE_MOVEMENT_SPEED = 0.3;
    private static final double FOLLOW_RANGE = 32.0;
    private static final int STARTING_LEVEL = 1;
    private static final float LOOK_DISTANCE = 12.0F;

    public SoulMasterEntity(final EntityType<? extends Monster> entityType, final Level level) {
        super(entityType, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE)
                .add(Attributes.ARMOR, BASE_ARMOR)
                .add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SIMULATED_LEVEL, STARTING_LEVEL);
    }

    /** Raises the opponent to the level it fights at, on the same stat curve a cultivator climbs. */
    public void prepareForDuel(final int simulatedLevel, final UUID challengerId, final int round) {
        entityData.set(SIMULATED_LEVEL, simulatedLevel);
        getPersistentData().putUUID(CHALLENGER_KEY, challengerId);
        getPersistentData().putInt(ROUND_KEY, round);

        final double stat = SoulMasterStats.statPointsForLevel(simulatedLevel);
        setAttributeBase(Attributes.MAX_HEALTH, DerivedStats.maxHealth(BASE_MAX_HEALTH, stat));
        setAttributeBase(Attributes.ATTACK_DAMAGE, DerivedStats.attackDamage(BASE_ATTACK_DAMAGE, stat));
        setAttributeBase(Attributes.ARMOR, DerivedStats.armor(BASE_ARMOR, stat));
        setHealth(getMaxHealth());
    }

    private void setAttributeBase(final Holder<Attribute> attribute, final double value) {
        final var instance = getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    public int getSimulatedLevel() {
        return entityData.get(SIMULATED_LEVEL);
    }

    public UUID getChallengerId() {
        return getPersistentData().hasUUID(CHALLENGER_KEY) ? getPersistentData().getUUID(CHALLENGER_KEY) : null;
    }

    public int getRound() {
        return getPersistentData().getInt(ROUND_KEY);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.soulland.soul_master.named", getSimulatedLevel());
    }

    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, LOOK_DISTANCE));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
}
