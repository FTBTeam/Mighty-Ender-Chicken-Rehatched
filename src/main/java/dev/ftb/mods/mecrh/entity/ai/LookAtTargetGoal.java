package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.entity.EnderChickenEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class LookAtTargetGoal extends Goal {
    public EnderChickenEntity chicken;

    public LookAtTargetGoal(EnderChickenEntity chicken) {
        this.chicken = chicken;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return chicken.isHeadAvailable() && chicken.getTarget() != null
                && !chicken.isFiring() && !chicken.isCharging()
                && !chicken.isSpinning() && chicken.isAlive();
    }

    @Override
    public void tick() {
        Entity ent = chicken.getTarget();
        if (ent != null) {
            chicken.getLookControl().setLookAt(ent.getX(), ent.getY() + ent.getEyeHeight(), ent.getZ(), chicken.getMaxHeadXRot(), chicken.getMaxHeadYRot());
        }
    }
}
