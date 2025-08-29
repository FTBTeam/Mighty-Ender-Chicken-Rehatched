package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.entity.EnderChickenEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BreakEggGoal extends Goal {
    public EnderChickenEntity chicken;
    public float chance;
    public int breakCooldown;

    public BreakEggGoal(EnderChickenEntity chicken, float chance) {
        this.chicken = chicken;
        this.chance = chance;
        setFlags(EnumSet.of(Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.chicken.canUseAbility() && this.chicken.getEggState() >= 0 && this.chicken.level().random.nextFloat() < this.chance;
    }

    @Override
    public void start() {
        this.chicken.useAbility();
        this.chicken.breakEgg();
        this.breakCooldown = 60;
    }

    @Override
    public void stop() {
        this.chicken.endAbility();
    }

    @Override
    public boolean canContinueToUse() {
        return this.breakCooldown > 0;
    }

    @Override
    public void tick() {
        --this.breakCooldown;
    }
}
