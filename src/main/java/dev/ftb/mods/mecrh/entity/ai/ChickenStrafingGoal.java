package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import net.minecraft.world.entity.LivingEntity;

public class ChickenStrafingGoal extends ChickenGoal {
    private int strafingRunTime;
    private int eggCooldown;

    public ChickenStrafingGoal(EnderChicken chicken) {
        super(chicken);
    }

    @Override
    public boolean canUse() {
        if (chicken.canUseAbility() && chicken.getMaxStrafingRunTime() <= 0) {
            LivingEntity target = chicken.getTarget();
            if (target != null && target.isAlive()) {
                return chicken.canUseAbility();
            } else {
                return false;
            }
        } else {
            chicken.setMaxStrafingRunTime(chicken.getMaxStrafingRunTime() - 1);
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return strafingRunTime < chicken.getMaxStrafingRunTime() && chicken.isAlive();
    }

    @Override
    public void start() {
        chicken.useAbility();
        int min = Math.min(ServerConfig.STRAFE_MIN_TIME.get(), ServerConfig.STRAFE_MAX_TIME.get());
        int max = Math.max(ServerConfig.STRAFE_MIN_TIME.get(), ServerConfig.STRAFE_MAX_TIME.get());
        chicken.setMaxStrafingRunTime(min + chicken.getRandom().nextInt(max - min));
    }

    @Override
    public void stop() {
        chicken.endAbility();
        chicken.setMaxStrafingRunTime(0);
        strafingRunTime = 0;
    }

    @Override
    public void tick() {
        ++strafingRunTime;
        --eggCooldown;

        if (strafingRunTime < chicken.getMaxStrafingRunTime() && eggCooldown <= 0 && chicken.getRandom().nextFloat() < ServerConfig.STRAFE_EGG_CHANCE.get()) {
            chicken.launchEggBomb(0.09f, chicken.getRandom().nextBoolean());
            eggCooldown = 5;
        }
    }
}
