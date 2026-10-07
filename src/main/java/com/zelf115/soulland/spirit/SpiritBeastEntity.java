package com.zelf115.soulland.spirit;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class SpiritBeastEntity extends Monster {

    private static final EntityDataAccessor<Integer> TIER =
            SynchedEntityData.defineId(SpiritBeastEntity.class, EntityDataSerializers.INT);

    private final Optional<ServerBossEvent> bossBar;

    public SpiritBeastEntity(final EntityType<? extends Monster> entityType, final Level level) {
        super(entityType, level);
        this.bossBar = SpiritBosses.isBoss(entityType)
                ? Optional.of(new ServerBossEvent(entityType.getDescription(),
                        BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS))
                : Optional.empty();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    /**
     * Monsters value dark ground and refuse to spawn on lit ground; spirit beasts roam by day as well
     * as by night, so every position is worth the same to them.
     */
    @Override
    public float getWalkTargetValue(final BlockPos pos, final LevelReader level) {
        return 0.0F;
    }

    /** Left empty on purpose: monsters age toward despawning faster in daylight, and spirit beasts must not. */
    @Override
    protected void updateNoActionTime() {
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TIER, 1);
    }

    /** Publishes the rolled tier so the client can colour the beast by the ring it will drop. */
    public void syncTier(final int tier) {
        this.entityData.set(TIER, tier);
        bossBar.ifPresent(bar -> bar.setName(getDisplayName()));
    }

    @Override
    public void startSeenByPlayer(final ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.ifPresent(bar -> bar.addPlayer(player));
    }

    @Override
    public void stopSeenByPlayer(final ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.ifPresent(bar -> bar.removePlayer(player));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossBar.ifPresent(bar -> bar.setProgress(getHealth() / getMaxHealth()));
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
        this.goalSelector.addGoal(2, new SpiritBeastMeleeGoal(this));
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
