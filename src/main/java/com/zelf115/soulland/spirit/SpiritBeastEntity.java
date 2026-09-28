package com.zelf115.soulland.spirit;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SpiritBeastEntity extends Monster {

    private static final EntityDataAccessor<Integer> TIER =
            SynchedEntityData.defineId(SpiritBeastEntity.class, EntityDataSerializers.INT);

    public SpiritBeastEntity(final EntityType<? extends Monster> entityType, final Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TIER, 1);
    }

    /** Publishes the rolled tier so the client can colour the beast by the ring it will drop. */
    public void syncTier(final int tier) {
        this.entityData.set(TIER, tier);
    }

    public int getTier() {
        return this.entityData.get(TIER);
    }

    /**
     * A cultivator sizes up a beast before committing to the fight, so the name tag hangs over
     * every spirit beast rather than only over the ones someone has named.
     */
    @Override
    public boolean shouldShowName() {
        return true;
    }

    /** Names the beast in the colour of the soul ring it will drop, so players can judge a fight. */
    @Override
    public Component getDisplayName() {
        return super.getDisplayName().copy()
                .withStyle(style -> style.withColor(SpiritBeastManager.tierTextColor(getTier())));
    }

    @Override
    protected void registerGoals() {
        registerMovementGoals();
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    protected void registerMovementGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8D));
    }
}
