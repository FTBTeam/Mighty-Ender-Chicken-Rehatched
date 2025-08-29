package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChickenEntity;

public class ChickenRegenForcefieldGoal extends ChickenSkillGoal {
    private int breakCooldown;

    public ChickenRegenForcefieldGoal(EnderChickenEntity chicken) {
        super(chicken, ServerConfig.REGEN_FORCEFIELD_CHANCE.get());
    }

    @Override
    public boolean canUse() {
        return chicken.getEggState() < 0 && !chicken.isForceField() && chicken.isAlive() && chicken.getRandom().nextDouble() < chanceToUse;
    }

    @Override
    public boolean canContinueToUse() {
        return breakCooldown > 0;
    }

    @Override
    public void start() {
        breakCooldown = ServerConfig.REGEN_FORCEFIELD_COOLDOWN.get();
    }

    @Override
    public void stop() {
        chicken.setForceField(true);
    }

    @Override
    public void tick() {
        --breakCooldown;
    }
}
