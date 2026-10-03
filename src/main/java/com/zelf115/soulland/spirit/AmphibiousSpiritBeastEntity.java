package com.zelf115.soulland.spirit;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;

/** A sea beast that hunts in deep water but can follow its prey onto land and back. */
public class AmphibiousSpiritBeastEntity extends SpiritBeastEntity {

    private static final int MAX_PITCH_TURN = 85;
    private static final int MAX_YAW_TURN = 10;
    private static final float IN_WATER_SPEED_MODIFIER = 0.08F;
    private static final float ON_LAND_SPEED_MODIFIER = 1.0F;
    /** Swimming momentum kept each tick, the same drag vanilla swimmers use. */
    private static final double WATER_DRAG = 0.9D;
    private static final int SWIM_WANDER_INTERVAL = 40;
    /** The swimming move control never jumps, so stepping is the only way up a shore or ledge, as for the axolotl. */
    private static final double LAND_STEP_HEIGHT = 1.0D;

    public AmphibiousSpiritBeastEntity(final EntityType<? extends Monster> entityType, final Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(LAND_STEP_HEIGHT);
        this.moveControl = new SmoothSwimmingMoveControl(
                this, MAX_PITCH_TURN, MAX_YAW_TURN, IN_WATER_SPEED_MODIFIER, ON_LAND_SPEED_MODIFIER, false);
    }

    @Override
    protected void registerMovementGoals() {
        this.goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.0D, SWIM_WANDER_INTERVAL));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8D));
    }

    @Override
    protected PathNavigation createNavigation(final Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    @Override
    public boolean isPushedByFluid(final FluidType fluidType) {
        return false;
    }

    /** The default check refuses any spawn space holding liquid, which would forbid every sea spawn. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return level.isUnobstructed(this);
    }

    @Override
    public void travel(final Vec3 travelVector) {
        if (!this.isControlledByLocalInstance() || !this.isInWater()) {
            super.travel(travelVector);
            return;
        }
        this.moveRelative(this.getSpeed(), travelVector);
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(WATER_DRAG));
    }
}
