package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.entity.EnderChickenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ChickenWanderGoal extends Goal {
    private final EnderChickenEntity chicken;
    private Vec3 targetPos;
    private int stillTime;
    private int randomStillTime;
    private int collisionTime;
    private final double widthSq;

    public ChickenWanderGoal(EnderChickenEntity chicken) {
        this.chicken = chicken;

        setFlags(EnumSet.of(Flag.MOVE));
        widthSq = Math.pow(chicken.getBbWidth() * 0.6, 2);
    }

    @Override
    public boolean canUse() {
        return chicken.isAlive();
    }

    @Override
    public void tick() {
        if (targetPos == null) {
            ++stillTime;
            if (stillTime > 60 + randomStillTime) {
                findNewTargetPos();
            }
        } else {
            stillTime = 0;
            double dstY = Math.round(targetPos.y);
            chicken.getMoveControl().setWantedPosition(targetPos.x, dstY, targetPos.z, 0.2 * chicken.getScale());
            if (chicken.distanceToSqr(targetPos.x, dstY, targetPos.z) < widthSq) {
                targetPos = null;
                randomStillTime = chicken.getRandom().nextInt(40);
            }

            if (chicken.horizontalCollision) {
                ++collisionTime;
                if (collisionTime > 5) {
                    targetPos = null;
                    randomStillTime = chicken.getRandom().nextInt(40);
                }
            }
        }
    }

    public void findNewTargetPos() {
        double origX = chicken.getX();
        double origY = chicken.getY();
        double origZ = chicken.getZ();
        float scale = chicken.getScale();
        double newX = chicken.getX() + (chicken.getRandom().nextDouble() - 0.5) * 64.0 / 10.0 * (double) scale;
        double newY = chicken.getY() + (double) (chicken.getRandom().nextInt(64) - 32) / 10.0 * (double) scale;
        double newZ = chicken.getZ() + (chicken.getRandom().nextDouble() - 0.5) * 64.0 / 10.0 * (double) scale;

        boolean canMove = false;
        BlockPos blockpos = BlockPos.containing(newX, newY, newZ);
        if (chicken.level() instanceof ServerLevel level && level.isLoaded(blockpos)) {
            boolean foundBlock = false;

            while (!foundBlock && blockpos.getY() > 0) {
                BlockPos blockpos1 = blockpos.below();
                BlockState iblockstate = level.getBlockState(blockpos1);
                if (iblockstate.isFaceSturdy(level, blockpos1, Direction.UP)) {
                    foundBlock = true;
                } else {
                    --newY;
                    blockpos = blockpos1;
                }
            }

            if (foundBlock) {
                setChickenPosition(newX, newY, newZ);
                if (level.noCollision(chicken.getBoundingBox()) && !level.containsAnyLiquid(chicken.getBoundingBox())) {
                    canMove = true;
                }
            }
        }

        if (canMove) {
            targetPos = new Vec3(newX, newY, newZ);
        }

        setChickenPosition(origX, origY, origZ);
    }

    public void setChickenPosition(double x, double y, double z) {
        chicken.setPos(new Vec3(x, y, z));
        float w = chicken.getBbWidth() / 2.0F;
        float h = chicken.getBbHeight();
        chicken.setBoundingBox(new AABB(x - w, y, z - w, x + w, y + h, z + w));
    }
}
