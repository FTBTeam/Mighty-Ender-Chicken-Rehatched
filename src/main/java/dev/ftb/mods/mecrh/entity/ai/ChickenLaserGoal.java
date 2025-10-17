package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.entity.EnderChicken;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class ChickenLaserGoal extends ChickenGoal {
    public static final int WARMUP_TIME = 59;
    public static final int LASER_DURATION = 160;

    private Vec3 beamPathVector;
    private Vec3 targetStart;

    public ChickenLaserGoal(EnderChicken chicken) {
        super(chicken);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        return super.canUse()
                && !chicken.isFiringLaser()
                && chicken.getTarget() != null && chicken.getTarget().isAlive()
                && chicken.isLaserReady(chicken.getTarget().getY() > chicken.partBody.getY() + (double) chicken.partBody.getBbHeight());
    }

    @Override
    public boolean canContinueToUse() {
        return chicken.isFiringLaser() && chicken.isAlive() && chicken.getFiringProgress() < LASER_DURATION;
    }

    @Override
    public void start() {
        chicken.useAbility();
        chicken.setFiringLaser(true);

        LivingEntity target = chicken.getTarget();
        if (target != null) {
            targetStart = target.position().add(0.0, target.getBbHeight() / 2.0F, 0.0);
        }
    }

    @Override
    public void stop() {
        chicken.endAbility();
        chicken.setFiringLaser(false);
        targetStart = null;
        beamPathVector = null;
    }

    @Override
    public void tick() {
        LivingEntity target = chicken.getTarget();

        if (chicken.getFiringProgress() == 1) {
            if (target != null) {
                targetStart = target.position().add(0.0, target.getBbHeight() / 2.0F, 0.0);
            }
        } else if (chicken.getFiringProgress() < WARMUP_TIME) {
            if (targetStart != null) {
                chicken.getLookControl().setLookAt(targetStart);
            }
        } else if (chicken.getFiringProgress() == WARMUP_TIME) {
            double maxRange = 5.0;
            Vec3 targetEnd = null;
            if (target != null) {
                targetEnd = target.position().add(0.0, target.getBbHeight() / 2.0F, 0.0);
                double dist = chicken.distanceTo(target);
                if (dist < maxRange) {
                    maxRange = dist;
                }
            }

            boolean startNull = targetStart == null;
            RandomSource rnd = chicken.getRandom();
            if (startNull) {
                targetStart = Objects.requireNonNullElseGet(targetEnd, chicken::position).add(randomBeamOffset(rnd));
            }

            if (targetEnd == null) {
                if (!startNull) {
                    targetEnd = targetStart.add(randomBeamOffset(rnd));
                } else {
                    targetEnd = chicken.position().add(randomBeamOffset(rnd));
                }
            }

            if (targetEnd.equals(targetStart)) {
                targetEnd = targetEnd.add(randomBeamOffset(rnd));
            }

            Vec3 normVec = targetEnd.subtract(targetStart).normalize();
            double regress = rnd.nextDouble() * maxRange;
            targetStart = targetStart.subtract(normVec.x * regress, normVec.y * regress, normVec.z * regress);
            double progress = rnd.nextDouble() * maxRange;
            targetEnd = targetEnd.add(normVec.x * progress, normVec.y * progress, normVec.z * progress);
            beamPathVector = targetEnd.subtract(targetStart);
            targetStart = targetStart.subtract(chicken.position());
        }

        if (chicken.getFiringProgress() > WARMUP_TIME && chicken.getFiringProgress() < LASER_DURATION) {
            if (target != null) {
                double chickenHeadTop = chicken.partHead.getY() + chicken.partHead.getBbHeight();
                if (target.getY() > chickenHeadTop) {
                    chicken.getLookControl().setLookAt(target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ());
                    return;
                }
            }

            Vec3 beamTarget = chicken.position().add(targetStart).add(beamPathVector.scale(beamProgress()));
            chicken.getLookControl().setLookAt(beamTarget.x, beamTarget.y, beamTarget.z, chicken.getHeadRotSpeed(), 45);
        }
    }

    private Vec3 randomBeamOffset(RandomSource rnd) {
        return new Vec3(rnd.nextGaussian() * 20.0, rnd.nextGaussian() * 8.0, rnd.nextGaussian() * 20.0);
    }

    private double beamProgress() {
        double start = WARMUP_TIME + 1.0;
        return (chicken.getFiringProgress() - start) / (LASER_DURATION - start);
    }
}
