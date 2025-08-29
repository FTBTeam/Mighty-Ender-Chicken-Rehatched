package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChickenEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public abstract class ChickenSkillGoal extends Goal {
    protected final EnderChickenEntity chicken;
    protected final double chanceToUse;

    protected ChickenSkillGoal(EnderChickenEntity chicken, double chanceToUse) {
        this.chicken = chicken;
        this.chanceToUse = chanceToUse * (chicken.isChaosChicken() ? ServerConfig.CHAOS_SKILL_CHANCE_MULT.get() : 1.0);
    }
}
