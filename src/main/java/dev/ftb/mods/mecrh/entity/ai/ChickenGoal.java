package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.entity.EnderChicken;
import net.minecraft.world.entity.ai.goal.Goal;

public abstract class ChickenGoal extends Goal {
    protected final EnderChicken chicken;

    protected ChickenGoal(EnderChicken chicken) {
        this.chicken = chicken;
    }

    @Override
    public boolean canUse() {
        return chicken.canUseAbility();
    }
}
