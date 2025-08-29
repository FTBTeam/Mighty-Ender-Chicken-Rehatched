package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EggBomb;
import dev.ftb.mods.mecrh.entity.EnderChickenEntity;
import dev.ftb.mods.mecrh.registry.ModEntityTypes;
import dev.ftb.mods.mecrh.registry.ModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ChickenSpinGoal extends ChickenSkillGoal {
    private int time;

    public ChickenSpinGoal(EnderChickenEntity chicken) {
        super(chicken, ServerConfig.SPIN_CHANCE.get());
    }

    @Override
    public boolean canUse() {
        if (!chicken.isClearingArea() && !chicken.isSpinning()) {
            var target = chicken.getTarget();
            if (target != null && target.isAlive()) {
                return chicken.canUseAbility() && chicken.getEggState() < 0 && !chicken.isFiring() && chicken.isAlive() && chicken.getRandom().nextFloat() < chanceToUse;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return chicken.isAlive() && time < 140;
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

        if (time == 1) {
            chicken.playSound(ModSounds.ENDER_DEATH.get(), 0.3F * chicken.getScale(), 1.0F);
        }

        if (time > 10) {
            float eggChance = time / 140.0F * 0.5F;
            if (chicken.getRandom().nextFloat() < eggChance) {
                chicken.launchEggBomb(0.14f);
            }
        }

        Vec3 motion = chicken.getDeltaMovement();
        ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partWingL.getBoundingBox().expandTowards(motion.x, motion.y, motion.z).inflate(chicken.partWingL.getBbWidth() / 3.0));
        ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partWingR.getBoundingBox().expandTowards(motion.x, motion.y, motion.z).inflate(chicken.partWingR.getBbWidth() / 3.0));
        if (chicken.isHeadAvailable()) {
            ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partHead.getBoundingBox().expandTowards(motion.x, motion.y, motion.z).inflate(chicken.partHead.getBbWidth() / 3.0));
            ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partBill.getBoundingBox().expandTowards(motion.x, motion.y, motion.z).inflate(chicken.partBill.getBbWidth() / 3.0));
        }
    }
}
