package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChickenEntity;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class ChickenLaserGoal extends ChickenSkillGoal {
    private Vec3 lookVector;
    private Vec3 targetStart;

    public ChickenLaserGoal(EnderChickenEntity chicken) {
        super(chicken, ServerConfig.LASER_CHANCE.get());
    }

    @Override
    public boolean canUse() {
        if (!chicken.isFiring() && chicken.getFiringProgress() == 0) {
            double currentChance = chanceToUse;
            if (chicken.isHeadAvailable()) {
                var target = chicken.getTarget();
                if (target == null || !target.isAlive()) {
                    return false;
                }

                if (target.getY() > chicken.partBody.getY() + (double) chicken.partBody.getBbHeight()) {
                    currentChance *= 2.5F;
                }
            }

            return chicken.canUseAbility() && chicken.getRandom().nextFloat() < currentChance;
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return chicken.isFiring() && chicken.isAlive();
    }

    @Override
    public void start() {
        chicken.useAbility();
        chicken.setFiring(true);
    }

    @Override
    public void stop() {
        chicken.endAbility();
        targetStart = null;
        lookVector = null;
    }

    @Override
    public void tick() {
        double regress;

        LivingEntity target = chicken.getTarget();

        if (chicken.getFiringProgress() == 1) {
            if (chicken.isHeadAvailable()) {
                if (target != null) {
                    targetStart = target.position().add(0.0, target.getBbHeight() / 2.0F, 0.0);
                }
            }
        } else if (chicken.getFiringProgress() < 59) {
            if (chicken.isHeadAvailable() && targetStart != null) {
                chicken.getLookControl().setLookAt(targetStart.x, targetStart.y, targetStart.z, 2.0F, 3.0F);
            }
        } else if (chicken.getFiringProgress() == 59) {
            double maxRange = 5.0;
            Vec3 targetEnd = null;
            if (chicken.isHeadAvailable() && target != null) {
                targetEnd = target.position().add(0.0, target.getBbHeight() / 2.0F, 0.0);
                double dist = chicken.distanceTo(target);
                if (dist < maxRange) {
                    maxRange = dist;
                }
            }

            boolean startNull = targetStart == null;
            RandomSource rnd = chicken.getRandom();
            if (startNull) {
                targetStart = Objects.requireNonNullElseGet(targetEnd, chicken::position)
                        .add(rnd.nextGaussian() * 8.0, rnd.nextGaussian() * 8.0, rnd.nextGaussian() * 8.0);
            }

            if (targetEnd == null) {
                if (!startNull) {
                    targetEnd = targetStart.add(rnd.nextGaussian() * 8.0, rnd.nextGaussian() * 8.0, rnd.nextGaussian() * 8.0);
                } else {
                    targetEnd = chicken.position().add(rnd.nextGaussian() * 8.0, rnd.nextGaussian() * 8.0, rnd.nextGaussian() * 8.0);
                }
            }

            if (targetEnd.equals(targetStart)) {
                targetEnd = targetEnd.add(rnd.nextGaussian() * 8.0, rnd.nextGaussian() * 8.0, rnd.nextGaussian() * 8.0);
            }

            Vec3 normVec = targetEnd.subtract(targetStart).normalize();
            regress = rnd.nextDouble() * maxRange;
            targetStart = targetStart.subtract(normVec.x * regress, normVec.y * regress, normVec.z * regress);
            double progress = rnd.nextDouble() * maxRange;
            targetEnd = targetEnd.add(normVec.x * progress, normVec.y * progress, normVec.z * progress);
            lookVector = targetEnd.subtract(targetStart);
            targetStart = targetStart.subtract(chicken.position());
        }

        if (chicken.getFiringProgress() >= 59 && chicken.getFiringProgress() < 160) {
            if (chicken.isHeadAvailable() && target != null) {
                double chickenHeadTop = chicken.partHead.getY() + (double) chicken.partHead.getBbHeight();
                if (target.getY() > chickenHeadTop) {
                    chicken.getLookControl().setLookAt(target.getX(), target.getY() + (target.getBoundingBox().maxY - target.getBoundingBox().minY) / 2.0, target.getZ(), chicken.getMaxHeadXRot(), chicken.getMaxHeadYRot());
                    return;
                }
            }

            float prog = (float) (chicken.getFiringProgress() - 60) / 100.0F;
            double x = chicken.getX() + targetStart.x + lookVector.x * (double) prog;
            double y = chicken.getY() + targetStart.y + lookVector.y * (double) prog;
            regress = chicken.getZ() + targetStart.z + lookVector.z * (double) prog;
            chicken.getLookControl().setLookAt(x, y, regress, chicken.getMaxHeadYRot(), chicken.getMaxHeadYRot());
        }
    }
}
