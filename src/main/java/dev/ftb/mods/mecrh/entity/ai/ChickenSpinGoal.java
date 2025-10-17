package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.registry.ModSounds;

import java.util.EnumSet;

public class ChickenSpinGoal extends ChickenGoal {
    private int time;

    public ChickenSpinGoal(EnderChicken chicken) {
        super(chicken);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (chicken.canUseAbility() && !chicken.isClearingArea() && !chicken.isSpinning() && chicken.isSpinReady()) {
            var target = chicken.getTarget();
            if (target != null && target.isAlive()) {
                return chicken.canUseAbility() && !chicken.isFiringLaser() && chicken.isAlive();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return chicken.isAlive() && time < 100;
    }

    @Override
    public void start() {
        chicken.useAbility();
        time = 0;
        chicken.setSpinning(true);
    }

    @Override
    public void stop() {
        chicken.endAbility();
        chicken.setSpinning(false);
    }

    @Override
    public void tick() {
        ++time;

        if (time == 10) {
            chicken.playSound(ModSounds.CHICKEN_SPIN.get(), 1.0F, 1.0F);
        }

        if (time > 10) {
            float eggChance = time / 100.0F * 0.5F;
            if (chicken.getRandom().nextFloat() < eggChance) {
                chicken.launchEggBomb(ServerConfig.getEggSpeed(chicken.getRandom()));
            }
        }

        ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partWingL.getBlockDestructionAABB());
        ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partWingR.getBlockDestructionAABB());
        ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partHead.getBlockDestructionAABB());
        ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partBill.getBlockDestructionAABB());
    }
}
