package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.registry.ModSounds;

import java.util.EnumSet;

public class ChickenSpinGoal extends ChickenGoal {
    public static final int MAX_SPIN_TIME = 100;
    private int spinTicks;

    public ChickenSpinGoal(EnderChicken chicken) {
        super(chicken);

        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (chicken.canUseAbility() && !chicken.isClearingArea() && !chicken.isSpinning() && chicken.isSpinReady() && !chicken.isFiringLaser()) {
            return chicken.getTarget() != null && chicken.getTarget().isAlive();
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return chicken.isAlive() && spinTicks < MAX_SPIN_TIME;
    }

    @Override
    public void start() {
        chicken.useAbility();
        spinTicks = 0;
        chicken.setSpinning(true);
    }

    @Override
    public void stop() {
        chicken.endAbility();
        chicken.setSpinning(false);
    }

    @Override
    public void tick() {
        ++spinTicks;

        if (spinTicks == 10) {
            chicken.playSound(ModSounds.CHICKEN_SPIN.get(), 1.0F, 1.0F);
        }

        if (spinTicks > 10) {
            float eggChance = spinTicks / (MAX_SPIN_TIME * 0.5F);
            if (chicken.getRandom().nextFloat() < eggChance) {
                chicken.launchEggBomb(ServerConfig.getEggSpeed(chicken.getRandom()), chicken.getRandom().nextBoolean());
            }
        }

        ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partWingL.getBlockDestructionAABB());
        ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partWingR.getBlockDestructionAABB());
        ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partHead.getBlockDestructionAABB());
        ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partBill.getBlockDestructionAABB());
    }
}
