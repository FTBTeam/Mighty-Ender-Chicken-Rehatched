package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChickenEntity;
import dev.ftb.mods.mecrh.registry.ModSounds;

import java.util.EnumSet;

public class ClearAreaGoal extends ChickenSkillGoal {
    private int clearingAreaTime;

    public ClearAreaGoal(EnderChickenEntity chicken) {
        super(chicken, ServerConfig.CLEAR_AREA_CHANCE.get());

        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return chicken.canUseAbility()
                && (chicken.getEggState() >= 0 || chicken.getTarget() != null)
                && chicken.getRandom().nextFloat() < chanceToUse
                && chicken.shouldClearArea();
    }

    @Override
    public boolean canContinueToUse() {
        return chicken.isClearingArea() && chicken.isAlive();
    }

    @Override
    public void start() {
        chicken.useAbility();
        chicken.setClearingArea(true);
        clearingAreaTime = 0;
    }

    @Override
    public void stop() {
        chicken.endAbility();
        clearingAreaTime = 0;
    }

    @Override
    public void tick() {
        if (!chicken.isFiring()) {
            chicken.setXRot(20F);
        }
        ++clearingAreaTime;
        if (clearingAreaTime == 1) {
            chicken.playSound(ModSounds.CLEAR_WARN.get(), 1.0F + 0.2F * chicken.getScale(), 0.8F + chicken.getRandom().nextFloat() * 0.4F);
        } else if (clearingAreaTime == 40) {
            chicken.explode(chicken.partHead.getX(), chicken.partHead.getY(), chicken.partHead.getZ(), 3.75F * chicken.getScale(), false, true);
            chicken.explode(chicken.getX(), chicken.getY(), chicken.getZ(), 8.5F * chicken.getScale(), false, true);
        } else if (clearingAreaTime >= 55) {
            chicken.resetShouldClearArea();
            chicken.setClearingArea(false);
        }
    }
}
